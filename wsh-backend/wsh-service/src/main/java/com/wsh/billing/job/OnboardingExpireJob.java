package com.wsh.billing.job;

import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantOnboardingFee;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.MerchantOnboardingFeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 入驻费到期定时任务
 * 每天凌晨 02:00 执行：
 * 1. 扫描已过期但商户仍为正常状态的记录 → 冻结商户
 * 2. 扫描即将过期（30/7/1天）的记录 → 生成提醒（预留）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnboardingExpireJob {

    private final MerchantOnboardingFeeMapper feeMapper;
    private final MerchantMapper merchantMapper;
    private final RedisUtil redisUtil;

    /**
     * 每天凌晨 02:00 执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisUtil.tryLock(Constants.LOCK_ONBOARDING_EXPIRE, lockValue, 30, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(locked)) {
            log.info("OnboardingExpireJob: 未获取到锁，跳过执行");
            return;
        }

        try {
            log.info("OnboardingExpireJob: 开始扫描入驻费到期记录");

            // 1. 冻结已过期商户
            freezeExpiredMerchants();

            // 2. 到期提醒（30/7/1天前提醒）
            sendExpirationReminders();

            log.info("OnboardingExpireJob: 执行完成");
        } catch (Exception e) {
            log.error("OnboardingExpireJob: 执行异常", e);
        } finally {
            redisUtil.releaseLock(Constants.LOCK_ONBOARDING_EXPIRE, lockValue);
        }
    }

    /**
     * 冻结已过期但商户状态仍为正常的记录
     */
    private void freezeExpiredMerchants() {
        LocalDate today = LocalDate.now();
        List<MerchantOnboardingFee> expiredList = feeMapper.selectExpiredWithActiveMerchant(today);

        if (expiredList.isEmpty()) {
            log.info("OnboardingExpireJob: 无需冻结的商户");
            return;
        }

        int frozenCount = 0;
        for (MerchantOnboardingFee fee : expiredList) {
            try {
                Merchant merchant = merchantMapper.selectById(fee.getMerchantId());
                if (merchant != null && merchant.getStatus() == Constants.MERCHANT_STATUS_ACTIVE) {
                    merchant.setStatus(Constants.MERCHANT_STATUS_FROZEN);
                    merchantMapper.updateById(merchant);
                    frozenCount++;
                    log.info("商户已冻结（入驻费到期）: merchantId={}, validEndDate={}",
                            fee.getMerchantId(), fee.getValidEndDate());
                }
            } catch (Exception e) {
                log.error("冻结商户异常: merchantId={}", fee.getMerchantId(), e);
            }
        }

        log.info("OnboardingExpireJob: 冻结商户 {} 家", frozenCount);
    }

    /**
     * 发送到期提醒（30天/7天/1天前）
     * 预留：后续对接微信模板消息或订阅消息
     */
    private void sendExpirationReminders() {
        LocalDate today = LocalDate.now();
        int[] remindDays = {30, 7, 1};

        for (int days : remindDays) {
            LocalDate targetDate = today.plusDays(days);
            List<MerchantOnboardingFee> list = feeMapper.selectByExpireDate(targetDate);

            for (MerchantOnboardingFee fee : list) {
                // TODO: 生成 tb_equity_reminder 记录 + 发送订阅消息
                log.info("入驻费到期提醒（{}天后）: merchantId={}, validEndDate={}",
                        days, fee.getMerchantId(), fee.getValidEndDate());
            }
        }
    }
}
