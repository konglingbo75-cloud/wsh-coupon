package com.wsh.verification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.util.IdGenerator;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import com.wsh.integration.adapter.AdapterFactory;
import com.wsh.integration.wechat.WechatPayService;
import com.wsh.verification.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 核销服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VoucherMapper voucherMapper;
    private final VerificationRecordMapper verificationRecordMapper;
    private final ProfitSharingMapper profitSharingMapper;
    private final ServiceFeeRecordMapper serviceFeeRecordMapper;
    private final OrderMapper orderMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantBranchMapper branchMapper;
    private final MerchantEmployeeMapper employeeMapper;
    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final AdapterFactory adapterFactory;
    private final WechatPayService wechatPayService;

    /**
     * 扫码核销
     */
    @Transactional(rollbackFor = Exception.class)
    public VerifyResponse verify(VerifyRequest request, Long merchantId, Long employeeId) {
        String voucherCode = request.getVoucherCode();
        
        // 1. 查询券码
        Voucher voucher = voucherMapper.selectByVoucherCode(voucherCode);
        if (voucher == null) {
            throw new BusinessException("券码不存在");
        }
        
        // 2. 校验券码
        validateVoucher(voucher, merchantId);
        
        // 3. 更新券码状态
        voucher.setStatus(Constants.VOUCHER_STATUS_USED);
        voucher.setUsedTime(LocalDateTime.now());
        voucher.setUsedBranchId(request.getBranchId());
        voucher.setUsedEmployeeId(employeeId);
        voucherMapper.updateById(voucher);
        
        // 4. 创建核销记录
        VerificationRecord record = createVerificationRecord(voucher, request.getBranchId(), employeeId);
        verificationRecordMapper.insert(record);
        
        // 5. 触发分账
        ServiceFeeRecord feeRecord = triggerProfitSharing(voucher, record);
        
        // 6. 更新沉睡会员状态（如果是沉睡唤醒券）
        if (record.getIsDormancyAwake() != null && record.getIsDormancyAwake() == 1) {
            updateDormancyStatus(voucher.getUserId(), voucher.getMerchantId());
        }
        
        // 7. 异步同步核销到商户系统
        asyncSyncToMerchant(record);
        
        log.info("核销成功: voucherCode={}, merchantId={}, employeeId={}", voucherCode, merchantId, employeeId);
        
        // 8. 构建响应
        return buildVerifyResponse(voucher, record, feeRecord);
    }

    /**
     * 查询商户核销记录
     */
    public VerificationRecordListResponse getMerchantRecords(Long merchantId, Integer page, Integer pageSize) {
        List<VerificationRecord> allRecords = verificationRecordMapper.selectByMerchantId(merchantId);
        int total = allRecords.size();
        
        // 分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<VerificationRecord> pagedRecords = start < total ? allRecords.subList(start, end) : Collections.emptyList();
        
        // 统计今日数据
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int todayCount = 0;
        BigDecimal todayAmount = BigDecimal.ZERO;
        
        for (VerificationRecord r : allRecords) {
            if (r.getVerifyTime() != null && r.getVerifyTime().isAfter(todayStart)) {
                todayCount++;
                Voucher v = voucherMapper.selectById(r.getVoucherId());
                if (v != null && v.getVoucherValue() != null) {
                    todayAmount = todayAmount.add(v.getVoucherValue());
                }
            }
        }
        
        // 转换
        List<VerificationRecordListResponse.RecordItem> items = pagedRecords.stream()
                .map(this::convertRecordItem)
                .collect(Collectors.toList());
        
        return VerificationRecordListResponse.builder()
                .records(items)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .todayCount(todayCount)
                .todayAmount(todayAmount)
                .build();
    }

    /**
     * 查询员工核销记录
     */
    public VerificationRecordListResponse getEmployeeRecords(Long employeeId, Integer page, Integer pageSize) {
        List<VerificationRecord> allRecords = verificationRecordMapper.selectByEmployeeId(employeeId);
        int total = allRecords.size();
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<VerificationRecord> pagedRecords = start < total ? allRecords.subList(start, end) : Collections.emptyList();
        
        List<VerificationRecordListResponse.RecordItem> items = pagedRecords.stream()
                .map(this::convertRecordItem)
                .collect(Collectors.toList());
        
        return VerificationRecordListResponse.builder()
                .records(items)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 查询服务费账单
     */
    public ServiceFeeBillResponse getServiceFeeBill(Long merchantId, Integer year, Integer month, 
                                                     Integer page, Integer pageSize) {
        List<ServiceFeeRecord> records;
        
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDateTime startTime = ym.atDay(1).atStartOfDay();
            LocalDateTime endTime = ym.plusMonths(1).atDay(1).atStartOfDay();
            records = serviceFeeRecordMapper.selectByMerchantIdAndTimeRange(merchantId, startTime, endTime);
        } else {
            records = serviceFeeRecordMapper.selectByMerchantId(merchantId);
        }
        
        int total = records.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<ServiceFeeRecord> pagedRecords = start < total ? records.subList(start, end) : Collections.emptyList();
        
        List<ServiceFeeBillResponse.FeeItem> items = pagedRecords.stream()
                .map(this::convertFeeItem)
                .collect(Collectors.toList());
        
        return ServiceFeeBillResponse.builder()
                .records(items)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 查询服务费汇总
     */
    public ServiceFeeSummaryResponse getServiceFeeSummary(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        BigDecimal feeRate = merchant != null && merchant.getProfitSharingRate() != null 
                ? merchant.getProfitSharingRate() 
                : new BigDecimal("0.02");
        
        // 总计
        List<ServiceFeeRecord> allRecords = serviceFeeRecordMapper.selectByMerchantId(merchantId);
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        BigDecimal totalServiceFee = BigDecimal.ZERO;
        BigDecimal totalMerchantAmount = BigDecimal.ZERO;
        
        for (ServiceFeeRecord r : allRecords) {
            if (r.getOrderAmount() != null) totalOrderAmount = totalOrderAmount.add(r.getOrderAmount());
            if (r.getServiceFee() != null) totalServiceFee = totalServiceFee.add(r.getServiceFee());
            if (r.getMerchantAmount() != null) totalMerchantAmount = totalMerchantAmount.add(r.getMerchantAmount());
        }
        
        // 本月
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        
        List<ServiceFeeRecord> monthRecords = serviceFeeRecordMapper.selectByMerchantIdAndTimeRange(
                merchantId, monthStart, monthEnd);
        
        BigDecimal monthOrderAmount = BigDecimal.ZERO;
        BigDecimal monthServiceFee = BigDecimal.ZERO;
        BigDecimal monthMerchantAmount = BigDecimal.ZERO;
        
        for (ServiceFeeRecord r : monthRecords) {
            if (r.getOrderAmount() != null) monthOrderAmount = monthOrderAmount.add(r.getOrderAmount());
            if (r.getServiceFee() != null) monthServiceFee = monthServiceFee.add(r.getServiceFee());
            if (r.getMerchantAmount() != null) monthMerchantAmount = monthMerchantAmount.add(r.getMerchantAmount());
        }
        
        String feeRatePercent = feeRate.multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
        
        return ServiceFeeSummaryResponse.builder()
                .totalOrderAmount(totalOrderAmount)
                .totalServiceFee(totalServiceFee)
                .totalMerchantAmount(totalMerchantAmount)
                .totalCount(allRecords.size())
                .monthOrderAmount(monthOrderAmount)
                .monthServiceFee(monthServiceFee)
                .monthMerchantAmount(monthMerchantAmount)
                .monthCount(monthRecords.size())
                .serviceFeeRate(feeRate)
                .serviceFeeRatePercent(feeRatePercent)
                .build();
    }

    // ==================== 私有方法 ====================

    private void validateVoucher(Voucher voucher, Long merchantId) {
        // 校验商户
        if (!voucher.getMerchantId().equals(merchantId)) {
            throw new BusinessException("此券码不属于当前商户");
        }
        
        // 校验状态
        if (voucher.getStatus() == Constants.VOUCHER_STATUS_USED) {
            throw new BusinessException("券码已使用");
        }
        if (voucher.getStatus() == Constants.VOUCHER_STATUS_EXPIRED) {
            throw new BusinessException("券码已过期");
        }
        
        // 校验有效期
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getValidStartTime() != null && now.isBefore(voucher.getValidStartTime())) {
            throw new BusinessException("券码尚未生效");
        }
        if (voucher.getValidEndTime() != null && now.isAfter(voucher.getValidEndTime())) {
            // 更新状态为过期
            voucher.setStatus(Constants.VOUCHER_STATUS_EXPIRED);
            voucherMapper.updateById(voucher);
            throw new BusinessException("券码已过期");
        }
    }

    private VerificationRecord createVerificationRecord(Voucher voucher, Long branchId, Long employeeId) {
        // 判断是否沉睡唤醒核销
        Order order = orderMapper.selectById(voucher.getOrderId());
        int isDormancyAwake = (order != null && order.getIsDormancyAwake() != null && order.getIsDormancyAwake() == 1) 
                ? 1 : 0;
        
        VerificationRecord record = new VerificationRecord();
        record.setRecordId(IdGenerator.nextId());
        record.setVoucherId(voucher.getVoucherId());
        record.setVoucherCode(voucher.getVoucherCode());
        record.setUserId(voucher.getUserId());
        record.setMerchantId(voucher.getMerchantId());
        record.setBranchId(branchId);
        record.setEmployeeId(employeeId);
        record.setVerifyTime(LocalDateTime.now());
        record.setIsDormancyAwake(isDormancyAwake);
        record.setSyncStatus(0);
        record.setCreatedAt(LocalDateTime.now());
        
        return record;
    }

    private ServiceFeeRecord triggerProfitSharing(Voucher voucher, VerificationRecord record) {
        // 更新分账记录状态
        ProfitSharing sharing = profitSharingMapper.selectByVoucherId(voucher.getVoucherId());
        if (sharing != null) {
            profitSharingMapper.updateVerifyTriggered(voucher.getVoucherId(), record.getRecordId());
        }
        
        // 获取订单信息
        Order order = orderMapper.selectById(voucher.getOrderId());
        if (order == null) {
            log.warn("核销时未找到订单: voucherId={}", voucher.getVoucherId());
            return null;
        }
        
        // 获取商户服务费率
        Merchant merchant = merchantMapper.selectById(voucher.getMerchantId());
        BigDecimal feeRate = merchant != null && merchant.getProfitSharingRate() != null 
                ? merchant.getProfitSharingRate() 
                : new BigDecimal("0.02");
        
        // 计算服务费
        BigDecimal orderAmount = order.getPayAmount();
        BigDecimal serviceFee = orderAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal merchantAmount = orderAmount.subtract(serviceFee);
        
        // 创建服务费记录
        ServiceFeeRecord feeRecord = new ServiceFeeRecord();
        feeRecord.setRecordId(IdGenerator.nextId());
        feeRecord.setMerchantId(voucher.getMerchantId());
        feeRecord.setOrderNo(order.getOrderNo());
        feeRecord.setVoucherId(voucher.getVoucherId());
        feeRecord.setVerifyRecordId(record.getRecordId());
        feeRecord.setOrderAmount(orderAmount);
        feeRecord.setServiceFeeRate(feeRate);
        feeRecord.setServiceFee(serviceFee);
        feeRecord.setMerchantAmount(merchantAmount);
        feeRecord.setSharingStatus(0); // 待分账
        feeRecord.setCreatedAt(LocalDateTime.now());
        
        serviceFeeRecordMapper.insert(feeRecord);
        
        // 异步调用微信分账
        asyncExecuteProfitSharing(sharing, feeRecord);
        
        log.info("服务费记录已创建: orderNo={}, serviceFee={}, merchantAmount={}", 
                order.getOrderNo(), serviceFee, merchantAmount);
        
        return feeRecord;
    }

    @Async
    protected void asyncExecuteProfitSharing(ProfitSharing sharing, ServiceFeeRecord feeRecord) {
        try {
            // 调用微信分账API
            boolean success = wechatPayService.executeProfitSharing(
                    sharing != null ? sharing.getTransactionId() : null,
                    sharing != null ? sharing.getOrderNo() : feeRecord.getOrderNo(),
                    feeRecord.getMerchantAmount()
            );
            
            // 更新状态
            int status = success ? Constants.SHARING_STATUS_SUCCESS : Constants.SHARING_STATUS_FAILED;
            if (sharing != null) {
                profitSharingMapper.updateStatus(sharing.getSharingId(), status);
            }
            
            feeRecord.setSharingStatus(success ? 1 : 2);
            serviceFeeRecordMapper.updateById(feeRecord);
            
            log.info("分账执行完成: orderNo={}, success={}", feeRecord.getOrderNo(), success);
        } catch (Exception e) {
            log.error("分账执行失败: orderNo={}", feeRecord.getOrderNo(), e);
            if (sharing != null) {
                profitSharingMapper.incrementRetryCount(sharing.getSharingId());
            }
        }
    }

    private void updateDormancyStatus(Long userId, Long merchantId) {
        MerchantMemberSnapshot snapshot = snapshotMapper.selectByUserAndMerchant(userId, merchantId);
        if (snapshot != null && snapshot.getDormancyLevel() != null && snapshot.getDormancyLevel() > 0) {
            snapshot.setDormancyLevel(Constants.DORMANCY_ACTIVE);
            snapshot.setLastConsumeTime(LocalDateTime.now());
            snapshotMapper.updateById(snapshot);
            log.info("沉睡会员已唤醒: userId={}, merchantId={}", userId, merchantId);
        }
    }

    @Async
    protected void asyncSyncToMerchant(VerificationRecord record) {
        try {
            // 获取商户适配器并同步
            var adapter = adapterFactory.getAdapter(record.getMerchantId());
            if (adapter != null) {
                // TODO: 实现核销同步到商户系统
                log.debug("异步同步核销到商户系统: recordId={}", record.getRecordId());
            }
        } catch (Exception e) {
            log.error("同步核销到商户系统失败: recordId={}", record.getRecordId(), e);
        }
    }

    private VerifyResponse buildVerifyResponse(Voucher voucher, VerificationRecord record, ServiceFeeRecord feeRecord) {
        User user = userMapper.selectById(voucher.getUserId());
        Activity activity = activityMapper.selectById(voucher.getActivityId());
        
        return VerifyResponse.builder()
                .recordId(record.getRecordId())
                .voucherId(voucher.getVoucherId())
                .voucherCode(voucher.getVoucherCode())
                .voucherType(voucher.getVoucherType())
                .voucherTypeName(getVoucherTypeName(voucher.getVoucherType()))
                .voucherValue(voucher.getVoucherValue())
                .userId(voucher.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userAvatar(user != null ? user.getAvatarUrl() : null)
                .activityName(activity != null ? activity.getActivityName() : null)
                .verifyTime(record.getVerifyTime())
                .isDormancyAwake(record.getIsDormancyAwake() != null && record.getIsDormancyAwake() == 1)
                .serviceFee(feeRecord != null ? feeRecord.getServiceFee() : null)
                .merchantAmount(feeRecord != null ? feeRecord.getMerchantAmount() : null)
                .build();
    }

    private VerificationRecordListResponse.RecordItem convertRecordItem(VerificationRecord record) {
        Voucher voucher = voucherMapper.selectById(record.getVoucherId());
        Activity activity = voucher != null ? activityMapper.selectById(voucher.getActivityId()) : null;
        MerchantBranch branch = record.getBranchId() != null ? branchMapper.selectById(record.getBranchId()) : null;
        MerchantEmployee employee = record.getEmployeeId() != null ? employeeMapper.selectById(record.getEmployeeId()) : null;
        User user = record.getUserId() != null ? userMapper.selectById(record.getUserId()) : null;
        
        // 获取服务费信息
        ServiceFeeRecord feeRecord = serviceFeeRecordMapper.selectByOrderNo(
                voucher != null && voucher.getOrderId() != null 
                        ? orderMapper.selectById(voucher.getOrderId()).getOrderNo() 
                        : null);
        
        return VerificationRecordListResponse.RecordItem.builder()
                .recordId(record.getRecordId())
                .voucherId(record.getVoucherId())
                .voucherCode(record.getVoucherCode())
                .voucherType(voucher != null ? voucher.getVoucherType() : null)
                .voucherTypeName(voucher != null ? getVoucherTypeName(voucher.getVoucherType()) : null)
                .voucherValue(voucher != null ? voucher.getVoucherValue() : null)
                .userId(record.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .activityName(activity != null ? activity.getActivityName() : null)
                .branchName(branch != null ? branch.getBranchName() : null)
                .employeeName(employee != null ? employee.getName() : null)
                .verifyTime(record.getVerifyTime())
                .isDormancyAwake(record.getIsDormancyAwake() != null && record.getIsDormancyAwake() == 1)
                .serviceFee(feeRecord != null ? feeRecord.getServiceFee() : null)
                .merchantAmount(feeRecord != null ? feeRecord.getMerchantAmount() : null)
                .build();
    }

    private ServiceFeeBillResponse.FeeItem convertFeeItem(ServiceFeeRecord record) {
        Voucher voucher = voucherMapper.selectById(record.getVoucherId());
        
        String feeRatePercent = record.getServiceFeeRate() != null 
                ? record.getServiceFeeRate().multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP).toPlainString() + "%"
                : null;
        
        return ServiceFeeBillResponse.FeeItem.builder()
                .recordId(record.getRecordId())
                .orderNo(record.getOrderNo())
                .voucherId(record.getVoucherId())
                .voucherCode(voucher != null ? voucher.getVoucherCode() : null)
                .orderAmount(record.getOrderAmount())
                .serviceFeeRate(record.getServiceFeeRate())
                .serviceFeeRatePercent(feeRatePercent)
                .serviceFee(record.getServiceFee())
                .merchantAmount(record.getMerchantAmount())
                .sharingStatus(record.getSharingStatus())
                .sharingStatusName(getSharingStatusName(record.getSharingStatus()))
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String getVoucherTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "代金券";
            case 2 -> "兑换券";
            case 3 -> "储值码";
            case 4 -> "沉睡唤醒券";
            default -> "未知";
        };
    }

    private String getSharingStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待分账";
            case 1 -> "已分账";
            case 2 -> "分账失败";
            default -> "未知";
        };
    }
}
