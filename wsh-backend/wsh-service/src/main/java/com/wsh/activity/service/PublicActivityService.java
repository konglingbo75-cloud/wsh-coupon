package com.wsh.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.activity.dto.ActivityDetailResponse;
import com.wsh.activity.dto.PublicActivityResponse;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.Activity;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.mapper.ActivityMapper;
import com.wsh.domain.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 公开活动服务
 * 提供同城活动广场等无需登录的公开活动查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicActivityService {

    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;
    private final RedisUtil redisUtil;

    private static final int CACHE_EXPIRE_MINUTES = 5;

    /**
     * 获取同城公开活动（按类型分组）
     * 此接口无需登录
     */
    public PublicActivityResponse getPublicActivitiesByCity(String city) {
        // 尝试从缓存获取
        String cacheKey = Constants.CACHE_PUBLIC_ACTIVITY + city;
        PublicActivityResponse cached = (PublicActivityResponse) redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取公开活动: city={}", city);
            return cached;
        }

        // 查询该城市的公开活动
        List<Activity> activities = activityMapper.selectPublicActivitiesByCity(city);

        // 加载商户信息
        Map<Long, Merchant> merchantMap = loadMerchantMap(activities);

        // 按类型分组
        Map<Integer, List<Activity>> activitiesByType = activities.stream()
                .collect(Collectors.groupingBy(a -> 
                        a.getActivityType() != null ? a.getActivityType() : 0));

        // 转换为响应
        List<ActivityDetailResponse> voucherList = convertList(
                activitiesByType.getOrDefault(Constants.ACTIVITY_TYPE_VOUCHER, Collections.emptyList()),
                merchantMap);
        List<ActivityDetailResponse> depositList = convertList(
                activitiesByType.getOrDefault(Constants.ACTIVITY_TYPE_DEPOSIT, Collections.emptyList()),
                merchantMap);
        List<ActivityDetailResponse> pointsList = convertList(
                activitiesByType.getOrDefault(Constants.ACTIVITY_TYPE_POINTS, Collections.emptyList()),
                merchantMap);
        List<ActivityDetailResponse> groupList = convertList(
                activitiesByType.getOrDefault(Constants.ACTIVITY_TYPE_GROUP, Collections.emptyList()),
                merchantMap);

        PublicActivityResponse.ActivityTypeCount typeCount = PublicActivityResponse.ActivityTypeCount.builder()
                .voucher(voucherList.size())
                .deposit(depositList.size())
                .points(pointsList.size())
                .group(groupList.size())
                .total(activities.size())
                .build();

        PublicActivityResponse response = PublicActivityResponse.builder()
                .city(city)
                .voucherActivities(voucherList)
                .depositActivities(depositList)
                .pointsActivities(pointsList)
                .groupActivities(groupList)
                .typeCount(typeCount)
                .build();

        // 写入缓存
        redisUtil.set(cacheKey, response, CACHE_EXPIRE_MINUTES * 60L, TimeUnit.SECONDS);
        log.debug("公开活动查询完成: city={}, total={}", city, activities.size());

        return response;
    }

    /**
     * 按类型获取公开活动列表
     */
    public List<ActivityDetailResponse> getPublicActivitiesByType(String city, Integer activityType) {
        List<Activity> activities;
        if (activityType != null) {
            activities = activityMapper.selectPublicActivitiesByCityAndType(city, activityType);
        } else {
            activities = activityMapper.selectPublicActivitiesByCity(city);
        }

        Map<Long, Merchant> merchantMap = loadMerchantMap(activities);
        return convertList(activities, merchantMap);
    }

    /**
     * 获取同城入驻商户列表（公开）
     */
    public List<MerchantPublicInfo> getPublicMerchants(String city) {
        List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getCity, city)
                        .eq(Merchant::getStatus, Constants.MERCHANT_STATUS_ACTIVE)
                        .orderByDesc(Merchant::getCreatedAt));

        return merchants.stream()
                .map(m -> MerchantPublicInfo.builder()
                        .merchantId(m.getMerchantId())
                        .merchantName(m.getMerchantName())
                        .logoUrl(m.getLogoUrl())
                        .address(m.getAddress())
                        .businessCategory(m.getBusinessCategory())
                        .longitude(m.getLongitude())
                        .latitude(m.getLatitude())
                        .activityCount(activityMapper.countActiveByMerchantId(m.getMerchantId()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 获取活动详情（公开）
     */
    public ActivityDetailResponse getPublicActivityDetail(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return null;
        }

        // 检查是否公开
        if (activity.getIsPublic() == null || activity.getIsPublic() != 1) {
            log.warn("尝试访问非公开活动: activityId={}", activityId);
            return null;
        }

        Merchant merchant = merchantMapper.selectById(activity.getMerchantId());
        return convertToResponse(activity, merchant);
    }

    /**
     * 清除城市活动缓存
     */
    public void clearCityCache(String city) {
        String cacheKey = Constants.CACHE_PUBLIC_ACTIVITY + city;
        redisUtil.delete(cacheKey);
        log.info("已清除城市活动缓存: {}", city);
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

    private List<ActivityDetailResponse> convertList(List<Activity> activities, 
                                                      Map<Long, Merchant> merchantMap) {
        return activities.stream()
                .map(a -> convertToResponse(a, merchantMap.get(a.getMerchantId())))
                .collect(Collectors.toList());
    }

    private ActivityDetailResponse convertToResponse(Activity activity, Merchant merchant) {
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
                .targetMemberType(activity.getTargetMemberType())
                .isPublic(activity.getIsPublic())
                .isExclusive(false)
                .exclusiveTip(null)
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

    /**
     * 商户公开信息
     */
    @lombok.Data
    @lombok.Builder
    public static class MerchantPublicInfo {
        private Long merchantId;
        private String merchantName;
        private String logoUrl;
        private String address;
        private String businessCategory;
        private java.math.BigDecimal longitude;
        private java.math.BigDecimal latitude;
        private Integer activityCount;
    }
}
