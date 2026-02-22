package com.wsh.equity.controller;

import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.equity.dto.*;
import com.wsh.equity.service.EquityReminderService;
import com.wsh.equity.service.EquitySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04-权益资产")
@RestController
@RequestMapping("/v1/equity")
@RequiredArgsConstructor
public class EquityController {

    private final EquitySummaryService equitySummaryService;
    private final EquityReminderService equityReminderService;

    @Operation(summary = "权益资产总览", description = "获取当前用户的权益资产汇总（积分+储值+券）")
    @GetMapping("/summary")
    public R<EquitySummaryResponse> getSummary() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(equitySummaryService.getSummary(userId));
    }

    @Operation(summary = "刷新权益汇总", description = "强制重新计算权益汇总数据")
    @PostMapping("/summary/refresh")
    public R<EquitySummaryResponse> refreshSummary() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(equitySummaryService.refreshSummary(userId));
    }

    @Operation(summary = "即将过期的权益列表", description = "获取7天内即将过期的积分、券等权益")
    @GetMapping("/expiring")
    public R<ExpiringEquityResponse> getExpiringEquities() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(equityReminderService.getExpiringEquities(userId));
    }

    @Operation(summary = "权益提醒消息列表", description = "获取用户的权益提醒消息（站内消息）")
    @GetMapping("/messages")
    public R<List<EquityReminderResponse>> getMessages() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(equityReminderService.getUserReminders(userId));
    }

    @Operation(summary = "获取通知设置", description = "获取当前用户的权益提醒通知设置")
    @GetMapping("/notification-settings")
    public R<NotificationSettingResponse> getNotificationSettings() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(equityReminderService.getNotificationSetting(userId));
    }

    @Operation(summary = "更新通知设置", description = "更新用户的权益提醒通知设置（积分/储值/券过期提醒开关）")
    @PutMapping("/notification-settings")
    public R<NotificationSettingResponse> updateNotificationSettings(@RequestBody NotificationSettingUpdateRequest request) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(equityReminderService.updateNotificationSetting(userId, request));
    }
}
