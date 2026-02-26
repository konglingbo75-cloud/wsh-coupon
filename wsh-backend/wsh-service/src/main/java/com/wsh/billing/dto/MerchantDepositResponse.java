package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "商户保证金信息")
public class MerchantDepositResponse {

    @Schema(description = "保证金记录ID")
    private Long depositId;

    @Schema(description = "保证金金额")
    private BigDecimal depositAmount;

    @Schema(description = "支付状态：0待支付 1已支付 2已关闭")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
