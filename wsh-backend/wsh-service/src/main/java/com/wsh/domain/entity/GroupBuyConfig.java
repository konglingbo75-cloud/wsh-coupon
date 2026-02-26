package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拼团配置实体
 */
@Data
@TableName("tb_group_buy_config")
public class GroupBuyConfig {

    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    /** 关联活动ID */
    private Long activityId;

    /** 最少成团人数 */
    private Integer minMembers;

    /** 最多成团人数 */
    private Integer maxMembers;

    /** 拼团有效期(小时) */
    private Integer expireHours;

    /** 超时自动退款 0:否 1:是 */
    private Integer autoRefund;

    /** 允许凑单购买 0:否 1:是 */
    private Integer allowSelfBuy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
