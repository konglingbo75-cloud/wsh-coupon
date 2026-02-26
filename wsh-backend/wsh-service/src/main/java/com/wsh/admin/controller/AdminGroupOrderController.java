package com.wsh.admin.controller;

import com.wsh.admin.service.AdminGroupOrderService;
import com.wsh.admin.service.AdminGroupOrderService.GroupOrderDetail;
import com.wsh.admin.service.AdminGroupOrderService.GroupOrderItem;
import com.wsh.common.core.result.R;
import com.wsh.common.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员拼团控制器
 */
@RestController
@RequestMapping("/v1/admin/group-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminGroupOrderController {

    private final AdminGroupOrderService adminGroupOrderService;

    /**
     * 查询拼团列表
     */
    @GetMapping
    public R<PageResult<GroupOrderItem>> getGroupOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long activityId) {
        PageResult<GroupOrderItem> result = adminGroupOrderService.getGroupOrders(page, size, keyword, status, activityId);
        return R.ok(result);
    }

    /**
     * 查询拼团详情
     */
    @GetMapping("/{groupOrderId}")
    public R<GroupOrderDetail> getGroupOrderDetail(@PathVariable Long groupOrderId) {
        GroupOrderDetail detail = adminGroupOrderService.getGroupOrderDetail(groupOrderId);
        if (detail == null) {
            return R.fail("拼团不存在");
        }
        return R.ok(detail);
    }
}
