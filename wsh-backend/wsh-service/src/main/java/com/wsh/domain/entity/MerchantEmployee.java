package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_employee")
public class MerchantEmployee {

    @TableId(type = IdType.ASSIGN_ID)
    private Long employeeId;

    private Long merchantId;
    private Long branchId;
    private String name;
    private String phone;
    private String openid;
    private Integer role;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
