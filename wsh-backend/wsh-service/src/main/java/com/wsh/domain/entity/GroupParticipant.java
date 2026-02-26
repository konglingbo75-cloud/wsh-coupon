package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拼团参与者实体
 */
@Data
@TableName("tb_group_participant")
public class GroupParticipant {

    @TableId(type = IdType.ASSIGN_ID)
    private Long participantId;

    /** 拼团记录ID */
    private Long groupOrderId;

    /** 用户ID */
    private Long userId;

    /** 关联订单ID */
    private Long orderId;

    /** 是否发起人 0:否 1:是 */
    private Integer isInitiator;

    /** 加入时间 */
    private LocalDateTime joinTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
