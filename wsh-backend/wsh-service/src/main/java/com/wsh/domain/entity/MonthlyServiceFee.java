package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_monthly_service_fee")
public class MonthlyServiceFee {

    @TableId(type = IdType.ASSIGN_ID)
    private Long summaryId;

    private Long merchantId;
    private String yearMonth;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal serviceFee;
    private Integer deductStatus;
    private LocalDateTime deductTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
