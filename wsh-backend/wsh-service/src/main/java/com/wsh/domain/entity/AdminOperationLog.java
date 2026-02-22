package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_admin_operation_log")
public class AdminOperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    private Long adminId;
    private String module;
    private String action;
    private String targetId;
    private String detail;
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
