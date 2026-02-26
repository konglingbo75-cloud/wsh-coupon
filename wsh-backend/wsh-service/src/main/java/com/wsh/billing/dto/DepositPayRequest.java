package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "保证金支付请求")
public class DepositPayRequest {

    @NotNull(message = "保证金金额不能为空")
    @DecimalMin(value = "0", message = "保证金金额不能为负数")
    @Schema(description = "保证金金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}
