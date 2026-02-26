package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_balance_log")
public class MerchantBalanceLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    private Long merchantId;
    private Integer changeType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String relatedOrderNo;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
