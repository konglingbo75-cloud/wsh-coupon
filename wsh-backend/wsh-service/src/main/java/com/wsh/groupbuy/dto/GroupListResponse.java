package com.wsh.groupbuy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团列表响应
 */
@Data
@Builder
public class GroupListResponse {
    
    /** 拼团列表 */
    private List<GroupItem> groups;
    
    /** 总数 */
    private Integer total;
    
    /** 当前页 */
    private Integer page;
    
    /** 每页大小 */
    private Integer pageSize;
    
    @Data
    @Builder
    public static class GroupItem {
        /** 拼团ID */
        private Long groupOrderId;
        
        /** 拼团编号 */
        private String groupNo;
        
        /** 活动ID */
        private Long activityId;
        
        /** 活动名称 */
        private String activityName;
        
        /** 活动封面 */
        private String coverImage;
        
        /** 商户名称 */
        private String merchantName;
        
        /** 拼团价格 */
        private BigDecimal groupPrice;
        
        /** 成团所需人数 */
        private Integer requiredMembers;
        
        /** 当前参与人数 */
        private Integer currentMembers;
        
        /** 状态 */
        private Integer status;
        
        /** 状态名称 */
        private String statusName;
        
        /** 是否为发起人 */
        private Boolean isInitiator;
        
        /** 拼团截止时间 */
        private LocalDateTime expireTime;
        
        /** 剩余秒数 */
        private Long remainingSeconds;
        
        /** 创建时间 */
        private LocalDateTime createdAt;
    }
}
