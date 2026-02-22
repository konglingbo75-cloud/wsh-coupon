package com.wsh.equity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "权益提醒消息")
public class EquityReminderResponse {

    @Schema(description = "提醒ID")
    private Long reminderId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "提醒类型：1积分过期 2储值提醒 3券过期 4等级降级 5沉睡唤醒")
    private Integer reminderType;

    @Schema(description = "提醒类型文字描述")
    private String reminderTypeText;

    @Schema(description = "权益类型：points/balance/voucher")
    private String equityType;

    @Schema(description = "权益价值（元）")
    private BigDecimal equityValue;

    @Schema(description = "过期日期")
    private LocalDate expireDate;

    @Schema(description = "发送状态：0待发送 1已发送 2发送失败")
    private Integer remindStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
