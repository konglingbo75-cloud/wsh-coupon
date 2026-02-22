package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "管理员登录响应")
public class AdminLoginResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色：1-超级管理员 2-运营人员")
    private Integer role;
}
