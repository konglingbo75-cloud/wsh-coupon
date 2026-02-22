package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "仪表盘统计数据")
public class DashboardStatsResponse {

    @Schema(description = "商户总数")
    private Long totalMerchants;

    @Schema(description = "待审核商户数")
    private Long pendingMerchants;

    @Schema(description = "活跃商户数")
    private Long activeMerchants;

    @Schema(description = "冻结商户数")
    private Long frozenMerchants;

    @Schema(description = "用户总数")
    private Long totalUsers;

    @Schema(description = "活动总数")
    private Long totalActivities;

    @Schema(description = "进行中活动数")
    private Long activeActivities;

    @Schema(description = "订单总数")
    private Long totalOrders;

    @Schema(description = "订单总金额")
    private BigDecimal totalOrderAmount;

    @Schema(description = "已核销券数")
    private Long verifiedVouchers;

    @Schema(description = "入驻费收入总额")
    private BigDecimal totalOnboardingFee;

    @Schema(description = "服务费收入总额")
    private BigDecimal totalServiceFee;
}
