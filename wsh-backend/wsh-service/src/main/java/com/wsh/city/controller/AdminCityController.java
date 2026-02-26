package com.wsh.city.controller;

import com.wsh.city.dto.AdminCityRequest;
import com.wsh.city.dto.AdminCityResponse;
import com.wsh.city.service.AdminCityService;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 平台后台城市管理接口
 */
@Tag(name = "城市管理", description = "平台后台城市管理")
@RestController
@RequestMapping("/v1/admin/cities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminCityController {

    private final AdminCityService adminCityService;

    @Operation(summary = "获取城市列表", description = "分页查询城市列表，支持关键词和状态筛选")
    @GetMapping
    public R<PageResult<AdminCityResponse>> getCityList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(adminCityService.getCityList(page, size, keyword, status));
    }

    @Operation(summary = "获取城市详情", description = "根据ID获取城市详细信息")
    @GetMapping("/{cityId}")
    public R<AdminCityResponse> getCityDetail(@PathVariable Long cityId) {
        return R.ok(adminCityService.getCityDetail(cityId));
    }

    @Operation(summary = "创建城市", description = "新增一个城市")
    @PostMapping
    public R<AdminCityResponse> createCity(@Valid @RequestBody AdminCityRequest request) {
        return R.ok(adminCityService.createCity(request));
    }

    @Operation(summary = "更新城市", description = "更新城市信息")
    @PutMapping("/{cityId}")
    public R<AdminCityResponse> updateCity(
            @PathVariable Long cityId,
            @Valid @RequestBody AdminCityRequest request) {
        return R.ok(adminCityService.updateCity(cityId, request));
    }

    @Operation(summary = "更新城市状态", description = "开通或关闭城市")
    @PutMapping("/{cityId}/status")
    public R<Void> updateCityStatus(
            @PathVariable Long cityId,
            @RequestParam Integer status) {
        adminCityService.updateCityStatus(cityId, status);
        return R.ok();
    }
}
