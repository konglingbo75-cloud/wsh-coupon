package com.wsh.verification.controller;

import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.verification.dto.*;
import com.wsh.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 核销接口
 */
@Tag(name = "05-核销中心", description = "券码核销、记录查询、服务费账单相关接口")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @Operation(summary = "扫码核销", description = "商家扫描用户券码进行核销")
    @PostMapping("/verification/verify")
    public R<VerifyResponse> verify(@Valid @RequestBody VerifyRequest request) {
        Long merchantId = SecurityUtil.getMerchantId();
        Long employeeId = SecurityUtil.getEmployeeId();
        
        if (merchantId == null) {
            return R.fail("当前用户不是商户员工");
        }
        
        return R.ok(verificationService.verify(request, merchantId, employeeId));
    }

    @Operation(summary = "商户核销记录", description = "查询商户的核销记录列表")
    @GetMapping("/verification/records")
    public R<VerificationRecordListResponse> getMerchantRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long merchantId = SecurityUtil.getMerchantId();
        
        if (merchantId == null) {
            return R.fail("当前用户不是商户员工");
        }
        
        return R.ok(verificationService.getMerchantRecords(merchantId, page, pageSize));
    }

    @Operation(summary = "员工核销记录", description = "查询当前员工的核销记录")
    @GetMapping("/verification/my-records")
    public R<VerificationRecordListResponse> getMyRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long employeeId = SecurityUtil.getEmployeeId();
        
        if (employeeId == null) {
            return R.fail("当前用户不是商户员工");
        }
        
        return R.ok(verificationService.getEmployeeRecords(employeeId, page, pageSize));
    }

    @Operation(summary = "服务费账单", description = "查询商户服务费扣除明细")
    @GetMapping("/merchant/billing/service-fees/detail")
    public R<ServiceFeeBillResponse> getServiceFeeBill(
            @Parameter(description = "年份（可选）") @RequestParam(required = false) Integer year,
            @Parameter(description = "月份（可选）") @RequestParam(required = false) Integer month,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long merchantId = SecurityUtil.getMerchantId();
        
        if (merchantId == null) {
            return R.fail("当前用户不是商户管理员");
        }
        
        return R.ok(verificationService.getServiceFeeBill(merchantId, year, month, page, pageSize));
    }

    @Operation(summary = "服务费汇总", description = "查询商户服务费汇总（总计+本月）")
    @GetMapping("/merchant/billing/service-fees/summary")
    public R<ServiceFeeSummaryResponse> getServiceFeeSummary() {
        Long merchantId = SecurityUtil.getMerchantId();
        
        if (merchantId == null) {
            return R.fail("当前用户不是商户管理员");
        }
        
        return R.ok(verificationService.getServiceFeeSummary(merchantId));
    }
}
