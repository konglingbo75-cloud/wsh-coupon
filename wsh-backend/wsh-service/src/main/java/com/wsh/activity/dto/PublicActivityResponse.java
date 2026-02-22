package com.wsh.activity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 公开活动广场响应（按类型分组）
 */
@Data
@Builder
public class PublicActivityResponse {

    /** 城市 */
    private String city;
    
    /** 按活动类型分组的活动列表 */
    private Map<String, List<ActivityDetailResponse>> activitiesByType;
    
    /** 代金券活动 */
    private List<ActivityDetailResponse> voucherActivities;
    
    /** 储值活动 */
    private List<ActivityDetailResponse> depositActivities;
    
    /** 积分兑换活动 */
    private List<ActivityDetailResponse> pointsActivities;
    
    /** 团购活动 */
    private List<ActivityDetailResponse> groupActivities;
    
    /** 各类型活动数量统计 */
    private ActivityTypeCount typeCount;
    
    @Data
    @Builder
    public static class ActivityTypeCount {
        private Integer voucher;
        private Integer deposit;
        private Integer points;
        private Integer group;
        private Integer total;
    }
}
