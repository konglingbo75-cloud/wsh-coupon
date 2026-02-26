package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "套餐购买记录列表项")
public class AdminPackagePurchaseResponse {

    @Schema(description = "购买记录ID")
    private Long purchaseId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "套餐名称")
    private String packageName;

    @Schema(description = "实付金额")
    private BigDecimal pricePaid;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "有效期开始")
    private String validStartDate;

    @Schema(description = "有效期结束")
    private String validEndDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
