package com.wsh.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单请求
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID", required = true)
    private Long activityId;

    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须大于0")
    @Schema(description = "购买数量", required = true, example = "1")
    private Integer quantity;

    @Schema(description = "优惠码（如有）")
    private String couponCode;

    @Schema(description = "备注")
    private String remark;
}
