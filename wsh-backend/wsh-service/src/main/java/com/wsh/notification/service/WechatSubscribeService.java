package com.wsh.notification.service;

import com.wsh.domain.entity.EquityReminder;
import com.wsh.domain.entity.User;
import com.wsh.domain.mapper.EquityReminderMapper;
import com.wsh.domain.mapper.UserMapper;
import com.wsh.integration.wechat.WechatApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信订阅消息服务
 * 负责发送权益过期提醒等订阅消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatSubscribeService {

    private final WechatApiClient wechatApiClient;
    private final UserMapper userMapper;
    private final EquityReminderMapper reminderMapper;

    // 订阅消息模板ID（需在微信小程序后台配置）
    private static final String TPL_EQUITY_EXPIRE = "your_template_id_here";

    /**
     * 发送权益过期提醒订阅消息
     *
     * @param reminder 提醒记录
     * @return 是否发送成功
     */
    @Transactional
    public boolean sendEquityExpireReminder(EquityReminder reminder) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null || user.getOpenid() == null) {
                log.warn("发送订阅消息失败: 用户不存在或无openid, userId={}", reminder.getUserId());
                return false;
            }

            // 构建消息内容
            Map<String, Object> data = buildEquityExpireData(reminder);

            // 调用微信API发送订阅消息
            boolean success = wechatApiClient.sendSubscribeMessage(
                    user.getOpenid(),
                    TPL_EQUITY_EXPIRE,
                    "/subPackages/consumer/equity/expiring",
                    data
            );

            // 更新提醒状态
            reminder.setRemindStatus(success ? 1 : 2);
            reminder.setRemindTime(LocalDateTime.now());
            reminderMapper.updateById(reminder);

            if (success) {
                log.info("订阅消息发送成功: userId={}, reminderType={}", reminder.getUserId(), reminder.getReminderType());
            } else {
                log.warn("订阅消息发送失败: userId={}, reminderType={}", reminder.getUserId(), reminder.getReminderType());
            }

            return success;
        } catch (Exception e) {
            log.error("发送订阅消息异常: userId={}", reminder.getUserId(), e);
            reminder.setRemindStatus(2);
            reminder.setRemindTime(LocalDateTime.now());
            reminderMapper.updateById(reminder);
            return false;
        }
    }

    /**
     * 构建权益过期提醒消息数据
     */
    private Map<String, Object> buildEquityExpireData(EquityReminder reminder) {
        Map<String, Object> data = new HashMap<>();

        // 权益类型
        String equityTypeText = switch (reminder.getEquityType()) {
            case "points" -> "积分";
            case "balance" -> "储值余额";
            case "voucher" -> "优惠券";
            default -> "权益";
        };

        // thing1: 提醒内容
        data.put("thing1", Map.of("value", "您的" + equityTypeText + "即将过期"));

        // amount2: 权益价值
        if (reminder.getEquityValue() != null) {
            data.put("amount2", Map.of("value", reminder.getEquityValue().toString()));
        }

        // date3: 过期日期
        if (reminder.getExpireDate() != null) {
            data.put("date3", Map.of("value", reminder.getExpireDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        }

        // thing4: 温馨提示
        data.put("thing4", Map.of("value", "请及时使用，避免过期浪费"));

        return data;
    }
}
