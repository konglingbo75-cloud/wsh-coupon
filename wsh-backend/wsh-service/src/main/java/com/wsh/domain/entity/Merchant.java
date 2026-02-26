package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant")
public class Merchant {

    @TableId(type = IdType.ASSIGN_ID)
    private Long merchantId;

    private String merchantCode;
    private String merchantName;
    private String logoUrl;
    private String contactName;
    private String contactPhone;
    private String address;
    private String city;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String businessCategory;
    private Integer status;
    private Integer integrationType;
    private BigDecimal profitSharingRate;
    private Integer serviceFeeMode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
