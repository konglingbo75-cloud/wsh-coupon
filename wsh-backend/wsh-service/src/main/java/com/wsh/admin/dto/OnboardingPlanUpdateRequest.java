package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "入驻费套餐更新请求")
public class OnboardingPlanUpdateRequest {

    @Size(max = 64, message = "套餐名称最多64个字符")
    @Schema(description = "套餐名称")
    private String planName;

    @Min(value = 1, message = "套餐类型值无效")
    @Max(value = 2, message = "套餐类型值无效")
    @Schema(description = "套餐类型：1按门店 2按品牌")
    private Integer planType;

    @DecimalMin(value = "0.01", message = "费用金额最低0.01元")
    @Schema(description = "费用金额")
    private BigDecimal feeAmount;

    @Min(value = 1, message = "有效期至少1个月")
    @Schema(description = "有效期（月）")
    private Integer durationMonths;

    @Size(max = 500, message = "描述最多500个字符")
    @Schema(description = "描述")
    private String description;
}
