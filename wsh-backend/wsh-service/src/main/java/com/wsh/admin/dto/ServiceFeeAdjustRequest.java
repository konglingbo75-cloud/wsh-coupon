package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "服务费状态调整请求")
public class ServiceFeeAdjustRequest {

    @NotNull(message = "新状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 2, message = "状态值无效")
    @Schema(description = "新状态：0待扣减 1已扣减 2余额不足", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer newStatus;

    @NotBlank(message = "调整原因不能为空")
    @Schema(description = "调整原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
