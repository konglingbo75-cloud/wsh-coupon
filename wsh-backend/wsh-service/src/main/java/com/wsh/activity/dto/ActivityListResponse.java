package com.wsh.activity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 活动列表响应
 */
@Data
@Builder
public class ActivityListResponse {

    /** 活动列表 */
    private List<ActivityDetailResponse> activities;
    
    /** 总数 */
    private Integer total;
    
    /** 当前页 */
    private Integer page;
    
    /** 每页大小 */
    private Integer pageSize;
}
