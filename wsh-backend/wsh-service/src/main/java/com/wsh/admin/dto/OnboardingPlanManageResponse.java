package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "入驻费套餐管理列表项")
public class OnboardingPlanManageResponse {

    @Schema(description = "套餐ID")
    private Long planId;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "套餐类型：1按门店 2按品牌")
    private Integer planType;

    @Schema(description = "费用金额")
    private BigDecimal feeAmount;

    @Schema(description = "有效期（月）")
    private Integer durationMonths;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0停用 1启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
