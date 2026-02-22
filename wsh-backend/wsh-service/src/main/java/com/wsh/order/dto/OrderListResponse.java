package com.wsh.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 订单列表响应
 */
@Data
@Builder
@Schema(description = "订单列表响应")
public class OrderListResponse {

    @Schema(description = "订单列表")
    private List<OrderResponse> orders;

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "每页大小")
    private Integer pageSize;
}
