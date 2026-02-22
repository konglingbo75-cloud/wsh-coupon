package com.wsh.integration.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 从商户系统同步的活动数据
 */
@Data
@Builder
public class ActivityDTO {

    private String sourceActivityId;
    /** 活动类型：1积分兑换 2储值 3代金券 4团购 */
    private Integer activityType;
    private String activityName;
    private String activityDesc;
    private String coverImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 活动配置JSON */
    private String config;
    private Integer stock;
    private Integer soldCount;
    /** 目标会员类型：0全部 1仅活跃 2仅沉睡 */
    private Integer targetMemberType;
    /** 是否公开可见：0仅会员 1公开 */
    private Integer isPublic;
    private Integer sortOrder;
}
