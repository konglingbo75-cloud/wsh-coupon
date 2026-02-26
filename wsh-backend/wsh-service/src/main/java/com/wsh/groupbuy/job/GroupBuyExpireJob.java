package com.wsh.groupbuy.job;

import com.wsh.common.redis.util.RedisUtil;
import com.wsh.groupbuy.service.GroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 拼团超时处理定时任务
 * 每分钟执行一次，处理超时未成团的拼团订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyExpireJob {

    private final GroupBuyService groupBuyService;
    private final RedisUtil redisUtil;

    private static final String LOCK_KEY = "lock:groupbuy:expire";
    private static final int LOCK_EXPIRE_SECONDS = 120;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processExpiredGroups() {
        // 获取分布式锁
        boolean locked = Boolean.TRUE.equals(redisUtil.tryLock(LOCK_KEY, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS));
        if (!locked) {
            log.debug("拼团超时任务正在执行中，跳过本次");
            return;
        }

        try {
            log.info("开始执行拼团超时处理任务...");
            int processedCount = groupBuyService.handleExpiredGroups();
            if (processedCount > 0) {
                log.info("拼团超时处理任务完成，处理拼团数: {}", processedCount);
            }
        } catch (Exception e) {
            log.error("拼团超时处理任务执行失败", e);
        } finally {
            redisUtil.delete(LOCK_KEY);
        }
    }
}
