package com.wsh.equity.job;

import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.EquityReminder;
import com.wsh.domain.mapper.EquityReminderMapper;
import com.wsh.notification.service.InAppMessageService;
import com.wsh.notification.service.WechatSubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 提醒发送定时任务
 * 每日 08:00 执行：
 * 将待发送的提醒记录通过微信订阅消息和站内消息发送给用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderSendJob {

    private final EquityReminderMapper reminderMapper;
    private final WechatSubscribeService wechatSubscribeService;
    private final InAppMessageService inAppMessageService;
    private final RedisUtil redisUtil;

    private static final String LOCK_REMINDER_SEND = "lock:reminder:send";

    /**
     * 每天 08:00 执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void execute() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisUtil.tryLock(LOCK_REMINDER_SEND, lockValue, 30, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(locked)) {
            log.info("ReminderSendJob: 未获取到分布式锁，跳过执行");
            return;
        }

        try {
            log.info("===== 提醒发送任务开始 =====");
            sendAllPending();
            log.info("===== 提醒发送任务完成 =====");
        } catch (Exception e) {
            log.error("ReminderSendJob: 执行异常", e);
        } finally {
            redisUtil.releaseLock(LOCK_REMINDER_SEND, lockValue);
        }
    }

    /**
     * 发送所有待发送的提醒
     */
    private void sendAllPending() {
        List<EquityReminder> pending = reminderMapper.selectPending();

        if (pending.isEmpty()) {
            log.info("ReminderSendJob: 无待发送的提醒记录");
            return;
        }

        int wechatSuccess = 0;
        int wechatFailed = 0;
        int inAppCount = 0;

        for (EquityReminder reminder : pending) {
            // 1. 尝试发送微信订阅消息
            try {
                boolean success = wechatSubscribeService.sendEquityExpireReminder(reminder);
                if (success) {
                    wechatSuccess++;
                } else {
                    wechatFailed++;
                    // 微信发送失败，至少确保站内消息可见（状态已在 service 中更新）
                }
            } catch (Exception e) {
                wechatFailed++;
                log.warn("发送微信订阅消息异常: reminderId={}", reminder.getReminderId(), e);
                // 微信发送失败时，确保站内消息标记
                inAppMessageService.markAsInAppSent(reminder);
            }

            // 站内消息在创建时即可见，无需额外处理
            inAppCount++;
        }

        log.info("ReminderSendJob 统计: 总数={}, 微信成功={}, 微信失败={}, 站内={}", 
                pending.size(), wechatSuccess, wechatFailed, inAppCount);
    }
}
