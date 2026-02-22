package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_branch")
public class MerchantBranch {

    @TableId(type = IdType.ASSIGN_ID)
    private Long branchId;

    private Long merchantId;
    private String branchName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
