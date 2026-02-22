package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_audit_log")
public class MerchantAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    private Long merchantId;
    private Long adminId;
    private String action;
    private Integer prevStatus;
    private Integer newStatus;
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
