package com.wsh.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务费账单响应
 */
@Data
@Builder
@Schema(description = "服务费账单响应")
public class ServiceFeeBillResponse {

    @Schema(description = "服务费记录列表")
    private List<FeeItem> records;

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "每页大小")
    private Integer pageSize;

    @Data
    @Builder
    @Schema(description = "服务费记录项")
    public static class FeeItem {
        @Schema(description = "记录ID")
        private Long recordId;

        @Schema(description = "订单号")
        private String orderNo;

        @Schema(description = "券码ID")
        private Long voucherId;

        @Schema(description = "券码")
        private String voucherCode;

        @Schema(description = "订单金额")
        private BigDecimal orderAmount;

        @Schema(description = "服务费率")
        private BigDecimal serviceFeeRate;

        @Schema(description = "服务费率百分比")
        private String serviceFeeRatePercent;

        @Schema(description = "服务费金额")
        private BigDecimal serviceFee;

        @Schema(description = "商户到账金额")
        private BigDecimal merchantAmount;

        @Schema(description = "分账状态：0待分账 1已分账 2分账失败")
        private Integer sharingStatus;

        @Schema(description = "分账状态名称")
        private String sharingStatusName;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
    }
}
