package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "保证金退款审核请求")
public class DepositRefundRequest {

    @NotNull(message = "审核动作不能为空")
    @Schema(description = "审核动作：approve通过 reject拒绝", requiredMode = Schema.RequiredMode.REQUIRED, example = "approve")
    private String action;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String reason;
}
