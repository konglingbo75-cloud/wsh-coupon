package com.wsh.equity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "即将过期的权益项")
public class ExpiringEquityItem {

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "权益类型：points/balance/voucher")
    private String equityType;

    @Schema(description = "权益类型文字描述")
    private String equityTypeText;

    @Schema(description = "权益价值（元）")
    private BigDecimal equityValue;

    @Schema(description = "过期日期")
    private LocalDate expireDate;

    @Schema(description = "距过期天数")
    private Integer daysUntilExpire;

    @Schema(description = "关联ID（快照ID或券ID）")
    private Long referenceId;
}
