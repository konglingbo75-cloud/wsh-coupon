package com.wsh.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务费汇总响应
 */
@Data
@Builder
@Schema(description = "服务费汇总响应")
public class ServiceFeeSummaryResponse {

    @Schema(description = "总交易金额")
    private BigDecimal totalOrderAmount;

    @Schema(description = "总服务费")
    private BigDecimal totalServiceFee;

    @Schema(description = "总到账金额")
    private BigDecimal totalMerchantAmount;

    @Schema(description = "核销总笔数")
    private Integer totalCount;

    @Schema(description = "本月交易金额")
    private BigDecimal monthOrderAmount;

    @Schema(description = "本月服务费")
    private BigDecimal monthServiceFee;

    @Schema(description = "本月到账金额")
    private BigDecimal monthMerchantAmount;

    @Schema(description = "本月核销笔数")
    private Integer monthCount;

    @Schema(description = "服务费率")
    private BigDecimal serviceFeeRate;

    @Schema(description = "服务费率百分比")
    private String serviceFeeRatePercent;
}
