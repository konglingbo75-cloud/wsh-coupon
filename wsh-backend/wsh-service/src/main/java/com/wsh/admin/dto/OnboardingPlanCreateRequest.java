package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "入驻费套餐创建请求")
public class OnboardingPlanCreateRequest {

    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 64, message = "套餐名称最多64个字符")
    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String planName;

    @NotNull(message = "套餐类型不能为空")
    @Min(value = 1, message = "套餐类型值无效")
    @Max(value = 2, message = "套餐类型值无效")
    @Schema(description = "套餐类型：1按门店 2按品牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer planType;

    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0.01", message = "费用金额最低0.01元")
    @Schema(description = "费用金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal feeAmount;

    @NotNull(message = "有效期不能为空")
    @Min(value = 1, message = "有效期至少1个月")
    @Schema(description = "有效期（月）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMonths;

    @Size(max = 500, message = "描述最多500个字符")
    @Schema(description = "描述")
    private String description;
}
