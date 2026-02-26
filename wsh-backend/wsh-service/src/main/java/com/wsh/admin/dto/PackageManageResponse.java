package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "套餐管理列表项")
public class PackageManageResponse {

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

    @Schema(description = "服务费率（0-1）")
    private BigDecimal serviceFeeRate;

    @Schema(description = "套餐特性说明（JSON）")
    private String features;

    @Schema(description = "状态：0停用 1启用")
    private Integer status;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
