package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "套餐状态更新请求")
public class PackageStatusRequest {

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    @Schema(description = "状态：0停用 1启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
