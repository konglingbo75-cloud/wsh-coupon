package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_notification_setting")
public class UserNotificationSetting {

    @TableId(type = IdType.ASSIGN_ID)
    private Long settingId;

    private Long userId;

    /** 积分过期提醒 0关 1开 */
    private Integer pointsExpireNotify;

    /** 储值余额提醒 0关 1开 */
    private Integer balanceNotify;

    /** 券过期提醒 0关 1开 */
    private Integer voucherExpireNotify;

    /** 新活动提醒 0关 1开 */
    private Integer activityNotify;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
