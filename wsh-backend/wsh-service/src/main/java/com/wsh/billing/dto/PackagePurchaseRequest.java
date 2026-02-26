package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "套餐购买请求")
public class PackagePurchaseRequest {

    @NotNull(message = "套餐ID不能为空")
    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long packageId;
}
