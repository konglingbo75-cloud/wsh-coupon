package com.wsh.admin.controller;

import com.wsh.admin.dto.*;
import com.wsh.admin.service.AdminMerchantService;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "平台-商户管理", description = "商户列表、详情、审核、冻结/解冻")
@RestController
@RequestMapping("/v1/admin/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @Operation(summary = "商户列表", description = "分页查询所有商户，支持关键词搜索和状态筛选")
    @GetMapping
    public R<PageResult<AdminMerchantListResponse>> listMerchants(PageQueryRequest query) {
        return R.ok(adminMerchantService.listMerchants(query));
    }

    @Operation(summary = "商户详情", description = "查询商户完整信息，包括门店、入驻费、审核日志")
    @GetMapping("/{merchantId}")
    public R<AdminMerchantDetailResponse> getMerchantDetail(
            @Parameter(description = "商户ID") @PathVariable Long merchantId) {
        return R.ok(adminMerchantService.getMerchantDetail(merchantId));
    }

    @Operation(summary = "审核商户", description = "对待审核商户执行审核（通过/拒绝）")
    @PostMapping("/audit")
    public R<Void> auditMerchant(@Valid @RequestBody MerchantAuditRequest request) {
        adminMerchantService.auditMerchant(request);
        return R.ok();
    }

    @Operation(summary = "冻结/解冻商户", description = "冻结或解冻商户")
    @PostMapping("/status")
    public R<Void> updateMerchantStatus(@Valid @RequestBody MerchantStatusUpdateRequest request) {
        adminMerchantService.updateMerchantStatus(request);
        return R.ok();
    }
}
