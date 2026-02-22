package com.wsh.admin.controller;

import com.wsh.admin.dto.AdminBillingListResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.admin.service.AdminBillingService;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "平台-费用管理", description = "商户入驻费查询")
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
}
