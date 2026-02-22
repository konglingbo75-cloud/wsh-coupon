package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "商户入驻费记录响应")
public class OnboardingFeeResponse {

    @Schema(description = "入驻费记录ID")
    private Long feeId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "套餐ID")
    private Long planId;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "缴纳金额（元）")
    private BigDecimal feeAmount;

    @Schema(description = "支付状态：0待支付 1已支付 2已关闭")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "有效期开始日期")
    private LocalDate validStartDate;

    @Schema(description = "有效期结束日期")
    private LocalDate validEndDate;

    @Schema(description = "剩余天数（-1表示未支付）")
    private Integer remainingDays;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
