package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_equity_reminder")
public class EquityReminder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long reminderId;

    private Long userId;
    private Long merchantId;

    /** 1积分过期 2储值提醒 3券过期 4等级降级 5沉睡唤醒 */
    private Integer reminderType;

    /** points/balance/voucher */
    private String equityType;

    private BigDecimal equityValue;
    private LocalDate expireDate;

    /** 0待发送 1已发送 2发送失败 */
    private Integer remindStatus;

    private LocalDateTime remindTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
