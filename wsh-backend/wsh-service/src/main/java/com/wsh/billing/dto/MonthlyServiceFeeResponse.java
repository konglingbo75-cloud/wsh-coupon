package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "月度服务费汇总")
public class MonthlyServiceFeeResponse {

    @Schema(description = "汇总ID")
    private Long summaryId;

    @Schema(description = "年月，格式 yyyy-MM")
    private String yearMonth;

    @Schema(description = "订单笔数")
    private Integer orderCount;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "服务费金额")
    private BigDecimal serviceFee;

    @Schema(description = "扣减状态：0待扣减 1已扣减 2余额不足")
    private Integer deductStatus;

    @Schema(description = "扣减时间")
    private String deductTime;
}
