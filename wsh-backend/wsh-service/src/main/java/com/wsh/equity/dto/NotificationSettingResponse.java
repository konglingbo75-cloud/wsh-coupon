package com.wsh.equity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户通知设置响应")
public class NotificationSettingResponse {

    @Schema(description = "积分过期提醒：0关 1开")
    private Integer pointsExpireNotify;

    @Schema(description = "储值余额提醒：0关 1开")
    private Integer balanceNotify;

    @Schema(description = "券过期提醒：0关 1开")
    private Integer voucherExpireNotify;

    @Schema(description = "新活动提醒：0关 1开")
    private Integer activityNotify;
}
