package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("tb_order")
public class Order {

    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId;

    /** 订单号（唯一） */
    private String orderNo;

    private Long userId;
    private Long merchantId;
    private Long activityId;

    /** 订单类型：1代金券 2储值 3积分兑换 4团购 5沉睡唤醒券 */
    private Integer orderType;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 状态：0待支付 1已支付 2已关闭 3已退款 */
    private Integer status;

    private LocalDateTime payTime;

    /** 微信支付单号 */
    private String transactionId;

    /** 是否沉睡唤醒订单：0否 1是 */
    private Integer isDormancyAwake;

    /** 关联拼团ID（拼团订单专用） */
    private Long groupOrderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
