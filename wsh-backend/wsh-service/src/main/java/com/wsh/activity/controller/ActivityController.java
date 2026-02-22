package com.wsh.activity.controller;

import com.wsh.activity.dto.*;
import com.wsh.activity.service.ActivityDisplayService;
import com.wsh.activity.service.PublicActivityService;
import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 活动接口
 */
@Tag(name = "03-活动中心", description = "活动查询与展示相关接口")
@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityDisplayService activityDisplayService;
    private final PublicActivityService publicActivityService;

    // ========== 需要登录的接口 ==========

    @Operation(summary = "活动列表", description = "查询活动列表，支持多种筛选条件")
    @GetMapping("/v1/activities")
    public R<ActivityListResponse> getActivities(
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "活动类型：1代金券 2储值 3积分兑换 4团购") @RequestParam(required = false) Integer activityType,
            @Parameter(description = "城市") @RequestParam(required = false) String city,
            @Parameter(description = "是否只显示专属活动") @RequestParam(required = false) Boolean exclusiveOnly,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Long userId = SecurityUtil.getUserId();
        
        ActivityQueryRequest request = new ActivityQueryRequest();
        request.setMerchantId(merchantId);
        request.setActivityType(activityType);
        request.setCity(city);
        request.setExclusiveOnly(exclusiveOnly);
        request.setPage(page);
        request.setPageSize(pageSize);
        
        return R.ok(activityDisplayService.queryActivities(request, userId));
    }

    @Operation(summary = "活动详情", description = "获取活动详细信息")
    @GetMapping("/v1/activities/{activityId}")
    public R<ActivityDetailResponse> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        
        Long userId = SecurityUtil.getUserId();
        ActivityDetailResponse response = activityDisplayService.getActivityDetail(activityId, userId);
        if (response == null) {
            return R.fail("活动不存在");
        }
        return R.ok(response);
    }

    @Operation(summary = "附近活动", description = "基于位置推荐附近商户的活动")
    @GetMapping("/v1/activities/nearby")
    public R<NearbyActivityResponse> getNearbyActivities(
            @Parameter(description = "经度", required = true) @RequestParam BigDecimal longitude,
            @Parameter(description = "纬度", required = true) @RequestParam BigDecimal latitude,
            @Parameter(description = "搜索范围（公里），默认5公里") @RequestParam(defaultValue = "5") Integer distanceKm) {
        
        Long userId = SecurityUtil.getUserId();
        return R.ok(activityDisplayService.getNearbyActivities(longitude, latitude, distanceKm, userId));
    }

    @Operation(summary = "我的专属活动", description = "根据用户会员状态筛选展示对应的专属活动")
    @GetMapping("/v1/activities/exclusive")
    public R<ExclusiveActivityResponse> getExclusiveActivities() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(activityDisplayService.getExclusiveActivities(userId));
    }

    // ========== 公开接口（无需登录） ==========

    @Operation(summary = "同城公开活动（无需登录）", description = "获取同城公开活动广场，按活动类型分组")
    @GetMapping("/v1/public/activities")
    public R<PublicActivityResponse> getPublicActivities(
            @Parameter(description = "城市", required = true) @RequestParam String city) {
        return R.ok(publicActivityService.getPublicActivitiesByCity(city));
    }

    @Operation(summary = "公开活动详情（无需登录）", description = "获取公开活动的详细信息")
    @GetMapping("/v1/public/activities/{activityId}")
    public R<ActivityDetailResponse> getPublicActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        ActivityDetailResponse response = publicActivityService.getPublicActivityDetail(activityId);
        if (response == null) {
            return R.fail("活动不存在或非公开活动");
        }
        return R.ok(response);
    }

    @Operation(summary = "按类型获取公开活动（无需登录）", description = "按活动类型获取同城公开活动列表")
    @GetMapping("/v1/public/activities/type")
    public R<List<ActivityDetailResponse>> getPublicActivitiesByType(
            @Parameter(description = "城市", required = true) @RequestParam String city,
            @Parameter(description = "活动类型：1代金券 2储值 3积分兑换 4团购") @RequestParam(required = false) Integer activityType) {
        return R.ok(publicActivityService.getPublicActivitiesByType(city, activityType));
    }

    @Operation(summary = "同城入驻商户（无需登录）", description = "获取同城所有入驻商户列表")
    @GetMapping("/v1/public/merchants")
    public R<List<PublicActivityService.MerchantPublicInfo>> getPublicMerchants(
            @Parameter(description = "城市", required = true) @RequestParam String city) {
        return R.ok(publicActivityService.getPublicMerchants(city));
    }
}
