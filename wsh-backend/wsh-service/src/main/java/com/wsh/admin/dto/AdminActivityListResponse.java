package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "活动列表项")
public class AdminActivityListResponse {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "活动类型：1代金券 2储值 3积分兑换 4团购")
    private Integer activityType;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动状态：0草稿 1上架 2下架 3结束")
    private Integer status;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "已售")
    private Integer soldCount;

    @Schema(description = "是否公开")
    private Integer isPublic;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
