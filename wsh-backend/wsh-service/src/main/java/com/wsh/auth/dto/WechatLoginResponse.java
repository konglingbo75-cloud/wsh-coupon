package com.wsh.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "微信登录响应")
public class WechatLoginResponse {

    @Schema(description = "JWT Token，后续请求放入 Authorization: Bearer {token}")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "手机号（已授权时返回）")
    private String phone;

    @Schema(description = "角色：0消费者 1商户管理员 2商户员工")
    private Integer role;

    @Schema(description = "是否新用户（首次登录）")
    private Boolean isNewUser;
}
