package com.wsh.billing.controller;

import com.wsh.billing.dto.OnboardingFeePayRequest;
import com.wsh.billing.dto.OnboardingFeePayResponse;
import com.wsh.billing.dto.OnboardingFeePlanResponse;
import com.wsh.billing.dto.OnboardingFeeResponse;
import com.wsh.billing.service.OnboardingFeeService;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "入驻费管理", description = "入驻费套餐查询、支付、记录查看")
@RestController
@RequestMapping("/v1/merchant/billing")
@RequiredArgsConstructor
public class BillingController {

    private final OnboardingFeeService onboardingFeeService;

    @Operation(summary = "查询入驻费套餐列表",
            description = "获取所有启用的入驻费套餐选项")
    @GetMapping("/plans")
    public R<List<OnboardingFeePlanResponse>> listPlans() {
        return R.ok(onboardingFeeService.listPlans());
    }

    @Operation(summary = "发起入驻费支付",
            description = "选择套餐后发起微信支付，返回小程序调起支付所需参数")
    @PostMapping("/pay")
    public R<OnboardingFeePayResponse> createPayOrder(@Valid @RequestBody OnboardingFeePayRequest request) {
        return R.ok(onboardingFeeService.createPayOrder(request));
    }

    @Operation(summary = "查询商户最新入驻费记录",
            description = "查询指定商户最新的入驻费缴纳记录，包含有效期和剩余天数")
    @GetMapping("/{merchantId}/latest")
    public R<OnboardingFeeResponse> getLatestFee(
            @Parameter(description = "商户ID") @PathVariable Long merchantId) {
        return R.ok(onboardingFeeService.getLatestFee(merchantId));
    }
}
