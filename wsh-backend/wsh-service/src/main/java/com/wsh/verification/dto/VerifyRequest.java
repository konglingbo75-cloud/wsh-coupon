package com.wsh.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 核销请求
 */
@Data
@Schema(description = "核销请求")
public class VerifyRequest {

    @NotBlank(message = "券码不能为空")
    @Schema(description = "券码", required = true, example = "V1A2B3C4D5E6")
    private String voucherCode;

    @Schema(description = "门店ID（可选）")
    private Long branchId;
}
