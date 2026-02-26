package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@Schema(description = "商户活动详情响应（编辑用）")
public class MerchantActivityDetailResponse {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "活动类型：1代金券 3积分兑换 4团购")
    private Integer activityType;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "是否公开")
    private Boolean isPublic;

    @Schema(description = "活动状态：0草稿 1进行中 2暂停 3结束")
    private Integer status;

    @Schema(description = "类型特定配置")
    private Map<String, Object> typeConfig;
}
