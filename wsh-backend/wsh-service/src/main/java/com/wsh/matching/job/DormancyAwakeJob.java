package com.wsh.matching.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.util.IdGenerator;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 沉睡会员唤醒定时任务
 * 每天凌晨04:00执行：
 * 1. 扫描所有沉睡会员（dormancyLevel >= 1）
 * 2. 查找商户针对沉睡会员的唤醒活动
 * 3. 为符合条件的沉睡会员发放唤醒券
 * 4. 生成唤醒提醒记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DormancyAwakeJob {

    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final ActivityMapper activityMapper;
    private final VoucherMapper voucherMapper;
    private final EquityReminderMapper reminderMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    private static final String LOCK_KEY = "lock:dormancy_awake";

    /**
     * 每天凌晨04:00执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void execute() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisUtil.tryLock(LOCK_KEY, lockValue, 60, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(locked)) {
            log.info("DormancyAwakeJob: 未获取到锁，跳过执行");
            return;
        }

        try {
            log.info("DormancyAwakeJob: 开始沉睡会员唤醒扫描");

            // 查询所有沉睡会员（dormancyLevel >= 1）
            List<MerchantMemberSnapshot> dormantMembers = snapshotMapper.selectList(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                    .ge(MerchantMemberSnapshot::getDormancyLevel, Constants.DORMANCY_LIGHT)
                    .eq(MerchantMemberSnapshot::getSyncStatus, 1)
            );

            if (dormantMembers.isEmpty()) {
                log.info("DormancyAwakeJob: 无沉睡会员");
                return;
            }

            log.info("DormancyAwakeJob: 发现{}个沉睡会员", dormantMembers.size());

            int awakeVoucherCount = 0;
            int reminderCount = 0;

            for (MerchantMemberSnapshot snapshot : dormantMembers) {
                try {
                    // 检查商户是否有针对沉睡会员的唤醒活动
                    List<Activity> awakeActivities = activityMapper.selectList(
                        new LambdaQueryWrapper<Activity>()
                            .eq(Activity::getMerchantId, snapshot.getMerchantId())
                            .eq(Activity::getTargetMemberType, Constants.TARGET_MEMBER_DORMANT)
                            .eq(Activity::getStatus, Constants.ACTIVITY_STATUS_ACTIVE)
                            .lt(Activity::getStartTime, LocalDateTime.now())
                            .gt(Activity::getEndTime, LocalDateTime.now())
                    );

                    if (awakeActivities.isEmpty()) {
                        continue;
                    }

                    // 选择第一个可用的唤醒活动
                    Activity activity = awakeActivities.get(0);

                    // 检查是否已发过该活动的唤醒券（避免重复发放）
                    Voucher existingVoucher = voucherMapper.selectOne(
                        new LambdaQueryWrapper<Voucher>()
                            .eq(Voucher::getUserId, snapshot.getUserId())
                            .eq(Voucher::getActivityId, activity.getActivityId())
                            .eq(Voucher::getVoucherType, 4) // 4-沉睡唤醒券
                    );

                    if (existingVoucher != null) {
                        continue; // 已发过，跳过
                    }

                    // 发放唤醒券
                    boolean issued = issueAwakeVoucher(snapshot, activity);
                    if (issued) {
                        awakeVoucherCount++;
                    }

                    // 生成唤醒提醒
                    createAwakeReminder(snapshot, activity);
                    reminderCount++;

                } catch (Exception e) {
                    log.error("处理沉睡会员唤醒异常: userId={}, merchantId={}",
                        snapshot.getUserId(), snapshot.getMerchantId(), e);
                }
            }

            log.info("DormancyAwakeJob: 执行完成，发放唤醒券{}张，生成提醒{}条",
                awakeVoucherCount, reminderCount);

        } catch (Exception e) {
            log.error("DormancyAwakeJob: 执行异常", e);
        } finally {
            redisUtil.releaseLock(LOCK_KEY, lockValue);
        }
    }

    /**
     * 发放唤醒券
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean issueAwakeVoucher(MerchantMemberSnapshot snapshot, Activity activity) {
        // 检查库存
        if (activity.getStock() != null && activity.getStock() > 0) {
            if (activity.getSoldCount() >= activity.getStock()) {
                log.warn("唤醒活动库存不足: activityId={}", activity.getActivityId());
                return false;
            }
        }

        // 创建唤醒券
        Voucher voucher = new Voucher();
        voucher.setVoucherId(IdGenerator.nextId());
        voucher.setVoucherCode(IdGenerator.nextVoucherCode());
        voucher.setOrderId(0L); // 无订单，系统自动发放
        voucher.setUserId(snapshot.getUserId());
        voucher.setMerchantId(snapshot.getMerchantId());
        voucher.setActivityId(activity.getActivityId());
        voucher.setVoucherType(4); // 4-沉睡唤醒券
        voucher.setVoucherValue(getActivityValue(activity));
        voucher.setStatus(Constants.VOUCHER_STATUS_UNUSED);
        voucher.setValidStartTime(LocalDateTime.now());
        voucher.setValidEndTime(activity.getEndTime());
        voucherMapper.insert(voucher);

        // 更新活动已售数量
        activity.setSoldCount(activity.getSoldCount() + 1);
        activityMapper.updateById(activity);

        log.info("沉睡唤醒券发放成功: userId={}, merchantId={}, voucherCode={}",
            snapshot.getUserId(), snapshot.getMerchantId(), voucher.getVoucherCode());

        return true;
    }

    /**
     * 创建唤醒提醒记录
     */
    private void createAwakeReminder(MerchantMemberSnapshot snapshot, Activity activity) {
        EquityReminder reminder = new EquityReminder();
        reminder.setReminderId(IdGenerator.nextId());
        reminder.setUserId(snapshot.getUserId());
        reminder.setMerchantId(snapshot.getMerchantId());
        reminder.setReminderType(Constants.REMINDER_TYPE_DORMANCY_AWAKE);
        reminder.setEquityType("awake_voucher");
        reminder.setEquityValue(getActivityValue(activity));
        reminder.setRemindStatus(0); // 0-待发送
        reminderMapper.insert(reminder);
    }

    /**
     * 获取活动价值
     */
    private BigDecimal getActivityValue(Activity activity) {
        // 从活动config中获取价值，简化处理返回默认值
        // 实际应解析activity.config JSON
        return BigDecimal.valueOf(10); // 默认10元唤醒券
    }
}
