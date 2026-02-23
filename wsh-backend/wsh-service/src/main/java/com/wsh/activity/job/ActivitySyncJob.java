package com.wsh.activity.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.matching.service.ActivityMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 活动同步定时任务
 * 定时从商户系统同步活动数据到平台
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivitySyncJob {

    private final ActivityMatchingService activityMatchingService;
    private final MerchantMapper merchantMapper;
    private final RedisUtil redisUtil;

    private static final String LOCK_KEY = "lock:activity:sync";
    private static final int LOCK_EXPIRE_SECONDS = 30 * 60; // 30分钟

    /**
     * 每天凌晨2点执行活动同步
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncActivities() {
        log.info("========= 开始执行活动同步任务 =========");

        // 获取分布式锁
        String lockValue = UUID.randomUUID().toString();
        boolean locked = Boolean.TRUE.equals(redisUtil.tryLock(LOCK_KEY, lockValue, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS));
        if (!locked) {
            log.warn("活动同步任务已在其他节点执行，跳过");
            return;
        }

        try {
            doSync();
        } catch (Exception e) {
            log.error("活动同步任务异常: {}", e.getMessage(), e);
        } finally {
            redisUtil.releaseLock(LOCK_KEY, lockValue);
            log.info("========= 活动同步任务执行结束 =========");
        }
    }

    /**
     * 手动触发同步（用于测试或紧急更新）
     */
    public void manualSync() {
        log.info("手动触发活动同步");
        doSync();
    }

    /**
     * 同步指定商户的活动
     */
    public int syncMerchantActivities(Long merchantId) {
        try {
            int count = activityMatchingService.syncActivitiesFromMerchant(merchantId);
            log.info("商户{}活动同步完成: {}个", merchantId, count);
            
            // 清除相关缓存
            clearMerchantActivityCache(merchantId);
            
            return count;
        } catch (Exception e) {
            log.error("商户{}活动同步失败: {}", merchantId, e.getMessage(), e);
            return 0;
        }
    }

    private void doSync() {
        // 查询所有正常状态且需要API同步的商户
        List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, Constants.MERCHANT_STATUS_ACTIVE)
                        .in(Merchant::getIntegrationType, 1, 2)); // 1-API, 2-RPA

        if (merchants.isEmpty()) {
            log.info("没有需要同步活动的商户");
            return;
        }

        log.info("开始同步{}个商户的活动", merchants.size());

        int totalSynced = 0;
        int successCount = 0;
        int failCount = 0;
        Set<String> cities = merchants.stream()
                .map(Merchant::getCity)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());

        for (Merchant merchant : merchants) {
            try {
                int count = activityMatchingService.syncActivitiesFromMerchant(merchant.getMerchantId());
                totalSynced += count;
                successCount++;
                log.debug("商户[{}]同步完成: {}个活动", merchant.getMerchantName(), count);
            } catch (Exception e) {
                failCount++;
                log.error("商户[{}]同步失败: {}", merchant.getMerchantName(), e.getMessage());
            }
        }

        // 清除城市活动缓存
        for (String city : cities) {
            String cacheKey = Constants.CACHE_PUBLIC_ACTIVITY + city;
            redisUtil.delete(cacheKey);
        }

        log.info("活动同步完成: 成功{}个商户, 失败{}个商户, 共同步{}个活动",
                successCount, failCount, totalSynced);
    }

    private void clearMerchantActivityCache(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant != null && merchant.getCity() != null) {
            String cacheKey = Constants.CACHE_PUBLIC_ACTIVITY + merchant.getCity();
            redisUtil.delete(cacheKey);
        }
    }
}
