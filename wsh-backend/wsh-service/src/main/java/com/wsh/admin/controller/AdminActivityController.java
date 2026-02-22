package com.wsh.admin.controller;

import com.wsh.admin.dto.AdminActivityListResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.admin.service.AdminActivityService;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "平台-活动管理", description = "平台活动查询")
@RestController
@RequestMapping("/v1/admin/activities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    @Operation(summary = "活动列表", description = "分页查询所有商户活动，支持关键词搜索和状态筛选")
    @GetMapping
    public R<PageResult<AdminActivityListResponse>> listActivities(PageQueryRequest query) {
        return R.ok(adminActivityService.listActivities(query));
    }
}
