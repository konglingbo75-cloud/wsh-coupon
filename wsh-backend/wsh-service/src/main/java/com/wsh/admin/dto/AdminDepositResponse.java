package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "保证金记录列表项")
public class AdminDepositResponse {

    @Schema(description = "保证金ID")
    private Long depositId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "保证金金额")
    private BigDecimal depositAmount;

    @Schema(description = "支付状态")
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
