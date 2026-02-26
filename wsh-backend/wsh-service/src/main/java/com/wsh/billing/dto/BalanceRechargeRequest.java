package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "余额充值请求")
public class BalanceRechargeRequest {

    @Schema(description = "充值金额", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "1.00", message = "最低充值金额1元")
    private BigDecimal amount;
}
