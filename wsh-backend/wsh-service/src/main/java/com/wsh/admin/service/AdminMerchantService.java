package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.*;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMerchantService {

    private final MerchantMapper merchantMapper;
    private final MerchantBranchMapper merchantBranchMapper;
    private final ActivityMapper activityMapper;
    private final MerchantAuditLogMapper auditLogMapper;
    private final MerchantOnboardingFeeMapper onboardingFeeMapper;
    private final OnboardingFeePlanMapper onboardingFeePlanMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminOperationLogService operationLogService;

    /**
     * 分页查询商户列表
     */
    public PageResult<AdminMerchantListResponse> listMerchants(PageQueryRequest query) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(Merchant::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Merchant::getMerchantName, query.getKeyword())
                    .or().like(Merchant::getMerchantCode, query.getKeyword())
                    .or().like(Merchant::getContactName, query.getKeyword())
                    .or().like(Merchant::getContactPhone, query.getKeyword()));
        }
        wrapper.orderByDesc(Merchant::getCreatedAt);

        Page<Merchant> page = new Page<>(query.getPage(), query.getSize());
        Page<Merchant> result = merchantMapper.selectPage(page, wrapper);

        List<AdminMerchantListResponse> records = result.getRecords().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    /**
     * 查询商户详情
     */
    public AdminMerchantDetailResponse getMerchantDetail(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        List<MerchantBranch> branches = merchantBranchMapper.selectList(
                new LambdaQueryWrapper<MerchantBranch>()
                        .eq(MerchantBranch::getMerchantId, merchantId));

        List<MerchantAuditLog> auditLogs = auditLogMapper.selectList(
                new LambdaQueryWrapper<MerchantAuditLog>()
                        .eq(MerchantAuditLog::getMerchantId, merchantId)
                        .orderByDesc(MerchantAuditLog::getCreatedAt));

        MerchantOnboardingFee onboardingFee = onboardingFeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantOnboardingFee>()
                        .eq(MerchantOnboardingFee::getMerchantId, merchantId)
                        .orderByDesc(MerchantOnboardingFee::getCreatedAt)
                        .last("LIMIT 1"));

        return buildDetailResponse(merchant, branches, auditLogs, onboardingFee);
    }

    /**
     * 审核商户（通过/拒绝）
     */
    @Transactional
    public void auditMerchant(MerchantAuditRequest request) {
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }
        if (merchant.getStatus() != Constants.MERCHANT_STATUS_PENDING) {
            throw new BusinessException(400, "当前商户状态不允许审核操作");
        }

        int prevStatus = merchant.getStatus();
        int newStatus;
        String action = request.getAction().toUpperCase();

        if (Constants.AUDIT_ACTION_APPROVE.equals(action)) {
            newStatus = Constants.MERCHANT_STATUS_ACTIVE;
        } else if (Constants.AUDIT_ACTION_REJECT.equals(action)) {
            newStatus = Constants.MERCHANT_STATUS_PENDING; // 拒绝后仍为待审核，商户可修改后重新提交
        } else {
            throw new BusinessException(400, "无效的审核动作：" + action);
        }

        merchant.setStatus(newStatus);
        merchantMapper.updateById(merchant);

        // 记录审核日志
        MerchantAuditLog auditLog = new MerchantAuditLog();
        auditLog.setMerchantId(request.getMerchantId());
        auditLog.setAdminId(SecurityUtil.getAdminId() != null ? SecurityUtil.getAdminId() : SecurityUtil.getUserId());
        auditLog.setAction(action);
        auditLog.setPrevStatus(prevStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setReason(request.getReason());
        auditLogMapper.insert(auditLog);

        operationLogService.log("merchant", "AUDIT_" + action,
                String.valueOf(request.getMerchantId()),
                String.format("审核商户[%s]，动作：%s，原因：%s", merchant.getMerchantName(), action, request.getReason()));

        log.info("商户审核: merchantId={}, action={}, prevStatus={}, newStatus={}",
                request.getMerchantId(), action, prevStatus, newStatus);
    }

    /**
     * 冻结/解冻商户
     */
    @Transactional
    public void updateMerchantStatus(MerchantStatusUpdateRequest request) {
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        String action = request.getAction().toUpperCase();
        int prevStatus = merchant.getStatus();
        int newStatus;

        if (Constants.AUDIT_ACTION_FREEZE.equals(action)) {
            if (merchant.getStatus() != Constants.MERCHANT_STATUS_ACTIVE) {
                throw new BusinessException(400, "只有正常状态的商户可以冻结");
            }
            newStatus = Constants.MERCHANT_STATUS_FROZEN;
        } else if (Constants.AUDIT_ACTION_UNFREEZE.equals(action)) {
            if (merchant.getStatus() != Constants.MERCHANT_STATUS_FROZEN) {
                throw new BusinessException(400, "只有冻结状态的商户可以解冻");
            }
            newStatus = Constants.MERCHANT_STATUS_ACTIVE;
        } else {
            throw new BusinessException(400, "无效的操作：" + action);
        }

        merchant.setStatus(newStatus);
        merchantMapper.updateById(merchant);

        MerchantAuditLog auditLog = new MerchantAuditLog();
        auditLog.setMerchantId(request.getMerchantId());
        auditLog.setAdminId(SecurityUtil.getAdminId() != null ? SecurityUtil.getAdminId() : SecurityUtil.getUserId());
        auditLog.setAction(action);
        auditLog.setPrevStatus(prevStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setReason(request.getReason());
        auditLogMapper.insert(auditLog);

        operationLogService.log("merchant", action,
                String.valueOf(request.getMerchantId()),
                String.format("%s商户[%s]，原因：%s", action, merchant.getMerchantName(), request.getReason()));

        log.info("商户状态更新: merchantId={}, action={}, {} -> {}",
                request.getMerchantId(), action, prevStatus, newStatus);
    }

    // ==================== 私有方法 ====================

    private AdminMerchantListResponse toListResponse(Merchant m) {
        Long branchCount = merchantBranchMapper.selectCount(
                new LambdaQueryWrapper<MerchantBranch>()
                        .eq(MerchantBranch::getMerchantId, m.getMerchantId()));
        Long activityCount = activityMapper.selectCount(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getMerchantId, m.getMerchantId()));

        return AdminMerchantListResponse.builder()
                .merchantId(m.getMerchantId())
                .merchantCode(m.getMerchantCode())
                .merchantName(m.getMerchantName())
                .contactName(m.getContactName())
                .contactPhone(m.getContactPhone())
                .city(m.getCity())
                .businessCategory(m.getBusinessCategory())
                .status(m.getStatus())
                .profitSharingRate(m.getProfitSharingRate())
                .integrationType(m.getIntegrationType())
                .branchCount(branchCount.intValue())
                .activityCount(activityCount.intValue())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private AdminMerchantDetailResponse buildDetailResponse(Merchant merchant,
                                                             List<MerchantBranch> branches,
                                                             List<MerchantAuditLog> auditLogs,
                                                             MerchantOnboardingFee fee) {
        List<AdminMerchantDetailResponse.BranchItem> branchItems = branches.stream()
                .map(b -> AdminMerchantDetailResponse.BranchItem.builder()
                        .branchId(b.getBranchId())
                        .branchName(b.getBranchName())
                        .address(b.getAddress())
                        .status(b.getStatus())
                        .build())
                .collect(Collectors.toList());

        List<AdminMerchantDetailResponse.AuditLogItem> logItems = auditLogs.stream()
                .map(l -> {
                    String adminName = "";
                    AdminUser admin = adminUserMapper.selectById(l.getAdminId());
                    if (admin != null) {
                        adminName = admin.getRealName();
                    }
                    return AdminMerchantDetailResponse.AuditLogItem.builder()
                            .logId(l.getLogId())
                            .action(l.getAction())
                            .prevStatus(l.getPrevStatus())
                            .newStatus(l.getNewStatus())
                            .reason(l.getReason())
                            .adminName(adminName)
                            .createdAt(l.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        AdminMerchantDetailResponse.OnboardingFeeItem feeItem = null;
        if (fee != null) {
            OnboardingFeePlan plan = onboardingFeePlanMapper.selectById(fee.getPlanId());
            feeItem = AdminMerchantDetailResponse.OnboardingFeeItem.builder()
                    .feeId(fee.getFeeId())
                    .planName(plan != null ? plan.getPlanName() : "未知套餐")
                    .feeAmount(fee.getFeeAmount())
                    .payStatus(fee.getPayStatus())
                    .payTime(fee.getPayTime())
                    .validStartDate(fee.getValidStartDate() != null ? fee.getValidStartDate().toString() : null)
                    .validEndDate(fee.getValidEndDate() != null ? fee.getValidEndDate().toString() : null)
                    .build();
        }

        return AdminMerchantDetailResponse.builder()
                .merchantId(merchant.getMerchantId())
                .merchantCode(merchant.getMerchantCode())
                .merchantName(merchant.getMerchantName())
                .logoUrl(merchant.getLogoUrl())
                .contactName(merchant.getContactName())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .city(merchant.getCity())
                .longitude(merchant.getLongitude())
                .latitude(merchant.getLatitude())
                .businessCategory(merchant.getBusinessCategory())
                .status(merchant.getStatus())
                .integrationType(merchant.getIntegrationType())
                .profitSharingRate(merchant.getProfitSharingRate())
                .createdAt(merchant.getCreatedAt())
                .branches(branchItems)
                .onboardingFee(feeItem)
                .auditLogs(logItems)
                .build();
    }
}
