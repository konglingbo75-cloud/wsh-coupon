package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "服务套餐信息")
public class ServicePackageResponse {

    @Schema(description = "套餐ID")
    private Long packageId;

    @Schema(description = "套餐名称")
    private String packageName;

    @Schema(description = "套餐类型：1基础版 2专业版 3旗舰版")
    private Integer packageType;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "有效期（月）")
    private Integer durationMonths;

    @Schema(description = "服务费率")
    private BigDecimal serviceFeeRate;

    @Schema(description = "套餐功能描述")
    private String features;
}
