package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "入驻费套餐信息响应")
public class OnboardingFeePlanResponse {

    @Schema(description = "套餐ID")
    private Long planId;

    @Schema(description = "套餐名称", example = "单店年费")
    private String planName;

    @Schema(description = "套餐类型：1按门店 2按品牌")
    private Integer planType;

    @Schema(description = "费用金额（元）", example = "2000.00")
    private BigDecimal feeAmount;

    @Schema(description = "有效期（月）", example = "12")
    private Integer durationMonths;

    @Schema(description = "套餐说明")
    private String description;
}
