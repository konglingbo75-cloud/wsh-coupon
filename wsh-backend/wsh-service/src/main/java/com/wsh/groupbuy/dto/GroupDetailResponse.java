package com.wsh.groupbuy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团详情响应
 */
@Data
@Builder
public class GroupDetailResponse {
    
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
    
    /** 商户ID */
    private Long merchantId;
    
    /** 商户名称 */
    private String merchantName;
    
    /** 拼团价格 */
    private BigDecimal groupPrice;
    
    /** 原价 */
    private BigDecimal originalPrice;
    
    /** 成团所需人数 */
    private Integer requiredMembers;
    
    /** 当前参与人数 */
    private Integer currentMembers;
    
    /** 还差几人 */
    private Integer remainingMembers;
    
    /** 状态 0:拼团中 1:已成团 2:已失败 3:已取消 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 拼团截止时间 */
    private LocalDateTime expireTime;
    
    /** 剩余秒数 */
    private Long remainingSeconds;
    
    /** 成团时间 */
    private LocalDateTime completeTime;
    
    /** 发起人ID */
    private Long initiatorUserId;
    
    /** 发起人昵称 */
    private String initiatorNickname;
    
    /** 发起人头像 */
    private String initiatorAvatar;
    
    /** 是否为发起人 */
    private Boolean isInitiator;
    
    /** 是否已参与 */
    private Boolean hasJoined;
    
    /** 参与者列表 */
    private List<ParticipantItem> participants;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    public static class ParticipantItem {
        private Long userId;
        private String nickname;
        private String avatarUrl;
        private Boolean isInitiator;
        private LocalDateTime joinTime;
    }
}
