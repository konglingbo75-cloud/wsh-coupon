package com.wsh.activity.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 附近活动响应
 */
@Data
@Builder
public class NearbyActivityResponse {

    /** 活动列表（按距离排序） */
    private List<NearbyActivityItem> activities;
    
    /** 总数 */
    private Integer total;
    
    /** 搜索范围（公里） */
    private Integer distanceKm;
    
    @Data
    @Builder
    public static class NearbyActivityItem {
        
        private ActivityDetailResponse activity;
        
        /** 商户距离（米） */
        private Integer distanceMeters;
        
        /** 商户地址 */
        private String merchantAddress;
        
        /** 商户经度 */
        private BigDecimal longitude;
        
        /** 商户纬度 */
        private BigDecimal latitude;
    }
}
