package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_service_package")
public class ServicePackage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long packageId;

    private String packageName;
    private Integer packageType;
    private BigDecimal price;
    private Integer durationMonths;
    private BigDecimal serviceFeeRate;
    private String features;
    private Integer status;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
