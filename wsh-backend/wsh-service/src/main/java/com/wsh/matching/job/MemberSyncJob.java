package com.wsh.matching.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import com.wsh.domain.entity.User;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import com.wsh.domain.mapper.UserMapper;
import com.wsh.matching.service.MemberMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会员数据定时同步任务
 * 每日凌晨3:00，对所有已匹配的会员快照执行数据刷新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSyncJob {

    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final UserMapper userMapper;
    private final MemberMatchingService memberMatchingService;
    private final RedisUtil redisUtil;

    @Scheduled(cron = "0 0 3 * * ?")
    public void syncAllMembers() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisUtil.tryLock(Constants.LOCK_MEMBER_SYNC, lockValue, 30, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(locked)) {
            log.info("会员同步任务已在其他实例执行，跳过");
            return;
        }

        try {
            log.info("===== 会员数据定时同步开始 =====");
            doSync();
            log.info("===== 会员数据定时同步完成 =====");
        } catch (Exception e) {
            log.error("会员数据同步异常: {}", e.getMessage(), e);
        } finally {
            redisUtil.releaseLock(Constants.LOCK_MEMBER_SYNC, lockValue);
        }
    }

    private void doSync() {
        // 查询所有已成功同步的快照（按用户分组）
        List<MerchantMemberSnapshot> allSnapshots = snapshotMapper.selectList(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                        .eq(MerchantMemberSnapshot::getSyncStatus, 1));

        // 按 userId 分组
        var userGrouped = allSnapshots.stream()
                .collect(Collectors.groupingBy(MerchantMemberSnapshot::getUserId));

        int successCount = 0;
        int failCount = 0;

        for (var entry : userGrouped.entrySet()) {
            Long userId = entry.getKey();
            List<MerchantMemberSnapshot> snapshots = entry.getValue();

            for (MerchantMemberSnapshot snapshot : snapshots) {
                try {
                    memberMatchingService.syncMemberData(userId, snapshot.getMerchantId());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.warn("同步用户{}在商户{}的数据失败: {}",
                            userId, snapshot.getMerchantId(), e.getMessage());
                }
            }
        }

        log.info("会员数据同步统计: 总用户数={}, 总快照数={}, 成功={}, 失败={}",
                userGrouped.size(), allSnapshots.size(), successCount, failCount);
    }
}
