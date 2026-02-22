package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商户审核请求")
public class MerchantAuditRequest {

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作：APPROVE/REJECT", requiredMode = Schema.RequiredMode.REQUIRED, example = "APPROVE")
    private String action;

    @Schema(description = "审核原因/备注")
    private String reason;
}
