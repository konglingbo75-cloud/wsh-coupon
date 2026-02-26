package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "套餐创建请求")
public class PackageCreateRequest {

    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 64, message = "套餐名称最多64个字符")
    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageName;

    @NotNull(message = "套餐类型不能为空")
    @Min(value = 1, message = "套餐类型值无效")
    @Max(value = 3, message = "套餐类型值无效")
    @Schema(description = "套餐类型：1基础版 2专业版 3旗舰版", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer packageType;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格最低0.01元")
    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "有效期不能为空")
    @Min(value = 1, message = "有效期至少1个月")
    @Schema(description = "有效期（月）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMonths;

    @NotNull(message = "服务费率不能为空")
    @DecimalMin(value = "0", message = "服务费率不能小于0")
    @DecimalMax(value = "1", message = "服务费率不能大于1")
    @Schema(description = "服务费率（0-1，例如0.05表示5%）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal serviceFeeRate;

    @Size(max = 500, message = "套餐特性说明最多500个字符")
    @Schema(description = "套餐特性说明（JSON格式）")
    private String features;

    @Schema(description = "排序权重，默认0")
    private Integer sortOrder;
}
