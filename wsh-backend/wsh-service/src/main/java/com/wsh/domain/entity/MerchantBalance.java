package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_balance")
public class MerchantBalance {

    @TableId(type = IdType.ASSIGN_ID)
    private Long balanceId;

    private Long merchantId;
    private BigDecimal balance;
    private BigDecimal totalRecharge;
    private BigDecimal totalConsume;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
