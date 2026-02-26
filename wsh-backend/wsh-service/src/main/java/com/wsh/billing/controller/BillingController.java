package com.wsh.billing.controller;

import com.wsh.billing.dto.*;
import com.wsh.billing.service.MerchantBalanceService;
import com.wsh.billing.service.MerchantDepositService;
import com.wsh.billing.service.OnboardingFeeService;
import com.wsh.billing.service.ServicePackageService;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.domain.entity.MerchantEmployee;
import com.wsh.domain.mapper.MerchantEmployeeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商户付费管理", description = "套餐购买、保证金缴纳、余额查询、服务费账单")
@RestController
@RequestMapping("/v1/merchant/billing")
@RequiredArgsConstructor
public class BillingController {

    private final OnboardingFeeService onboardingFeeService;
    private final ServicePackageService servicePackageService;
    private final MerchantDepositService merchantDepositService;
    private final MerchantBalanceService merchantBalanceService;
    private final MerchantEmployeeMapper merchantEmployeeMapper;

    // ========== 入驻费（保留原有接口） ==========

    @Operation(summary = "查询入驻费套餐列表")
    @GetMapping("/plans")
    public R<List<OnboardingFeePlanResponse>> listPlans() {
        return R.ok(onboardingFeeService.listPlans());
    }

    @Operation(summary = "发起入驻费支付")
    @PostMapping("/pay")
    public R<OnboardingFeePayResponse> createPayOrder(@Valid @RequestBody OnboardingFeePayRequest request) {
        return R.ok(onboardingFeeService.createPayOrder(request));
    }

    @Operation(summary = "查询商户最新入驻费记录")
    @GetMapping("/{merchantId}/latest")
    public R<OnboardingFeeResponse> getLatestFee(
            @Parameter(description = "商户ID") @PathVariable Long merchantId) {
        return R.ok(onboardingFeeService.getLatestFee(merchantId));
    }

    // ========== 服务套餐 ==========

    @Operation(summary = "查询服务套餐列表", description = "获取所有可购买的服务套餐")
    @GetMapping("/packages")
    public R<List<ServicePackageResponse>> listPackages() {
        return R.ok(servicePackageService.listPackages());
    }

    @Operation(summary = "查询我的当前套餐", description = "获取当前商户的有效套餐信息")
    @GetMapping("/packages/current")
    public R<MerchantPackageInfoResponse> getCurrentPackage() {
        Long merchantId = resolveCurrentMerchantId();
        return R.ok(servicePackageService.getCurrentPackage(merchantId));
    }

    @Operation(summary = "购买服务套餐", description = "选择套餐并发起购买")
    @PostMapping("/packages/purchase")
    public R<MerchantPackageInfoResponse> purchasePackage(@Valid @RequestBody PackagePurchaseRequest request) {
        Long merchantId = resolveCurrentMerchantId();
        return R.ok(servicePackageService.purchasePackage(merchantId, request));
    }

    // ========== 保证金 ==========

    @Operation(summary = "查询保证金信息", description = "获取当前商户的保证金缴纳状态")
    @GetMapping("/deposit")
    public R<MerchantDepositResponse> getDeposit() {
        Long merchantId = resolveCurrentMerchantId();
        return R.ok(merchantDepositService.getDeposit(merchantId));
    }

    @Operation(summary = "缴纳保证金", description = "发起保证金缴纳，金额可为零")
    @PostMapping("/deposit/pay")
    public R<MerchantDepositResponse> payDeposit(@Valid @RequestBody DepositPayRequest request) {
        Long merchantId = resolveCurrentMerchantId();
        return R.ok(merchantDepositService.payDeposit(merchantId, request));
    }

    // ========== 余额管理（预付费模式） ==========

    @Operation(summary = "查询余额信息", description = "获取当前商户余额及近期流水")
    @GetMapping("/balance")
    public R<MerchantBalanceResponse> getBalance() {
        Long merchantId = resolveCurrentMerchantId();
        return R.ok(merchantBalanceService.getBalance(merchantId));
    }

    @Operation(summary = "余额充值", description = "商户余额充值（本地开发模式直接成功）")
    @PostMapping("/balance/recharge")
    public R<MerchantBalanceResponse> rechargeBalance(@Valid @RequestBody BalanceRechargeRequest request) {
        Long merchantId = resolveCurrentMerchantId();
        // 本地开发环境：直接增加余额（生产环境需对接微信支付）
        String mockTransactionId = "LOCAL_" + System.currentTimeMillis();
        return R.ok(merchantBalanceService.recharge(merchantId, request.getAmount(), mockTransactionId));
    }

    // ========== 月度服务费 ==========

    @Operation(summary = "查询月度服务费汇总", description = "获取各月服务费统计及扣减状态")
    @GetMapping("/service-fees")
    public R<List<MonthlyServiceFeeResponse>> listMonthlyFees() {
        Long merchantId = resolveCurrentMerchantId();
        return R.ok(merchantBalanceService.listMonthlyFees(merchantId));
    }

    // ==================== 私有方法 ====================

    private Long resolveCurrentMerchantId() {
        Long merchantId = SecurityUtil.getMerchantId();
        if (merchantId != null) {
            return merchantId;
        }
        String openid = SecurityUtil.getOpenid();
        if (openid == null) {
            throw new BusinessException(401, "请先登录");
        }
        MerchantEmployee employee = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getOpenid, openid)
                        .eq(MerchantEmployee::getStatus, 1)
                        .last("LIMIT 1"));
        if (employee == null) {
            throw new BusinessException(403, "您还未入驻商户");
        }
        return employee.getMerchantId();
    }
}
