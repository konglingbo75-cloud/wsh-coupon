package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_admin_user")
public class AdminUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long adminId;

    private String username;
    private String password;
    private String realName;
    private Integer role;
    private Integer status;
    private LocalDateTime lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
