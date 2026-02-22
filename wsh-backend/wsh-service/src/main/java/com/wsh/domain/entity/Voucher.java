package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_voucher")
public class Voucher {

    @TableId(type = IdType.ASSIGN_ID)
    private Long voucherId;

    private String voucherCode;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Long activityId;

    /** 1代金券 2兑换券 3储值码 4沉睡唤醒券 */
    private Integer voucherType;

    private BigDecimal voucherValue;

    /** 0未使用 1已使用 2已过期 3已退款 */
    private Integer status;

    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private LocalDateTime usedTime;
    private Long usedBranchId;
    private Long usedEmployeeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
