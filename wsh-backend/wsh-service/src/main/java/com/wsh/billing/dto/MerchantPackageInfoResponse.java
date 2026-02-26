package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "商户当前套餐信息")
public class MerchantPackageInfoResponse {

    @Schema(description = "购买记录ID")
    private Long purchaseId;

    @Schema(description = "套餐ID")
    private Long packageId;

    @Schema(description = "套餐名称")
    private String packageName;

    @Schema(description = "套餐类型")
    private Integer packageType;

    @Schema(description = "实付金额")
    private BigDecimal pricePaid;

    @Schema(description = "服务费率")
    private BigDecimal serviceFeeRate;

    @Schema(description = "支付状态：0待支付 1已支付 2已关闭")
    private Integer payStatus;

    @Schema(description = "有效期开始")
    private LocalDate validStartDate;

    @Schema(description = "有效期结束")
    private LocalDate validEndDate;

    @Schema(description = "剩余天数")
    private Integer remainingDays;

    @Schema(description = "套餐功能描述")
    private String features;

    @Schema(description = "购买时间")
    private LocalDateTime createdAt;
}
