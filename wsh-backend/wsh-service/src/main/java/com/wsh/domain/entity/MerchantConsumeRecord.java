package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_consume_record")
public class MerchantConsumeRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

    private Long userId;
    private Long merchantId;
    private Long branchId;
    private LocalDateTime consumeTime;
    private BigDecimal consumeAmount;
    private Integer invoiceStatus;
    private String invoiceNo;
    private String invoiceUrl;
    private String sourceOrderNo;
    private LocalDateTime syncTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
