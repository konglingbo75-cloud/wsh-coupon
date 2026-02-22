package com.wsh.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 我的券包响应
 */
@Data
@Builder
@Schema(description = "我的券包响应")
public class VoucherListResponse {

    @Schema(description = "券码列表")
    private List<VoucherItem> vouchers;

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "可用数量")
    private Integer availableCount;

    @Schema(description = "已使用数量")
    private Integer usedCount;

    @Schema(description = "已过期数量")
    private Integer expiredCount;

    @Data
    @Builder
    @Schema(description = "券码项")
    public static class VoucherItem {
        @Schema(description = "券码ID")
        private Long voucherId;

        @Schema(description = "券码")
        private String voucherCode;

        @Schema(description = "订单ID")
        private Long orderId;

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

        @Schema(description = "券码类型：1代金券 2兑换券 3储值码 4沉睡唤醒券")
        private Integer voucherType;

        @Schema(description = "券码类型名称")
        private String voucherTypeName;

        @Schema(description = "券码价值")
        private BigDecimal voucherValue;

        @Schema(description = "状态：0未使用 1已使用 2已过期 3已退款")
        private Integer status;

        @Schema(description = "状态名称")
        private String statusName;

        @Schema(description = "有效开始时间")
        private LocalDateTime validStartTime;

        @Schema(description = "有效结束时间")
        private LocalDateTime validEndTime;

        @Schema(description = "使用时间")
        private LocalDateTime usedTime;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;

        @Schema(description = "是否即将过期（7天内）")
        private Boolean expiringSoon;

        @Schema(description = "剩余天数")
        private Integer remainingDays;
    }
}
