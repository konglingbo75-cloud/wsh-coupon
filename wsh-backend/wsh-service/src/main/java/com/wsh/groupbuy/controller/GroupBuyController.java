package com.wsh.groupbuy.controller;

import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.groupbuy.dto.*;
import com.wsh.groupbuy.service.GroupBuyService;
import com.wsh.order.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团控制器
 */
@RestController
@RequestMapping("/v1/group-buy")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

    /**
     * 发起拼团
     */
    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    public R<GroupDetailResponse> initiateGroup(@Validated @RequestBody InitiateGroupRequest request) {
        Long userId = SecurityUtil.getUserId();
        GroupDetailResponse response = groupBuyService.initiateGroup(request.getActivityId(), userId);
        return R.ok(response);
    }

    /**
     * 参与拼团
     */
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public R<GroupDetailResponse> joinGroup(@Validated @RequestBody JoinGroupRequest request) {
        Long userId = SecurityUtil.getUserId();
        GroupDetailResponse response = groupBuyService.joinGroup(request.getGroupOrderId(), userId);
        return R.ok(response);
    }

    /**
     * 获取拼团详情
     */
    @GetMapping("/{groupOrderId}")
    @PreAuthorize("isAuthenticated()")
    public R<GroupDetailResponse> getGroupDetail(@PathVariable Long groupOrderId) {
        Long userId = SecurityUtil.getUserId();
        GroupDetailResponse response = groupBuyService.getGroupDetail(groupOrderId, userId);
        return R.ok(response);
    }

    /**
     * 获取活动的可参与拼团列表
     */
    @GetMapping("/activity/{activityId}")
    @PreAuthorize("isAuthenticated()")
    public R<List<GroupDetailResponse>> getActivityGroups(@PathVariable Long activityId) {
        Long userId = SecurityUtil.getUserId();
        List<GroupDetailResponse> response = groupBuyService.getActivityGroups(activityId, userId);
        return R.ok(response);
    }

    /**
     * 获取我的拼团列表
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public R<GroupListResponse> getMyGroups(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getUserId();
        GroupListResponse response = groupBuyService.getUserGroups(userId, status, page, pageSize);
        return R.ok(response);
    }

    /**
     * 发起拼团支付
     */
    @PostMapping("/{groupOrderId}/pay")
    @PreAuthorize("isAuthenticated()")
    public R<PaymentResponse> requestPayment(@PathVariable Long groupOrderId) {
        Long userId = SecurityUtil.getUserId();
        String openid = SecurityUtil.getOpenid();
        PaymentResponse response = groupBuyService.requestGroupPayment(groupOrderId, userId, openid);
        return R.ok(response);
    }

    /**
     * 取消拼团
     */
    @PostMapping("/{groupOrderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public R<Void> cancelGroup(@PathVariable Long groupOrderId) {
        Long userId = SecurityUtil.getUserId();
        groupBuyService.cancelGroup(groupOrderId, userId);
        return R.ok();
    }
}
