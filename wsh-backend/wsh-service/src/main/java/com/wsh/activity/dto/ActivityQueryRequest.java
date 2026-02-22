package com.wsh.activity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 活动查询请求
 */
@Data
public class ActivityQueryRequest {

    /** 商户ID（可选） */
    private Long merchantId;
    
    /** 活动类型：1代金券 2储值 3积分兑换 4团购（可选） */
    private Integer activityType;
    
    /** 城市（可选，用于同城筛选） */
    private String city;
    
    /** 用户经度（可选，用于附近推荐） */
    private BigDecimal longitude;
    
    /** 用户纬度（可选，用于附近推荐） */
    private BigDecimal latitude;
    
    /** 附近范围（公里，默认5公里） */
    private Integer distanceKm;
    
    /** 是否只查询专属活动 */
    private Boolean exclusiveOnly;
    
    /** 页码 */
    private Integer page = 1;
    
    /** 每页大小 */
    private Integer pageSize = 20;
}
