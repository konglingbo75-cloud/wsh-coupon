package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "用户列表项")
public class AdminUserListResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关联商户数")
    private Integer merchantCount;

    @Schema(description = "消费总金额")
    private BigDecimal totalConsumeAmount;

    @Schema(description = "消费总次数")
    private Integer totalConsumeCount;

    @Schema(description = "会员等级")
    private Integer memberLevel;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;
}
