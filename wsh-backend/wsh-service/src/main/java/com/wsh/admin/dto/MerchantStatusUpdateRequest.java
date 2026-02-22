package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商户状态更新请求")
public class MerchantStatusUpdateRequest {

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    @NotBlank(message = "操作动作不能为空")
    @Schema(description = "操作：FREEZE/UNFREEZE", requiredMode = Schema.RequiredMode.REQUIRED, example = "FREEZE")
    private String action;

    @Schema(description = "操作原因")
    private String reason;
}
