package com.wsh.merchant.controller;

import com.wsh.common.core.result.R;
import com.wsh.merchant.dto.ActivityCreateRequest;
import com.wsh.merchant.dto.MerchantActivityDetailResponse;
import com.wsh.merchant.dto.MerchantActivityListResponse;
import com.wsh.merchant.service.MerchantActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商户活动管理接口
 */
@Tag(name = "商户活动管理", description = "商户创建、编辑、发布活动")
@RestController
@RequestMapping("/v1/merchant/activities")
@RequiredArgsConstructor
public class MerchantActivityController {

    private final MerchantActivityService merchantActivityService;

    @Operation(summary = "查询活动列表", description = "查询当前商户的活动列表，支持按状态和关键字筛选")
    @GetMapping
    public R<MerchantActivityListResponse> listActivities(
            @Parameter(description = "活动状态：0草稿 1进行中 2暂停 3结束") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(merchantActivityService.listActivities(status, keyword, page, pageSize));
    }

    @Operation(summary = "创建活动", description = "创建一个新活动，可保存为草稿或直接发布")
    @PostMapping
    public R<MerchantActivityDetailResponse> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        return R.ok(merchantActivityService.createActivity(request));
    }

    @Operation(summary = "获取活动详情", description = "获取活动详情用于编辑")
    @GetMapping("/{activityId}")
    public R<MerchantActivityDetailResponse> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        return R.ok(merchantActivityService.getActivityDetail(activityId));
    }

    @Operation(summary = "更新活动", description = "更新已有活动信息")
    @PostMapping("/{activityId}")
    public R<MerchantActivityDetailResponse> updateActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Valid @RequestBody ActivityCreateRequest request) {
        return R.ok(merchantActivityService.updateActivity(activityId, request));
    }

    @Operation(summary = "发布活动", description = "将草稿状态的活动发布上线")
    @PostMapping("/{activityId}/publish")
    public R<Void> publishActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        merchantActivityService.publishActivity(activityId);
        return R.ok();
    }

    @Operation(summary = "暂停活动", description = "暂停进行中的活动")
    @PostMapping("/{activityId}/pause")
    public R<Void> pauseActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        merchantActivityService.pauseActivity(activityId);
        return R.ok();
    }

    @Operation(summary = "恢复活动", description = "恢复已暂停的活动")
    @PostMapping("/{activityId}/resume")
    public R<Void> resumeActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        merchantActivityService.resumeActivity(activityId);
        return R.ok();
    }
}
