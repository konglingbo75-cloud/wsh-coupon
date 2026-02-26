package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "月度服务费汇总列表项")
public class AdminServiceFeeSummaryResponse {

    @Schema(description = "汇总ID")
    private Long summaryId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "年月")
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
    private LocalDateTime deductTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
