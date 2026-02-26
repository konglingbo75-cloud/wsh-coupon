package com.wsh.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.domain.entity.Activity;
import com.wsh.domain.entity.GroupBuyConfig;
import com.wsh.domain.entity.MerchantEmployee;
import com.wsh.domain.mapper.ActivityMapper;
import com.wsh.domain.mapper.GroupBuyConfigMapper;
import com.wsh.domain.mapper.MerchantEmployeeMapper;
import com.wsh.domain.mapper.ServiceFeeRecordMapper;
import com.wsh.domain.mapper.VerificationRecordMapper;
import com.wsh.merchant.dto.ActivityCreateRequest;
import com.wsh.merchant.dto.MerchantActivityDetailResponse;
import com.wsh.merchant.dto.MerchantActivityListResponse;
import com.wsh.merchant.dto.MerchantActivityListResponse.ActivityListItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户活动管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantActivityService {

    private final ActivityMapper activityMapper;
    private final GroupBuyConfigMapper groupBuyConfigMapper;
    private final MerchantEmployeeMapper merchantEmployeeMapper;
    private final VerificationRecordMapper verificationRecordMapper;
    private final ServiceFeeRecordMapper serviceFeeRecordMapper;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 创建活动
     */
    @Transactional
    public MerchantActivityDetailResponse createActivity(ActivityCreateRequest request) {
        Long merchantId = resolveCurrentMerchantId();

        Activity activity = new Activity();
        activity.setMerchantId(merchantId);
        activity.setActivityName(request.getTitle());
        activity.setActivityType(request.getActivityType());
        activity.setCoverImage(request.getCoverUrl());
        activity.setActivityDesc(request.getDescription());
        activity.setStock(request.getStock());
        activity.setSoldCount(0);
        activity.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()) ? 1 : 0);
        activity.setTargetMemberType(Constants.TARGET_MEMBER_ALL);
        activity.setSyncSource(Constants.SYNC_SOURCE_MANUAL);
        activity.setSortOrder(0);

        // 解析时间
        if (StringUtils.hasText(request.getStartTime())) {
            activity.setStartTime(LocalDate.parse(request.getStartTime(), DATE_FMT).atStartOfDay());
        }
        if (StringUtils.hasText(request.getEndTime())) {
            activity.setEndTime(LocalDate.parse(request.getEndTime(), DATE_FMT).atTime(23, 59, 59));
        }

        // 状态
        activity.setStatus("active".equals(request.getStatus())
                ? Constants.ACTIVITY_STATUS_ACTIVE
                : Constants.ACTIVITY_STATUS_DRAFT);

        // 序列化配置（含 price / originalPrice + typeConfig）
        activity.setConfig(buildConfigJson(request));

        activityMapper.insert(activity);
        log.info("商户创建活动: merchantId={}, activityId={}", merchantId, activity.getActivityId());

        // 团购活动自动创建拼团配置
        if (activity.getActivityType() == Constants.ACTIVITY_TYPE_GROUP) {
            saveGroupBuyConfig(activity.getActivityId(), request.getTypeConfig());
        }

        return toDetailResponse(activity);
    }

    /**
     * 更新活动
     */
    @Transactional
    public MerchantActivityDetailResponse updateActivity(Long activityId, ActivityCreateRequest request) {
        Long merchantId = resolveCurrentMerchantId();

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !activity.getMerchantId().equals(merchantId)) {
            throw new BusinessException(404, "活动不存在");
        }

        activity.setActivityName(request.getTitle());
        activity.setActivityType(request.getActivityType());
        activity.setCoverImage(request.getCoverUrl());
        activity.setActivityDesc(request.getDescription());
        activity.setStock(request.getStock());
        activity.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()) ? 1 : 0);

        if (StringUtils.hasText(request.getStartTime())) {
            activity.setStartTime(LocalDate.parse(request.getStartTime(), DATE_FMT).atStartOfDay());
        }
        if (StringUtils.hasText(request.getEndTime())) {
            activity.setEndTime(LocalDate.parse(request.getEndTime(), DATE_FMT).atTime(23, 59, 59));
        }

        // 允许从草稿发布
        if ("active".equals(request.getStatus()) && activity.getStatus() == Constants.ACTIVITY_STATUS_DRAFT) {
            activity.setStatus(Constants.ACTIVITY_STATUS_ACTIVE);
        } else if ("draft".equals(request.getStatus())) {
            activity.setStatus(Constants.ACTIVITY_STATUS_DRAFT);
        }

        activity.setConfig(buildConfigJson(request));

        activityMapper.updateById(activity);
        log.info("商户更新活动: merchantId={}, activityId={}", merchantId, activityId);

        // 团购活动更新拼团配置
        if (activity.getActivityType() == Constants.ACTIVITY_TYPE_GROUP) {
            saveGroupBuyConfig(activityId, request.getTypeConfig());
        }

        return toDetailResponse(activity);
    }

    /**
     * 获取活动详情（编辑用）
     */
    public MerchantActivityDetailResponse getActivityDetail(Long activityId) {
        Long merchantId = resolveCurrentMerchantId();

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !activity.getMerchantId().equals(merchantId)) {
            throw new BusinessException(404, "活动不存在");
        }

        return toDetailResponse(activity);
    }

    /**
     * 查询商户活动列表
     */
    public MerchantActivityListResponse listActivities(Integer status, String keyword, int page, int pageSize) {
        Long merchantId = resolveCurrentMerchantId();

        // 查询全部（用于统计各状态数量）
        List<Activity> allActivities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getMerchantId, merchantId)
                        .orderByDesc(Activity::getCreatedAt));

        // 状态统计
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        statusCounts.put("all", allActivities.size());
        statusCounts.put("draft", (int) allActivities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_DRAFT).count());
        statusCounts.put("active", (int) allActivities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_ACTIVE).count());
        statusCounts.put("paused", (int) allActivities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_PAUSED).count());
        statusCounts.put("ended", (int) allActivities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_ENDED).count());

        // 应用筛选
        List<Activity> filtered = allActivities;
        if (status != null) {
            filtered = filtered.stream().filter(a -> a.getStatus().equals(status)).collect(Collectors.toList());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.toLowerCase();
            filtered = filtered.stream()
                    .filter(a -> a.getActivityName() != null && a.getActivityName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        // 分页
        int fromIndex = Math.min((page - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<Activity> pageList = filtered.subList(fromIndex, toIndex);

        List<ActivityListItem> items = pageList.stream().map(this::toListItem).collect(Collectors.toList());

        return MerchantActivityListResponse.builder()
                .list(items)
                .statusCounts(statusCounts)
                .build();
    }

    /**
     * 发布活动
     */
    @Transactional
    public void publishActivity(Long activityId) {
        Long merchantId = resolveCurrentMerchantId();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !activity.getMerchantId().equals(merchantId)) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() != Constants.ACTIVITY_STATUS_DRAFT) {
            throw new BusinessException(400, "只有草稿状态的活动可以发布");
        }
        activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getActivityId, activityId)
                .set(Activity::getStatus, Constants.ACTIVITY_STATUS_ACTIVE));
    }

    /**
     * 暂停活动
     */
    @Transactional
    public void pauseActivity(Long activityId) {
        Long merchantId = resolveCurrentMerchantId();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !activity.getMerchantId().equals(merchantId)) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() != Constants.ACTIVITY_STATUS_ACTIVE) {
            throw new BusinessException(400, "只有进行中的活动可以暂停");
        }
        activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getActivityId, activityId)
                .set(Activity::getStatus, Constants.ACTIVITY_STATUS_PAUSED));
    }

    /**
     * 恢复活动
     */
    @Transactional
    public void resumeActivity(Long activityId) {
        Long merchantId = resolveCurrentMerchantId();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !activity.getMerchantId().equals(merchantId)) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() != Constants.ACTIVITY_STATUS_PAUSED) {
            throw new BusinessException(400, "只有已暂停的活动可以恢复");
        }
        activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getActivityId, activityId)
                .set(Activity::getStatus, Constants.ACTIVITY_STATUS_ACTIVE));
    }

    // ==================== 私有方法 ====================

    /**
     * 保存/更新拼团配置（创建或更新活动时调用）
     */
    private void saveGroupBuyConfig(Long activityId, Map<String, Object> typeConfig) {
        int groupSize = 3; // 默认成团人数
        int maxMembers = 10;
        int expireHours = 24;
        int autoRefund = 1;
        int allowSelfBuy = 0;

        if (typeConfig != null) {
            if (typeConfig.get("groupSize") instanceof Number) {
                groupSize = ((Number) typeConfig.get("groupSize")).intValue();
            }
            if (typeConfig.get("maxMembers") instanceof Number) {
                maxMembers = ((Number) typeConfig.get("maxMembers")).intValue();
            }
            if (typeConfig.get("expireHours") instanceof Number) {
                expireHours = ((Number) typeConfig.get("expireHours")).intValue();
            }
            if (typeConfig.get("autoRefund") instanceof Number) {
                autoRefund = ((Number) typeConfig.get("autoRefund")).intValue();
            }
            if (typeConfig.get("allowSelfBuy") instanceof Number) {
                allowSelfBuy = ((Number) typeConfig.get("allowSelfBuy")).intValue();
            }
        }

        // 确保 maxMembers >= groupSize
        if (maxMembers < groupSize) {
            maxMembers = groupSize;
        }

        // 查找是否已有配置
        GroupBuyConfig existing = groupBuyConfigMapper.selectOne(
                new LambdaQueryWrapper<GroupBuyConfig>()
                        .eq(GroupBuyConfig::getActivityId, activityId));

        if (existing != null) {
            existing.setMinMembers(groupSize);
            existing.setMaxMembers(maxMembers);
            existing.setExpireHours(expireHours);
            existing.setAutoRefund(autoRefund);
            existing.setAllowSelfBuy(allowSelfBuy);
            groupBuyConfigMapper.updateById(existing);
            log.info("更新拼团配置: activityId={}, minMembers={}", activityId, groupSize);
        } else {
            GroupBuyConfig config = new GroupBuyConfig();
            config.setActivityId(activityId);
            config.setMinMembers(groupSize);
            config.setMaxMembers(maxMembers);
            config.setExpireHours(expireHours);
            config.setAutoRefund(autoRefund);
            config.setAllowSelfBuy(allowSelfBuy);
            groupBuyConfigMapper.insert(config);
            log.info("创建拼团配置: activityId={}, minMembers={}", activityId, groupSize);
        }
    }

    /**
     * 获取当前登录商户的 merchantId
     */
    private Long resolveCurrentMerchantId() {
        Long merchantId = SecurityUtil.getMerchantId();
        if (merchantId != null) {
            return merchantId;
        }
        // 回退：通过 openid 查员工表
        String openid = SecurityUtil.getOpenid();
        if (openid == null) {
            throw new BusinessException(401, "请先登录");
        }
        MerchantEmployee employee = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getOpenid, openid)
                        .eq(MerchantEmployee::getStatus, 1)
                        .last("LIMIT 1"));
        if (employee == null) {
            throw new BusinessException(403, "您还未入驻商户");
        }
        return employee.getMerchantId();
    }

    /**
     * 构建 config JSON（合并 price / originalPrice / typeConfig）
     */
    private String buildConfigJson(ActivityCreateRequest request) {
        try {
            Map<String, Object> configMap = new LinkedHashMap<>();
            if (request.getPrice() != null) {
                configMap.put("price", request.getPrice());
            }
            if (request.getOriginalPrice() != null) {
                configMap.put("originalPrice", request.getOriginalPrice());
            }
            if (request.getTypeConfig() != null) {
                configMap.putAll(request.getTypeConfig());
            }
            return objectMapper.writeValueAsString(configMap);
        } catch (Exception e) {
            log.warn("序列化活动配置失败", e);
            return "{}";
        }
    }

    /**
     * 解析 config JSON
     */
    private Map<String, Object> parseConfig(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析活动配置失败: {}", configJson, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Activity → MerchantActivityDetailResponse
     */
    private MerchantActivityDetailResponse toDetailResponse(Activity a) {
        Map<String, Object> config = parseConfig(a.getConfig());

        // 提取 price / originalPrice
        BigDecimal price = toBigDecimal(config.remove("price"));
        BigDecimal originalPrice = toBigDecimal(config.remove("originalPrice"));

        // 剩余的就是 typeConfig
        Map<String, Object> typeConfig = config.isEmpty() ? null : config;

        return MerchantActivityDetailResponse.builder()
                .activityId(a.getActivityId())
                .title(a.getActivityName())
                .activityType(a.getActivityType())
                .coverUrl(a.getCoverImage())
                .price(price)
                .originalPrice(originalPrice)
                .stock(a.getStock())
                .soldCount(a.getSoldCount())
                .description(a.getActivityDesc())
                .startTime(a.getStartTime() != null ? a.getStartTime().format(DATE_FMT) : null)
                .endTime(a.getEndTime() != null ? a.getEndTime().format(DATE_FMT) : null)
                .isPublic(a.getIsPublic() != null && a.getIsPublic() == 1)
                .status(a.getStatus())
                .typeConfig(typeConfig)
                .build();
    }

    /**
     * Activity → ActivityListItem
     */
    private ActivityListItem toListItem(Activity a) {
        Map<String, Object> config = parseConfig(a.getConfig());
        BigDecimal price = toBigDecimal(config.get("price"));
        BigDecimal originalPrice = toBigDecimal(config.get("originalPrice"));

        int remainStock = -1;
        if (a.getStock() != null && a.getStock() >= 0) {
            remainStock = a.getStock() - (a.getSoldCount() != null ? a.getSoldCount() : 0);
            if (remainStock < 0) remainStock = 0;
        }

        return ActivityListItem.builder()
                .id(a.getActivityId())
                .title(a.getActivityName())
                .type(a.getActivityType())
                .coverUrl(a.getCoverImage())
                .price(price)
                .originalPrice(originalPrice)
                .status(a.getStatus())
                .soldCount(a.getSoldCount() != null ? a.getSoldCount() : 0)
                .remainStock(remainStock)
                .verifiedCount(verificationRecordMapper.countByActivityId(a.getActivityId()))
                .revenue(serviceFeeRecordMapper.sumRevenueByActivityId(a.getActivityId()))
                .validStart(a.getStartTime() != null ? a.getStartTime().format(DATE_FMT) : null)
                .validEnd(a.getEndTime() != null ? a.getEndTime().format(DATE_FMT) : null)
                .build();
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
