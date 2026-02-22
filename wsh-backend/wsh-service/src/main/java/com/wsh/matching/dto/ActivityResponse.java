package com.wsh.matching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "活动响应")
public class ActivityResponse {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "活动类型：1积分兑换 2储值 3代金券 4团购")
    private Integer activityType;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动描述")
    private String activityDesc;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动配置JSON")
    private String config;

    @Schema(description = "库存，-1无限")
    private Integer stock;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "目标会员类型：0全部 1仅活跃 2仅沉睡")
    private Integer targetMemberType;

    @Schema(description = "是否公开可见")
    private Integer isPublic;
}
