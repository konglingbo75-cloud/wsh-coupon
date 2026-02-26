package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Schema(description = "活动创建/更新请求")
public class ActivityCreateRequest {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 128, message = "活动标题不能超过128个字符")
    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "活动类型不能为空")
    @Schema(description = "活动类型：1代金券 3积分兑换 4团购", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityType;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价（划线价）")
    private BigDecimal originalPrice;

    @Schema(description = "库存数量，-1表示不限库存")
    private Integer stock;

    @Schema(description = "活动描述")
    @Size(max = 500, message = "活动描述不能超过500个字符")
    private String description;

    @Schema(description = "开始时间，格式 yyyy-MM-dd")
    private String startTime;

    @Schema(description = "结束时间，格式 yyyy-MM-dd")
    private String endTime;

    @Schema(description = "是否公开")
    private Boolean isPublic;

    @Schema(description = "活动状态：draft=草稿 active=发布")
    private String status;

    @Schema(description = "类型特定配置，如 {faceValue:50, minConsume:100}")
    private Map<String, Object> typeConfig;
}
