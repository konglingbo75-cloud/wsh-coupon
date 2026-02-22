package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "入驻费列表项")
public class AdminBillingListResponse {

    @Schema(description = "费用ID")
    private Long feeId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "费用金额")
    private BigDecimal feeAmount;

    @Schema(description = "支付状态：0待支付 1已支付 2已关闭")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "有效起始日期")
    private String validStartDate;

    @Schema(description = "有效截止日期")
    private String validEndDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
