package com.wsh.order.controller;

import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.order.dto.*;
import com.wsh.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口
 */
@Tag(name = "04-订单中心", description = "订单创建、支付、查询相关接口")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单", description = "根据活动创建订单")
    @PostMapping("/orders")
    public R<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(orderService.createOrder(request, userId));
    }

    @Operation(summary = "发起支付", description = "获取微信支付参数")
    @PostMapping("/orders/{orderId}/pay")
    public R<PaymentResponse> requestPayment(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtil.getUserId();
        String openid = SecurityUtil.getOpenid();
        return R.ok(orderService.requestPayment(orderId, userId, openid));
    }

    @Operation(summary = "我的订单列表", description = "查询当前用户的订单列表")
    @GetMapping("/orders")
    public R<OrderListResponse> getOrders(
            @Parameter(description = "状态筛选：0待支付 1已支付 2已关闭 3已退款") 
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(orderService.getUserOrders(userId, status, page, pageSize));
    }

    @Operation(summary = "订单详情", description = "查询订单详细信息")
    @GetMapping("/orders/{orderId}")
    public R<OrderResponse> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(orderService.getOrderDetail(orderId, userId));
    }

    @Operation(summary = "取消订单", description = "取消待支付的订单")
    @PostMapping("/orders/{orderId}/cancel")
    public R<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtil.getUserId();
        orderService.cancelOrder(orderId, userId);
        return R.ok();
    }

    @Operation(summary = "我的券包", description = "查询当前用户的券码列表")
    @GetMapping("/vouchers")
    public R<VoucherListResponse> getVouchers(
            @Parameter(description = "状态筛选：0未使用 1已使用 2已过期") 
            @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(orderService.getUserVouchers(userId, status));
    }

    @Operation(summary = "券码详情", description = "查询券码详细信息（含二维码数据）")
    @GetMapping("/vouchers/{voucherId}")
    public R<VoucherListResponse.VoucherItem> getVoucherDetail(
            @Parameter(description = "券码ID") @PathVariable Long voucherId) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(orderService.getVoucherDetail(voucherId, userId));
    }
}
