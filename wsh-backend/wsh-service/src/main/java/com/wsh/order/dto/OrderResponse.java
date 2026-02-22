package com.wsh.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应
 */
@Data
@Builder
@Schema(description = "订单响应")
public class OrderResponse {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户Logo")
    private String merchantLogo;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动封面")
    private String activityCover;

    @Schema(description = "订单类型：1代金券 2储值 3积分兑换 4团购 5沉睡唤醒券")
    private Integer orderType;

    @Schema(description = "订单类型名称")
    private String orderTypeName;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "状态：0待支付 1已支付 2已关闭 3已退款")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "微信支付单号")
    private String transactionId;

    @Schema(description = "是否沉睡唤醒订单")
    private Boolean isDormancyAwake;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "券码信息（已支付时返回）")
    private VoucherInfo voucherInfo;

    @Data
    @Builder
    @Schema(description = "券码信息")
    public static class VoucherInfo {
        @Schema(description = "券码ID")
        private Long voucherId;

        @Schema(description = "券码")
        private String voucherCode;

        @Schema(description = "券码类型")
        private Integer voucherType;

        @Schema(description = "券码价值")
        private BigDecimal voucherValue;

        @Schema(description = "状态：0未使用 1已使用 2已过期")
        private Integer status;

        @Schema(description = "有效开始时间")
        private LocalDateTime validStartTime;

        @Schema(description = "有效结束时间")
        private LocalDateTime validEndTime;
    }
}
