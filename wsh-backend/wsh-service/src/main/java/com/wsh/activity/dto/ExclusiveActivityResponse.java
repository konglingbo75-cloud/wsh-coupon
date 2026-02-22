package com.wsh.activity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 专属活动响应
 */
@Data
@Builder
public class ExclusiveActivityResponse {

    /** 专属活动列表 */
    private List<ActivityDetailResponse> activities;
    
    /** 总数 */
    private Integer total;
    
    /** 沉睡会员专属活动数 */
    private Integer dormantExclusiveCount;
    
    /** 活跃会员专属活动数 */
    private Integer activeExclusiveCount;
    
    /** 用户是否有沉睡会员身份 */
    private Boolean hasDormantMembership;
    
    /** 沉睡会员身份对应的商户数 */
    private Integer dormantMerchantCount;
}
