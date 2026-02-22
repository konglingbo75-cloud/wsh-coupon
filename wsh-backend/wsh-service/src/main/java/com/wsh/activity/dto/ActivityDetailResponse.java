package com.wsh.activity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动详情响应
 */
@Data
@Builder
public class ActivityDetailResponse {

    private Long activityId;
    private Long merchantId;
    private String merchantName;
    private String merchantLogo;
    
    /** 活动类型：1代金券 2储值 3积分兑换 4团购 */
    private Integer activityType;
    private String activityTypeName;
    private String activityName;
    private String activityDesc;
    private String coverImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /** 活动配置JSON */
    private String config;
    
    /** 库存，-1表示无限 */
    private Integer stock;
    private Integer soldCount;
    
    /** 目标会员类型：0全部 1仅活跃 2仅沉睡 */
    private Integer targetMemberType;
    
    /** 是否公开可见 */
    private Integer isPublic;
    
    /** 是否为专属活动（根据用户会员状态匹配） */
    private Boolean isExclusive;
    
    /** 专属提示语 */
    private String exclusiveTip;
    
    /** 活动状态：1进行中 2已结束 */
    private Integer status;
    
    /** 是否还有库存 */
    private Boolean hasStock;
    
    private LocalDateTime createdAt;
}
