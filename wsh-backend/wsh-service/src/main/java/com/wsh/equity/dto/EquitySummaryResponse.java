package com.wsh.equity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "权益资产汇总响应")
public class EquitySummaryResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "全部积分可兑换总价值（元）")
    private BigDecimal totalPointsValue;

    @Schema(description = "全部储值余额（元）")
    private BigDecimal totalBalance;

    @Schema(description = "全部未用券价值（元）")
    private BigDecimal totalVoucherValue;

    @Schema(description = "权益资产总价值（积分+储值+券）")
    private BigDecimal totalAssetValue;

    @Schema(description = "7天内过期积分价值（元）")
    private BigDecimal expiringPointsValue;

    @Schema(description = "7天内过期券数量")
    private Integer expiringVoucherCount;

    @Schema(description = "有权益的商户数")
    private Integer merchantCount;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdated;
}
