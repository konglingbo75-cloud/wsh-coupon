package com.wsh.admin.controller;

import com.wsh.admin.dto.DashboardStatsResponse;
import com.wsh.admin.service.AdminDashboardService;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "平台-仪表盘", description = "平台运营数据概览")
@RestController
@RequestMapping("/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "获取仪表盘统计数据", description = "获取商户、用户、订单、收入等核心统计数据")
    @GetMapping("/stats")
    public R<DashboardStatsResponse> getStats() {
        return R.ok(adminDashboardService.getStats());
    }
}
