package com.wsh.equity.job;

import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.equity.service.EquityReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 权益扫描定时任务
 * 每日凌晨 01:00 执行：
 * 扫描所有用户的即将过期权益（积分/储值/券），生成提醒记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquityScanJob {

    private final EquityReminderService equityReminderService;
    private final RedisUtil redisUtil;

    /**
     * 每天凌晨 01:00 执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void execute() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisUtil.tryLock(Constants.LOCK_EQUITY_SCAN, lockValue, 30, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(locked)) {
            log.info("EquityScanJob: 未获取到分布式锁，跳过执行");
            return;
        }

        try {
            log.info("===== 权益扫描任务开始 =====");
            int created = equityReminderService.scanAndCreateReminders();
            log.info("===== 权益扫描任务完成: 共创建 {} 条提醒记录 =====", created);
        } catch (Exception e) {
            log.error("EquityScanJob: 执行异常", e);
        } finally {
            redisUtil.releaseLock(Constants.LOCK_EQUITY_SCAN, lockValue);
        }
    }
}
