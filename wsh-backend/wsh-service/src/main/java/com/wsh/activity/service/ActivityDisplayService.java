package com.wsh.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.activity.dto.*;
import com.wsh.common.core.constant.Constants;
import com.wsh.domain.entity.Activity;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import com.wsh.domain.mapper.ActivityMapper;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动展示服务
 * 负责根据用户会员状态筛选展示对应的活动
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityDisplayService {

    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantMemberSnapshotMapper snapshotMapper;

    /**
     * 获取活动详情
     */
    public ActivityDetailResponse getActivityDetail(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return null;
        }

        Merchant merchant = merchantMapper.selectById(activity.getMerchantId());
        MerchantMemberSnapshot snapshot = null;
        if (userId != null) {
            snapshot = snapshotMapper.selectOne(
                    new LambdaQueryWrapper<MerchantMemberSnapshot>()
                            .eq(MerchantMemberSnapshot::getUserId, userId)
                            .eq(MerchantMemberSnapshot::getMerchantId, activity.getMerchantId()));
        }

        return convertToResponse(activity, merchant, snapshot);
    }

    /**
     * 查询活动列表（支持多种筛选条件）
     */
    public ActivityListResponse queryActivities(ActivityQueryRequest request, Long userId) {
        List<Activity> activities;

        if (request.getMerchantId() != null) {
            // 按商户查询
            if (request.getActivityType() != null) {
                activities = activityMapper.selectActiveByMerchantIdAndType(
                        request.getMerchantId(), request.getActivityType());
            } else {
                activities = activityMapper.selectActiveByMerchantId(request.getMerchantId());
            }
        } else if (request.getCity() != null) {
            // 按城市查询
            if (request.getActivityType() != null) {
                activities = activityMapper.selectPublicActivitiesByCityAndType(
                        request.getCity(), request.getActivityType());
            } else {
                activities = activityMapper.selectPublicActivitiesByCity(request.getCity());
            }
        } else {
            // 查询所有公开活动
            activities = activityMapper.selectPublicActivities();
        }

        // 如果用户已登录，根据会员状态筛选
        if (userId != null && Boolean.TRUE.equals(request.getExclusiveOnly())) {
            activities = filterByMemberStatus(activities, userId);
        }

        // 加载商户信息
        Map<Long, Merchant> merchantMap = loadMerchantMap(activities);
        Map<Long, MerchantMemberSnapshot> snapshotMap = userId != null ? 
                loadSnapshotMap(userId, activities) : Collections.emptyMap();

        // 转换为响应对象
        List<ActivityDetailResponse> responseList = activities.stream()
                .map(a -> convertToResponse(a, merchantMap.get(a.getMerchantId()), 
                        snapshotMap.get(a.getMerchantId())))
                .collect(Collectors.toList());

        // 分页
        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        int total = responseList.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<ActivityDetailResponse> pagedList = fromIndex < total ?
                responseList.subList(fromIndex, toIndex) : Collections.emptyList();

        return ActivityListResponse.builder()
                .activities(pagedList)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 获取用户的专属活动列表
     */
    public ExclusiveActivityResponse getExclusiveActivities(Long userId) {
        // 获取用户在所有商户的会员快照
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectByUserId(userId);
        if (snapshots.isEmpty()) {
            return ExclusiveActivityResponse.builder()
                    .activities(Collections.emptyList())
                    .total(0)
                    .dormantExclusiveCount(0)
                    .activeExclusiveCount(0)
                    .hasDormantMembership(false)
                    .dormantMerchantCount(0)
                    .build();
        }

        // 统计沉睡会员商户数
        long dormantCount = snapshots.stream()
                .filter(s -> s.getDormancyLevel() != null && s.getDormancyLevel() > 0)
                .count();

        // 获取所有相关商户的活动
        List<Long> merchantIds = snapshots.stream()
                .map(MerchantMemberSnapshot::getMerchantId)
                .collect(Collectors.toList());
        List<Activity> allActivities = activityMapper.selectActiveByMerchantIds(merchantIds);

        // 根据会员状态筛选专属活动
        Map<Long, Integer> merchantDormancyMap = snapshots.stream()
                .collect(Collectors.toMap(
                        MerchantMemberSnapshot::getMerchantId,
                        s -> s.getDormancyLevel() != null ? s.getDormancyLevel() : 0));

        List<Activity> exclusiveActivities = new ArrayList<>();
        int dormantExclusive = 0;
        int activeExclusive = 0;

        for (Activity activity : allActivities) {
            int targetType = activity.getTargetMemberType() != null ? activity.getTargetMemberType() : 0;
            int dormancyLevel = merchantDormancyMap.getOrDefault(activity.getMerchantId(), 0);
            boolean isDormant = dormancyLevel > 0;

            // 只筛选专属活动（非全部会员可见的）
            if (targetType == Constants.TARGET_MEMBER_DORMANT && isDormant) {
                exclusiveActivities.add(activity);
                dormantExclusive++;
            } else if (targetType == Constants.TARGET_MEMBER_ACTIVE && !isDormant) {
                exclusiveActivities.add(activity);
                activeExclusive++;
            }
        }

        // 加载商户信息
        Map<Long, Merchant> merchantMap = loadMerchantMap(exclusiveActivities);
        Map<Long, MerchantMemberSnapshot> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(MerchantMemberSnapshot::getMerchantId, s -> s));

        List<ActivityDetailResponse> responseList = exclusiveActivities.stream()
                .map(a -> convertToResponse(a, merchantMap.get(a.getMerchantId()),
                        snapshotMap.get(a.getMerchantId())))
                .collect(Collectors.toList());

        return ExclusiveActivityResponse.builder()
                .activities(responseList)
                .total(responseList.size())
                .dormantExclusiveCount(dormantExclusive)
                .activeExclusiveCount(activeExclusive)
                .hasDormantMembership(dormantCount > 0)
                .dormantMerchantCount((int) dormantCount)
                .build();
    }

    /**
     * 获取附近商户的活动
     */
    public NearbyActivityResponse getNearbyActivities(BigDecimal longitude, BigDecimal latitude, 
                                                       Integer distanceKm, Long userId) {
        int searchDistance = distanceKm != null ? distanceKm : 5;
        
        // 查询附近的商户
        List<Merchant> nearbyMerchants = findNearbyMerchants(longitude, latitude, searchDistance);
        if (nearbyMerchants.isEmpty()) {
            return NearbyActivityResponse.builder()
                    .activities(Collections.emptyList())
                    .total(0)
                    .distanceKm(searchDistance)
                    .build();
        }

        // 获取这些商户的活动
        List<Long> merchantIds = nearbyMerchants.stream()
                .map(Merchant::getMerchantId)
                .collect(Collectors.toList());
        List<Activity> activities = activityMapper.selectActiveByMerchantIds(merchantIds);

        // 加载用户会员快照
        Map<Long, MerchantMemberSnapshot> snapshotMap = userId != null ?
                loadSnapshotMap(userId, activities) : Collections.emptyMap();

        // 转换为响应，附带距离信息
        Map<Long, Merchant> merchantMap = nearbyMerchants.stream()
                .collect(Collectors.toMap(Merchant::getMerchantId, m -> m));

        List<NearbyActivityResponse.NearbyActivityItem> items = new ArrayList<>();
        for (Activity activity : activities) {
            Merchant merchant = merchantMap.get(activity.getMerchantId());
            if (merchant == null) continue;

            int distance = calculateDistance(longitude, latitude, 
                    merchant.getLongitude(), merchant.getLatitude());

            ActivityDetailResponse detail = convertToResponse(activity, merchant,
                    snapshotMap.get(activity.getMerchantId()));

            items.add(NearbyActivityResponse.NearbyActivityItem.builder()
                    .activity(detail)
                    .distanceMeters(distance)
                    .merchantAddress(merchant.getAddress())
                    .longitude(merchant.getLongitude())
                    .latitude(merchant.getLatitude())
                    .build());
        }

        // 按距离排序
        items.sort(Comparator.comparingInt(NearbyActivityResponse.NearbyActivityItem::getDistanceMeters));

        return NearbyActivityResponse.builder()
                .activities(items)
                .total(items.size())
                .distanceKm(searchDistance)
                .build();
    }

    /**
     * 根据用户会员状态筛选活动
     */
    private List<Activity> filterByMemberStatus(List<Activity> activities, Long userId) {
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectByUserId(userId);
        Map<Long, Integer> merchantDormancyMap = snapshots.stream()
                .collect(Collectors.toMap(
                        MerchantMemberSnapshot::getMerchantId,
                        s -> s.getDormancyLevel() != null ? s.getDormancyLevel() : 0));

        return activities.stream()
                .filter(activity -> {
                    int targetType = activity.getTargetMemberType() != null ? 
                            activity.getTargetMemberType() : 0;
                    if (targetType == Constants.TARGET_MEMBER_ALL) {
                        return true;
                    }
                    Integer dormancy = merchantDormancyMap.get(activity.getMerchantId());
                    if (dormancy == null) {
                        // 非会员只能看公开活动
                        return activity.getIsPublic() != null && activity.getIsPublic() == 1;
                    }
                    boolean isDormant = dormancy > 0;
                    if (targetType == Constants.TARGET_MEMBER_DORMANT) {
                        return isDormant;
                    } else if (targetType == Constants.TARGET_MEMBER_ACTIVE) {
                        return !isDormant;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查找附近商户
     */
    private List<Merchant> findNearbyMerchants(BigDecimal longitude, BigDecimal latitude, int distanceKm) {
        // 简化实现：查询所有正常状态的商户，然后在内存中筛选距离
        List<Merchant> allMerchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, Constants.MERCHANT_STATUS_ACTIVE)
                        .isNotNull(Merchant::getLongitude)
                        .isNotNull(Merchant::getLatitude));

        int maxDistanceMeters = distanceKm * 1000;
        return allMerchants.stream()
                .filter(m -> {
                    int distance = calculateDistance(longitude, latitude, m.getLongitude(), m.getLatitude());
                    return distance <= maxDistanceMeters;
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算两点之间的距离（米）
     * 使用Haversine公式
     */
    private int calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) {
            return Integer.MAX_VALUE;
        }

        double earthRadius = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (earthRadius * c);
    }

    private Map<Long, Merchant> loadMerchantMap(List<Activity> activities) {
        if (activities.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> merchantIds = activities.stream()
                .map(Activity::getMerchantId)
                .collect(Collectors.toSet());
        List<Merchant> merchants = merchantMapper.selectBatchIds(merchantIds);
        return merchants.stream()
                .collect(Collectors.toMap(Merchant::getMerchantId, m -> m));
    }

    private Map<Long, MerchantMemberSnapshot> loadSnapshotMap(Long userId, List<Activity> activities) {
        if (activities.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> merchantIds = activities.stream()
                .map(Activity::getMerchantId)
                .collect(Collectors.toSet());
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectList(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                        .eq(MerchantMemberSnapshot::getUserId, userId)
                        .in(MerchantMemberSnapshot::getMerchantId, merchantIds));
        return snapshots.stream()
                .collect(Collectors.toMap(MerchantMemberSnapshot::getMerchantId, s -> s));
    }

    private ActivityDetailResponse convertToResponse(Activity activity, Merchant merchant,
                                                      MerchantMemberSnapshot snapshot) {
        boolean isExclusive = false;
        String exclusiveTip = null;
        int targetType = activity.getTargetMemberType() != null ? activity.getTargetMemberType() : 0;

        if (snapshot != null && targetType != Constants.TARGET_MEMBER_ALL) {
            boolean isDormant = snapshot.getDormancyLevel() != null && snapshot.getDormancyLevel() > 0;
            if (targetType == Constants.TARGET_MEMBER_DORMANT && isDormant) {
                isExclusive = true;
                exclusiveTip = "您是" + (merchant != null ? merchant.getMerchantName() : "该店") + 
                        "的老会员，这是为您准备的专属优惠";
            } else if (targetType == Constants.TARGET_MEMBER_ACTIVE && !isDormant) {
                isExclusive = true;
                exclusiveTip = "活跃会员专属优惠";
            }
        }

        boolean hasStock = activity.getStock() == null || activity.getStock() < 0 
                || (activity.getSoldCount() != null && activity.getStock() > activity.getSoldCount());

        return ActivityDetailResponse.builder()
                .activityId(activity.getActivityId())
                .merchantId(activity.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : null)
                .merchantLogo(merchant != null ? merchant.getLogoUrl() : null)
                .activityType(activity.getActivityType())
                .activityTypeName(getActivityTypeName(activity.getActivityType()))
                .activityName(activity.getActivityName())
                .activityDesc(activity.getActivityDesc())
                .coverImage(activity.getCoverImage())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .config(activity.getConfig())
                .stock(activity.getStock())
                .soldCount(activity.getSoldCount())
                .targetMemberType(targetType)
                .isPublic(activity.getIsPublic())
                .isExclusive(isExclusive)
                .exclusiveTip(exclusiveTip)
                .status(activity.getStatus())
                .hasStock(hasStock)
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private String getActivityTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case Constants.ACTIVITY_TYPE_VOUCHER -> "代金券";
            case Constants.ACTIVITY_TYPE_DEPOSIT -> "储值充值";
            case Constants.ACTIVITY_TYPE_POINTS -> "积分兑换";
            case Constants.ACTIVITY_TYPE_GROUP -> "团购";
            default -> "未知";
        };
    }
}
