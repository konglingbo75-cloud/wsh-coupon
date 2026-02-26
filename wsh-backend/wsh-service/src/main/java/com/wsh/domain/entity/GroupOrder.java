package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拼团记录实体
 */
@Data
@TableName("tb_group_order")
public class GroupOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long groupOrderId;

    /** 拼团编号 */
    private String groupNo;

    /** 活动ID */
    private Long activityId;

    /** 发起人用户ID */
    private Long initiatorUserId;

    /** 成团所需人数 */
    private Integer requiredMembers;

    /** 当前参与人数 */
    private Integer currentMembers;

    /** 状态 0:拼团中 1:已成团 2:已失败(超时) 3:已取消 */
    private Integer status;

    /** 拼团截止时间 */
    private LocalDateTime expireTime;

    /** 成团时间 */
    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 状态常量
    public static final int STATUS_PENDING = 0;    // 拼团中
    public static final int STATUS_SUCCESS = 1;    // 已成团
    public static final int STATUS_FAILED = 2;     // 已失败(超时)
    public static final int STATUS_CANCELLED = 3;  // 已取消
}
