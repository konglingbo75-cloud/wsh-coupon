package com.wsh.admin.controller;

import com.wsh.admin.dto.*;
import com.wsh.admin.service.AdminBillingService;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "平台-费用管理", description = "入驻费、套餐、保证金、服务费查询及套餐管理")
@RestController
@RequestMapping("/v1/admin/billing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminBillingController {

    private final AdminBillingService adminBillingService;

    @Operation(summary = "入驻费列表", description = "分页查询商户入驻费记录")
    @GetMapping
    public R<PageResult<AdminBillingListResponse>> listBillings(PageQueryRequest query) {
        return R.ok(adminBillingService.listBillings(query));
    }

    @Operation(summary = "套餐购买记录", description = "分页查询商户套餐购买记录")
    @GetMapping("/packages")
    public R<PageResult<AdminPackagePurchaseResponse>> listPackagePurchases(PageQueryRequest query) {
        return R.ok(adminBillingService.listPackagePurchases(query));
    }

    @Operation(summary = "保证金记录", description = "分页查询商户保证金记录")
    @GetMapping("/deposits")
    public R<PageResult<AdminDepositResponse>> listDeposits(PageQueryRequest query) {
        return R.ok(adminBillingService.listDeposits(query));
    }

    @Operation(summary = "月度服务费汇总", description = "分页查询月度服务费统计")
    @GetMapping("/service-fees")
    public R<PageResult<AdminServiceFeeSummaryResponse>> listServiceFeeSummaries(PageQueryRequest query) {
        return R.ok(adminBillingService.listServiceFeeSummaries(query));
    }

    // ========== 套餐管理 ==========

    @Operation(summary = "套餐管理列表", description = "分页查询所有套餐（用于管理）")
    @GetMapping("/packages/manage")
    public R<PageResult<PackageManageResponse>> listPackageManage(PageQueryRequest query) {
        return R.ok(adminBillingService.listPackageManage(query));
    }

    @Operation(summary = "创建套餐", description = "创建新的服务套餐")
    @PostMapping("/packages/manage")
    public R<Long> createPackage(@RequestBody @Valid PackageCreateRequest request) {
        Long packageId = adminBillingService.createPackage(request);
        return R.ok(packageId);
    }

    @Operation(summary = "更新套餐", description = "更新套餐信息")
    @PutMapping("/packages/manage/{packageId}")
    public R<Void> updatePackage(@PathVariable Long packageId, @RequestBody @Valid PackageUpdateRequest request) {
        adminBillingService.updatePackage(packageId, request);
        return R.ok();
    }

    @Operation(summary = "更新套餐状态", description = "启用或停用套餐")
    @PutMapping("/packages/manage/{packageId}/status")
    public R<Void> updatePackageStatus(@PathVariable Long packageId, @RequestBody @Valid PackageStatusRequest request) {
        adminBillingService.updatePackageStatus(packageId, request);
        return R.ok();
    }

    // ========== 入驻费套餐管理 ==========

    @Operation(summary = "入驻费套餐管理列表", description = "分页查询所有入驻费套餐（用于管理）")
    @GetMapping("/onboarding-plans/manage")
    public R<PageResult<OnboardingPlanManageResponse>> listOnboardingPlanManage(PageQueryRequest query) {
        return R.ok(adminBillingService.listOnboardingPlanManage(query));
    }

    @Operation(summary = "创建入驻费套餐", description = "创建新的入驻费套餐")
    @PostMapping("/onboarding-plans/manage")
    public R<Long> createOnboardingPlan(@RequestBody @Valid OnboardingPlanCreateRequest request) {
        Long planId = adminBillingService.createOnboardingPlan(request);
        return R.ok(planId);
    }

    @Operation(summary = "更新入驻费套餐", description = "更新入驻费套餐信息")
    @PutMapping("/onboarding-plans/manage/{planId}")
    public R<Void> updateOnboardingPlan(@PathVariable Long planId, @RequestBody @Valid OnboardingPlanUpdateRequest request) {
        adminBillingService.updateOnboardingPlan(planId, request);
        return R.ok();
    }

    @Operation(summary = "更新入驻费套餐状态", description = "启用或停用入驻费套餐")
    @PutMapping("/onboarding-plans/manage/{planId}/status")
    public R<Void> updateOnboardingPlanStatus(@PathVariable Long planId, @RequestBody @Valid OnboardingPlanStatusRequest request) {
        adminBillingService.updateOnboardingPlanStatus(planId, request);
        return R.ok();
    }

    // ========== 保证金退款审核 ==========

    @Operation(summary = "审核保证金退款", description = "通过或拒绝保证金退款申请")
    @PutMapping("/deposits/{depositId}/refund")
    public R<Void> handleDepositRefund(@PathVariable Long depositId, @RequestBody @Valid DepositRefundRequest request) {
        adminBillingService.handleDepositRefund(depositId, request);
        return R.ok();
    }

    // ========== 服务费手动调整 ==========

    @Operation(summary = "手动扣减服务费", description = "手动执行服务费扣减")
    @PostMapping("/service-fees/{summaryId}/deduct")
    public R<Void> manualDeductServiceFee(@PathVariable Long summaryId) {
        adminBillingService.manualDeductServiceFee(summaryId);
        return R.ok();
    }

    @Operation(summary = "调整服务费状态", description = "手动调整服务费扣减状态")
    @PutMapping("/service-fees/{summaryId}/status")
    public R<Void> adjustServiceFeeStatus(@PathVariable Long summaryId, @RequestBody @Valid ServiceFeeAdjustRequest request) {
        adminBillingService.adjustServiceFeeStatus(summaryId, request);
        return R.ok();
    }
}
