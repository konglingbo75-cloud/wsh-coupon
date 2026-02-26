package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "商户余额信息")
public class MerchantBalanceResponse {

    @Schema(description = "当前余额")
    private BigDecimal balance;

    @Schema(description = "累计充值")
    private BigDecimal totalRecharge;

    @Schema(description = "累计消费")
    private BigDecimal totalConsume;

    @Schema(description = "近期交易记录")
    private List<BalanceLogItem> recentLogs;

    @Data
    @Builder
    @Schema(description = "余额变动记录")
    public static class BalanceLogItem {

        @Schema(description = "记录ID")
        private Long logId;

        @Schema(description = "变动类型：1充值 2扣减 3退款")
        private Integer changeType;

        @Schema(description = "变动金额")
        private BigDecimal amount;

        @Schema(description = "变动前余额")
        private BigDecimal balanceBefore;

        @Schema(description = "变动后余额")
        private BigDecimal balanceAfter;

        @Schema(description = "关联订单号")
        private String relatedOrderNo;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "创建时间")
        private String createdAt;
    }
}
