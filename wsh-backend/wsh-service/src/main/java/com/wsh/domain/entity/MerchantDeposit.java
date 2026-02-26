package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_deposit")
public class MerchantDeposit {

    @TableId(type = IdType.ASSIGN_ID)
    private Long depositId;

    private Long merchantId;
    private BigDecimal depositAmount;
    private Integer payStatus;
    private LocalDateTime payTime;
    private String transactionId;
    private LocalDateTime refundTime;
    private String refundReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
