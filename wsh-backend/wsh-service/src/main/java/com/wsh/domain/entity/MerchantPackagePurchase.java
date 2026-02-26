package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_package_purchase")
public class MerchantPackagePurchase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long purchaseId;

    private Long merchantId;
    private Long packageId;
    private BigDecimal pricePaid;
    private Integer payStatus;
    private LocalDateTime payTime;
    private String transactionId;
    private LocalDate validStartDate;
    private LocalDate validEndDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
