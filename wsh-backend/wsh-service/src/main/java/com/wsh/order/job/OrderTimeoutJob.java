package com.wsh.order.job;

import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 订单超时关闭定时任务
 * 每5分钟执行一次，关闭超时未支付的订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderService orderService;
    private final RedisUtil redisUtil;

    private static final String LOCK_KEY = "lock:order:timeout";
    private static final int LOCK_EXPIRE_SECONDS = 300;

    /**
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void closeExpiredOrders() {
        // 获取分布式锁
        boolean locked = Boolean.TRUE.equals(redisUtil.tryLock(LOCK_KEY, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS));
        if (!locked) {
            log.debug("订单超时任务正在执行中，跳过本次");
            return;
        }

        try {
            log.info("开始执行订单超时关闭任务...");
            int closedCount = orderService.closeExpiredOrders();
            log.info("订单超时关闭任务完成，关闭订单数: {}", closedCount);
        } catch (Exception e) {
            log.error("订单超时关闭任务执行失败", e);
        } finally {
            redisUtil.delete(LOCK_KEY);
        }
    }
}
