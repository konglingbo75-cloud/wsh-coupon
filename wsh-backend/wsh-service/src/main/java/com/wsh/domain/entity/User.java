package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
