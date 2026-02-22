package com.wsh.matching.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.domain.entity.Activity;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import com.wsh.domain.mapper.ActivityMapper;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import com.wsh.integration.adapter.AdapterFactory;
import com.wsh.integration.adapter.MerchantDataAdapter;
import com.wsh.integration.adapter.dto.ActivityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动匹配服务
 * 根据用户在各商户的会员状态（活跃/沉睡），筛选展示对应的专属活动
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityMatchingService {

    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final ActivityMapper activityMapper;
    private final AdapterFactory adapterFactory;

    /**
     * 为用户匹配可参与的活动
     * 遍历用户关联的所有商户，根据会员状态筛选活动
     *
     * @param userId 平台用户ID
     * @return 用户可参与的活动列表
     */
    public List<Activity> matchActivitiesForUser(Long userId) {
        // 1. 获取用户在所有商户的会员快照
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectByUserId(userId);

        List<Activity> matchedActivities = new ArrayList<>();

        for (MerchantMemberSnapshot snapshot : snapshots) {
            try {
                List<Activity> activities = matchActivitiesForMerchant(
                        snapshot.getMerchantId(), snapshot.getDormancyLevel());
                matchedActivities.addAll(activities);
            } catch (Exception e) {
                log.error("匹配商户{}活动时异常: {}", snapshot.getMerchantId(), e.getMessage(), e);
            }
        }

        log.debug("用户{}活动匹配完成: 共匹配到{}个活动", userId, matchedActivities.size());
        return matchedActivities;
    }

    /**
     * 根据会员沉睡等级筛选商户的活动
     */
    private List<Activity> matchActivitiesForMerchant(Long merchantId, Integer dormancyLevel) {
        // 查询该商户所有进行中的活动
        List<Activity> allActivities = activityMapper.selectActiveByMerchantId(merchantId);

        List<Activity> matched = new ArrayList<>();
        boolean isDormant = dormancyLevel != null && dormancyLevel > 0;

        for (Activity activity : allActivities) {
            int target = activity.getTargetMemberType() != null ? activity.getTargetMemberType() : 0;

            if (target == Constants.TARGET_MEMBER_ALL) {
                // 全部会员可见
                matched.add(activity);
            } else if (target == Constants.TARGET_MEMBER_ACTIVE && !isDormant) {
                // 仅活跃会员可见，且用户是活跃会员
                matched.add(activity);
            } else if (target == Constants.TARGET_MEMBER_DORMANT && isDormant) {
                // 仅沉睡会员可见，且用户是沉睡会员
                matched.add(activity);
            }
        }

        return matched;
    }

    /**
     * 从商户系统同步活动到平台
     */
    @Transactional
    public int syncActivitiesFromMerchant(Long merchantId) {
        MerchantDataAdapter adapter = adapterFactory.getAdapter(merchantId);
        List<ActivityDTO> activityDTOs = adapter.syncActivities(merchantId);

        if (activityDTOs == null || activityDTOs.isEmpty()) {
            return 0;
        }

        int syncCount = 0;
        for (ActivityDTO dto : activityDTOs) {
            try {
                saveOrUpdateActivity(merchantId, dto);
                syncCount++;
            } catch (Exception e) {
                log.error("同步商户{}活动{}异常: {}", merchantId, dto.getSourceActivityId(), e.getMessage(), e);
            }
        }

        log.info("商户{}活动同步完成: 同步{}个活动", merchantId, syncCount);
        return syncCount;
    }

    private void saveOrUpdateActivity(Long merchantId, ActivityDTO dto) {
        // 按 merchantId + sourceActivityId 查询是否已存在
        Activity existing = null;
        if (dto.getSourceActivityId() != null) {
            existing = activityMapper.selectOne(
                    new LambdaQueryWrapper<Activity>()
                            .eq(Activity::getMerchantId, merchantId)
                            .eq(Activity::getSourceActivityId, dto.getSourceActivityId()));
        }

        if (existing != null) {
            // 更新
            fillActivityFromDTO(existing, dto);
            existing.setSyncTime(LocalDateTime.now());
            activityMapper.updateById(existing);
        } else {
            // 新建
            Activity activity = new Activity();
            activity.setMerchantId(merchantId);
            activity.setSourceActivityId(dto.getSourceActivityId());
            fillActivityFromDTO(activity, dto);
            activity.setStatus(1); // 进行中
            activity.setSyncSource(Constants.SYNC_SOURCE_API);
            activity.setSyncTime(LocalDateTime.now());
            activityMapper.insert(activity);
        }
    }

    private void fillActivityFromDTO(Activity activity, ActivityDTO dto) {
        activity.setActivityType(dto.getActivityType());
        activity.setActivityName(dto.getActivityName());
        activity.setActivityDesc(dto.getActivityDesc());
        activity.setCoverImage(dto.getCoverImage());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setConfig(dto.getConfig());
        activity.setStock(dto.getStock());
        activity.setSoldCount(dto.getSoldCount());
        activity.setTargetMemberType(dto.getTargetMemberType() != null ? dto.getTargetMemberType() : 0);
        activity.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 0);
        activity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
    }
}
