package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.*;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBillingService {

    private final MerchantOnboardingFeeMapper onboardingFeeMapper;
    private final MerchantPackagePurchaseMapper purchaseMapper;
    private final MerchantDepositMapper depositMapper;
    private final MonthlyServiceFeeMapper monthlyFeeMapper;
    private final ServicePackageMapper packageMapper;
    private final OnboardingFeePlanMapper planMapper;
    private final MerchantMapper merchantMapper;

    // ========== 入驻费 ==========

    public PageResult<AdminBillingListResponse> listBillings(PageQueryRequest query) {
        LambdaQueryWrapper<MerchantOnboardingFee> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(MerchantOnboardingFee::getPayStatus, query.getStatus());
        }
        wrapper.orderByDesc(MerchantOnboardingFee::getCreatedAt);

        Page<MerchantOnboardingFee> page = new Page<>(query.getPage(), query.getSize());
        Page<MerchantOnboardingFee> result = onboardingFeeMapper.selectPage(page, wrapper);

        List<AdminBillingListResponse> records = result.getRecords().stream()
                .map(this::toOnboardingResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    // ========== 套餐购买记录 ==========

    public PageResult<AdminPackagePurchaseResponse> listPackagePurchases(PageQueryRequest query) {
        LambdaQueryWrapper<MerchantPackagePurchase> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(MerchantPackagePurchase::getPayStatus, query.getStatus());
        }
        wrapper.orderByDesc(MerchantPackagePurchase::getCreatedAt);

        Page<MerchantPackagePurchase> page = new Page<>(query.getPage(), query.getSize());
        Page<MerchantPackagePurchase> result = purchaseMapper.selectPage(page, wrapper);

        List<AdminPackagePurchaseResponse> records = result.getRecords().stream()
                .map(this::toPurchaseResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    // ========== 保证金记录 ==========

    public PageResult<AdminDepositResponse> listDeposits(PageQueryRequest query) {
        LambdaQueryWrapper<MerchantDeposit> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(MerchantDeposit::getPayStatus, query.getStatus());
        }
        wrapper.orderByDesc(MerchantDeposit::getCreatedAt);

        Page<MerchantDeposit> page = new Page<>(query.getPage(), query.getSize());
        Page<MerchantDeposit> result = depositMapper.selectPage(page, wrapper);

        List<AdminDepositResponse> records = result.getRecords().stream()
                .map(this::toDepositResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    // ========== 月度服务费 ==========

    public PageResult<AdminServiceFeeSummaryResponse> listServiceFeeSummaries(PageQueryRequest query) {
        LambdaQueryWrapper<MonthlyServiceFee> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(MonthlyServiceFee::getDeductStatus, query.getStatus());
        }
        wrapper.orderByDesc(MonthlyServiceFee::getYearMonth);

        Page<MonthlyServiceFee> page = new Page<>(query.getPage(), query.getSize());
        Page<MonthlyServiceFee> result = monthlyFeeMapper.selectPage(page, wrapper);

        List<AdminServiceFeeSummaryResponse> records = result.getRecords().stream()
                .map(this::toServiceFeeResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    // ==================== 私有方法 ====================

    private AdminBillingListResponse toOnboardingResponse(MerchantOnboardingFee fee) {
        Merchant merchant = merchantMapper.selectById(fee.getMerchantId());
        OnboardingFeePlan plan = planMapper.selectById(fee.getPlanId());

        return AdminBillingListResponse.builder()
                .feeId(fee.getFeeId())
                .merchantId(fee.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : "未知商户")
                .planName(plan != null ? plan.getPlanName() : "未知套餐")
                .feeAmount(fee.getFeeAmount())
                .payStatus(fee.getPayStatus())
                .payTime(fee.getPayTime())
                .validStartDate(fee.getValidStartDate() != null ? fee.getValidStartDate().toString() : null)
                .validEndDate(fee.getValidEndDate() != null ? fee.getValidEndDate().toString() : null)
                .createdAt(fee.getCreatedAt())
                .build();
    }

    private AdminPackagePurchaseResponse toPurchaseResponse(MerchantPackagePurchase purchase) {
        Merchant merchant = merchantMapper.selectById(purchase.getMerchantId());
        ServicePackage pkg = packageMapper.selectById(purchase.getPackageId());

        return AdminPackagePurchaseResponse.builder()
                .purchaseId(purchase.getPurchaseId())
                .merchantId(purchase.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : "未知商户")
                .packageName(pkg != null ? pkg.getPackageName() : "未知套餐")
                .pricePaid(purchase.getPricePaid())
                .payStatus(purchase.getPayStatus())
                .payTime(purchase.getPayTime())
                .validStartDate(purchase.getValidStartDate() != null ? purchase.getValidStartDate().toString() : null)
                .validEndDate(purchase.getValidEndDate() != null ? purchase.getValidEndDate().toString() : null)
                .createdAt(purchase.getCreatedAt())
                .build();
    }

    private AdminDepositResponse toDepositResponse(MerchantDeposit deposit) {
        Merchant merchant = merchantMapper.selectById(deposit.getMerchantId());

        return AdminDepositResponse.builder()
                .depositId(deposit.getDepositId())
                .merchantId(deposit.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : "未知商户")
                .depositAmount(deposit.getDepositAmount())
                .payStatus(deposit.getPayStatus())
                .payTime(deposit.getPayTime())
                .refundTime(deposit.getRefundTime())
                .refundReason(deposit.getRefundReason())
                .createdAt(deposit.getCreatedAt())
                .build();
    }

    private AdminServiceFeeSummaryResponse toServiceFeeResponse(MonthlyServiceFee fee) {
        Merchant merchant = merchantMapper.selectById(fee.getMerchantId());

        return AdminServiceFeeSummaryResponse.builder()
                .summaryId(fee.getSummaryId())
                .merchantId(fee.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : "未知商户")
                .yearMonth(fee.getYearMonth())
                .orderCount(fee.getOrderCount())
                .totalAmount(fee.getTotalAmount())
                .serviceFee(fee.getServiceFee())
                .deductStatus(fee.getDeductStatus())
                .deductTime(fee.getDeductTime())
                .createdAt(fee.getCreatedAt())
                .build();
    }

    // ========== 套餐管理 ==========

    public PageResult<PackageManageResponse> listPackageManage(PageQueryRequest query) {
        LambdaQueryWrapper<ServicePackage> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(ServicePackage::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(ServicePackage::getSortOrder)
               .orderByDesc(ServicePackage::getCreatedAt);

        Page<ServicePackage> page = new Page<>(query.getPage(), query.getSize());
        Page<ServicePackage> result = packageMapper.selectPage(page, wrapper);

        List<PackageManageResponse> records = result.getRecords().stream()
                .map(this::toPackageManageResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    @Transactional
    public Long createPackage(PackageCreateRequest request) {
        ServicePackage pkg = new ServicePackage();
        BeanUtils.copyProperties(request, pkg);
        pkg.setStatus(1); // 默认启用
        if (pkg.getSortOrder() == null) {
            pkg.setSortOrder(0);
        }
        packageMapper.insert(pkg);
        log.info("创建套餐成功: packageId={}, packageName={}", pkg.getPackageId(), pkg.getPackageName());
        return pkg.getPackageId();
    }

    @Transactional
    public void updatePackage(Long packageId, PackageUpdateRequest request) {
        ServicePackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new RuntimeException("套餐不存在: " + packageId);
        }

        if (request.getPackageName() != null) {
            pkg.setPackageName(request.getPackageName());
        }
        if (request.getPackageType() != null) {
            pkg.setPackageType(request.getPackageType());
        }
        if (request.getPrice() != null) {
            pkg.setPrice(request.getPrice());
        }
        if (request.getDurationMonths() != null) {
            pkg.setDurationMonths(request.getDurationMonths());
        }
        if (request.getServiceFeeRate() != null) {
            pkg.setServiceFeeRate(request.getServiceFeeRate());
        }
        if (request.getFeatures() != null) {
            pkg.setFeatures(request.getFeatures());
        }
        if (request.getSortOrder() != null) {
            pkg.setSortOrder(request.getSortOrder());
        }

        packageMapper.updateById(pkg);
        log.info("更新套餐成功: packageId={}", packageId);
    }

    @Transactional
    public void updatePackageStatus(Long packageId, PackageStatusRequest request) {
        ServicePackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new RuntimeException("套餐不存在: " + packageId);
        }

        pkg.setStatus(request.getStatus());
        packageMapper.updateById(pkg);
        log.info("更新套餐状态成功: packageId={}, status={}", packageId, request.getStatus());
    }

    private PackageManageResponse toPackageManageResponse(ServicePackage pkg) {
        return PackageManageResponse.builder()
                .packageId(pkg.getPackageId())
                .packageName(pkg.getPackageName())
                .packageType(pkg.getPackageType())
                .price(pkg.getPrice())
                .durationMonths(pkg.getDurationMonths())
                .serviceFeeRate(pkg.getServiceFeeRate())
                .features(pkg.getFeatures())
                .status(pkg.getStatus())
                .sortOrder(pkg.getSortOrder())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .build();
    }

    // ========== 入驻费套餐管理 ==========

    public PageResult<OnboardingPlanManageResponse> listOnboardingPlanManage(PageQueryRequest query) {
        LambdaQueryWrapper<OnboardingFeePlan> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(OnboardingFeePlan::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(OnboardingFeePlan::getPlanType)
               .orderByAsc(OnboardingFeePlan::getFeeAmount);

        Page<OnboardingFeePlan> page = new Page<>(query.getPage(), query.getSize());
        Page<OnboardingFeePlan> result = planMapper.selectPage(page, wrapper);

        List<OnboardingPlanManageResponse> records = result.getRecords().stream()
                .map(this::toOnboardingPlanResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    @Transactional
    public Long createOnboardingPlan(OnboardingPlanCreateRequest request) {
        OnboardingFeePlan plan = new OnboardingFeePlan();
        BeanUtils.copyProperties(request, plan);
        plan.setStatus(1); // 默认启用
        planMapper.insert(plan);
        log.info("创建入驻费套餐成功: planId={}, planName={}", plan.getPlanId(), plan.getPlanName());
        return plan.getPlanId();
    }

    @Transactional
    public void updateOnboardingPlan(Long planId, OnboardingPlanUpdateRequest request) {
        OnboardingFeePlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("入驻费套餐不存在: " + planId);
        }

        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if (request.getPlanType() != null) {
            plan.setPlanType(request.getPlanType());
        }
        if (request.getFeeAmount() != null) {
            plan.setFeeAmount(request.getFeeAmount());
        }
        if (request.getDurationMonths() != null) {
            plan.setDurationMonths(request.getDurationMonths());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }

        planMapper.updateById(plan);
        log.info("更新入驻费套餐成功: planId={}", planId);
    }

    @Transactional
    public void updateOnboardingPlanStatus(Long planId, OnboardingPlanStatusRequest request) {
        OnboardingFeePlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("入驻费套餐不存在: " + planId);
        }

        plan.setStatus(request.getStatus());
        planMapper.updateById(plan);
        log.info("更新入驻费套餐状态成功: planId={}, status={}", planId, request.getStatus());
    }

    private OnboardingPlanManageResponse toOnboardingPlanResponse(OnboardingFeePlan plan) {
        return OnboardingPlanManageResponse.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .planType(plan.getPlanType())
                .feeAmount(plan.getFeeAmount())
                .durationMonths(plan.getDurationMonths())
                .description(plan.getDescription())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .build();
    }

    // ========== 保证金退款审核 ==========

    @Transactional
    public void handleDepositRefund(Long depositId, DepositRefundRequest request) {
        MerchantDeposit deposit = depositMapper.selectById(depositId);
        if (deposit == null) {
            throw new RuntimeException("保证金记录不存在: " + depositId);
        }

        if (deposit.getPayStatus() != 2) {
            throw new RuntimeException("只能审核退还中状态的保证金");
        }

        if ("approve".equals(request.getAction())) {
            // 审核通过
            deposit.setPayStatus(3); // 已退还
            deposit.setRefundTime(LocalDateTime.now());
            depositMapper.updateById(deposit);
            log.info("保证金退款审核通过: depositId={}", depositId);
        } else if ("reject".equals(request.getAction())) {
            // 审核拒绝
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new RuntimeException("拒绝退款时必须填写原因");
            }
            deposit.setPayStatus(1); // 回退为已缴
            deposit.setRefundReason("退款被拒绝: " + request.getReason());
            depositMapper.updateById(deposit);
            log.info("保证金退款审核拒绝: depositId={}, reason={}", depositId, request.getReason());
        } else {
            throw new RuntimeException("无效的审核动作: " + request.getAction());
        }
    }

    // ========== 服务费手动调整 ==========

    @Transactional
    public void manualDeductServiceFee(Long summaryId) {
        MonthlyServiceFee fee = monthlyFeeMapper.selectById(summaryId);
        if (fee == null) {
            throw new RuntimeException("服务费记录不存在: " + summaryId);
        }

        if (fee.getDeductStatus() == 1) {
            throw new RuntimeException("该服务费已扣减，无需重复操作");
        }

        // 直接更新为已扣减（实际生产环境需要操作余额表）
        fee.setDeductStatus(1);
        fee.setDeductTime(LocalDateTime.now());
        monthlyFeeMapper.updateById(fee);
        log.info("手动扣减服务费成功: summaryId={}, merchantId={}, serviceFee={}", 
                summaryId, fee.getMerchantId(), fee.getServiceFee());
    }

    @Transactional
    public void adjustServiceFeeStatus(Long summaryId, ServiceFeeAdjustRequest request) {
        MonthlyServiceFee fee = monthlyFeeMapper.selectById(summaryId);
        if (fee == null) {
            throw new RuntimeException("服务费记录不存在: " + summaryId);
        }

        Integer oldStatus = fee.getDeductStatus();
        fee.setDeductStatus(request.getNewStatus());

        // 如果调整为已扣减，设置扣减时间
        if (request.getNewStatus() == 1 && oldStatus != 1) {
            fee.setDeductTime(LocalDateTime.now());
        }
        // 如果从已扣减改为其他状态，清空扣减时间
        if (oldStatus == 1 && request.getNewStatus() != 1) {
            fee.setDeductTime(null);
        }

        monthlyFeeMapper.updateById(fee);
        log.info("调整服务费状态成功: summaryId={}, oldStatus={}, newStatus={}, reason={}", 
                summaryId, oldStatus, request.getNewStatus(), request.getReason());
    }
}
