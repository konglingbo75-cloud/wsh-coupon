package com.wsh.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核销响应
 */
@Data
@Builder
@Schema(description = "核销响应")
public class VerifyResponse {

    @Schema(description = "核销记录ID")
    private Long recordId;

    @Schema(description = "券码ID")
    private Long voucherId;

    @Schema(description = "券码")
    private String voucherCode;

    @Schema(description = "券码类型")
    private Integer voucherType;

    @Schema(description = "券码类型名称")
    private String voucherTypeName;

    @Schema(description = "券码价值")
    private BigDecimal voucherValue;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "核销时间")
    private LocalDateTime verifyTime;

    @Schema(description = "是否沉睡唤醒核销")
    private Boolean isDormancyAwake;

    @Schema(description = "服务费")
    private BigDecimal serviceFee;

    @Schema(description = "商户到账金额")
    private BigDecimal merchantAmount;
}
