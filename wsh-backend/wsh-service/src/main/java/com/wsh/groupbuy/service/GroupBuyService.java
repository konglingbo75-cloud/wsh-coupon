package com.wsh.groupbuy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.util.IdGenerator;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import com.wsh.groupbuy.dto.*;
import com.wsh.integration.wechat.WechatPayService;
import com.wsh.order.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 拼团服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupParticipantMapper participantMapper;
    private final GroupBuyConfigMapper configMapper;
    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final WechatPayService wechatPayService;
    private final RedisUtil redisUtil;

    private static final String GROUP_LOCK_PREFIX = "group:lock:";
    private static final String STOCK_KEY_PREFIX = "activity:stock:";

    /**
     * 发起拼团
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupDetailResponse initiateGroup(Long activityId, Long userId) {
        // 1. 校验活动
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getActivityType() != Constants.ACTIVITY_TYPE_GROUP) {
            throw new BusinessException("该活动不是拼团活动");
        }
        validateActivity(activity);

        // 2. 获取拼团配置
        GroupBuyConfig config = getGroupConfig(activityId);
        if (config == null) {
            throw new BusinessException("拼团配置不存在");
        }

        // 3. 检查用户是否有进行中的拼团
        GroupOrder existingGroup = findUserPendingGroup(activityId, userId);
        if (existingGroup != null) {
            throw new BusinessException("您已有进行中的拼团，请勿重复发起");
        }

        // 4. 扣减库存
        if (!deductStock(activityId, activity.getStock())) {
            throw new BusinessException("活动库存不足");
        }

        try {
            // 5. 创建拼团订单
            GroupOrder groupOrder = new GroupOrder();
            groupOrder.setGroupOrderId(IdGenerator.nextId());
            groupOrder.setGroupNo(generateGroupNo());
            groupOrder.setActivityId(activityId);
            groupOrder.setInitiatorUserId(userId);
            groupOrder.setRequiredMembers(config.getMinMembers());
            groupOrder.setCurrentMembers(1);
            groupOrder.setStatus(GroupOrder.STATUS_PENDING);
            groupOrder.setExpireTime(LocalDateTime.now().plusHours(config.getExpireHours()));
            groupOrder.setCreatedAt(LocalDateTime.now());
            groupOrder.setUpdatedAt(LocalDateTime.now());

            groupOrderMapper.insert(groupOrder);

            // 6. 添加发起人为参与者
            GroupParticipant participant = new GroupParticipant();
            participant.setParticipantId(IdGenerator.nextId());
            participant.setGroupOrderId(groupOrder.getGroupOrderId());
            participant.setUserId(userId);
            participant.setOrderId(null); // 订单在支付后创建
            participant.setIsInitiator(1);
            participant.setJoinTime(LocalDateTime.now());
            participant.setCreatedAt(LocalDateTime.now());

            participantMapper.insert(participant);

            log.info("拼团发起成功: groupNo={}, userId={}, activityId={}", groupOrder.getGroupNo(), userId, activityId);

            // 7. 返回详情
            return getGroupDetail(groupOrder.getGroupOrderId(), userId);

        } catch (Exception e) {
            // 回滚库存
            rollbackStock(activityId);
            throw e;
        }
    }

    /**
     * 参与拼团
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupDetailResponse joinGroup(Long groupOrderId, Long userId) {
        // 1. 获取分布式锁防止并发
        String lockKey = GROUP_LOCK_PREFIX + groupOrderId;
        boolean locked = Boolean.TRUE.equals(redisUtil.tryLock(lockKey, "1", 30, TimeUnit.SECONDS));
        if (!locked) {
            throw new BusinessException("操作太频繁，请稍后重试");
        }

        try {
            // 2. 查询拼团
            GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
            if (groupOrder == null) {
                throw new BusinessException("拼团不存在");
            }

            // 3. 校验拼团状态
            if (groupOrder.getStatus() != GroupOrder.STATUS_PENDING) {
                throw new BusinessException("该拼团已结束");
            }
            if (groupOrder.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("该拼团已过期");
            }
            if (groupOrder.getCurrentMembers() >= groupOrder.getRequiredMembers()) {
                throw new BusinessException("该拼团人数已满");
            }

            // 4. 检查用户是否已参与
            GroupParticipant existing = participantMapper.selectOne(
                    new LambdaQueryWrapper<GroupParticipant>()
                            .eq(GroupParticipant::getGroupOrderId, groupOrderId)
                            .eq(GroupParticipant::getUserId, userId)
            );
            if (existing != null) {
                throw new BusinessException("您已参与该拼团");
            }

            // 5. 校验活动
            Activity activity = activityMapper.selectById(groupOrder.getActivityId());
            if (activity == null) {
                throw new BusinessException("活动不存在");
            }
            validateActivity(activity);

            // 6. 扣减库存
            if (!deductStock(activity.getActivityId(), activity.getStock())) {
                throw new BusinessException("活动库存不足");
            }

            try {
                // 7. 添加参与者
                GroupParticipant participant = new GroupParticipant();
                participant.setParticipantId(IdGenerator.nextId());
                participant.setGroupOrderId(groupOrderId);
                participant.setUserId(userId);
                participant.setOrderId(null);
                participant.setIsInitiator(0);
                participant.setJoinTime(LocalDateTime.now());
                participant.setCreatedAt(LocalDateTime.now());

                participantMapper.insert(participant);

                // 8. 更新拼团人数
                int newCount = groupOrder.getCurrentMembers() + 1;
                groupOrder.setCurrentMembers(newCount);
                groupOrder.setUpdatedAt(LocalDateTime.now());

                // 9. 检查是否成团
                if (newCount >= groupOrder.getRequiredMembers()) {
                    groupOrder.setStatus(GroupOrder.STATUS_SUCCESS);
                    groupOrder.setCompleteTime(LocalDateTime.now());
                    log.info("拼团成功: groupNo={}", groupOrder.getGroupNo());
                }

                groupOrderMapper.updateById(groupOrder);

                log.info("参与拼团成功: groupNo={}, userId={}, currentMembers={}", 
                        groupOrder.getGroupNo(), userId, newCount);

                return getGroupDetail(groupOrderId, userId);

            } catch (Exception e) {
                rollbackStock(activity.getActivityId());
                throw e;
            }

        } finally {
            redisUtil.delete(lockKey);
        }
    }

    /**
     * 获取拼团详情
     */
    public GroupDetailResponse getGroupDetail(Long groupOrderId, Long userId) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) {
            throw new BusinessException("拼团不存在");
        }

        Activity activity = activityMapper.selectById(groupOrder.getActivityId());
        Merchant merchant = activity != null ? merchantMapper.selectById(activity.getMerchantId()) : null;

        // 获取参与者列表
        List<GroupParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<GroupParticipant>()
                        .eq(GroupParticipant::getGroupOrderId, groupOrderId)
                        .orderByAsc(GroupParticipant::getJoinTime)
        );

        // 获取发起人信息
        User initiator = userMapper.selectById(groupOrder.getInitiatorUserId());

        // 构建参与者列表
        List<GroupDetailResponse.ParticipantItem> participantItems = participants.stream()
                .map(p -> {
                    User user = userMapper.selectById(p.getUserId());
                    return GroupDetailResponse.ParticipantItem.builder()
                            .userId(p.getUserId())
                            .nickname(user != null ? user.getNickname() : "用户" + p.getUserId())
                            .avatarUrl(user != null ? user.getAvatarUrl() : null)
                            .isInitiator(p.getIsInitiator() == 1)
                            .joinTime(p.getJoinTime())
                            .build();
                })
                .collect(Collectors.toList());

        // 检查当前用户是否已参与
        boolean hasJoined = participants.stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        // 计算剩余时间
        long remainingSeconds = 0;
        if (groupOrder.getStatus() == GroupOrder.STATUS_PENDING && 
            groupOrder.getExpireTime().isAfter(LocalDateTime.now())) {
            remainingSeconds = Duration.between(LocalDateTime.now(), groupOrder.getExpireTime()).getSeconds();
        }

        // 解析价格
        BigDecimal groupPrice = BigDecimal.ZERO;
        BigDecimal originalPrice = BigDecimal.ZERO;
        if (activity != null && activity.getConfig() != null) {
            groupPrice = extractDecimal(activity.getConfig(), "group_price");
            originalPrice = extractDecimal(activity.getConfig(), "original_price");
        }

        return GroupDetailResponse.builder()
                .groupOrderId(groupOrder.getGroupOrderId())
                .groupNo(groupOrder.getGroupNo())
                .activityId(groupOrder.getActivityId())
                .activityName(activity != null ? activity.getActivityName() : null)
                .coverImage(activity != null ? activity.getCoverImage() : null)
                .merchantId(activity != null ? activity.getMerchantId() : null)
                .merchantName(merchant != null ? merchant.getMerchantName() : null)
                .groupPrice(groupPrice)
                .originalPrice(originalPrice)
                .requiredMembers(groupOrder.getRequiredMembers())
                .currentMembers(groupOrder.getCurrentMembers())
                .remainingMembers(groupOrder.getRequiredMembers() - groupOrder.getCurrentMembers())
                .status(groupOrder.getStatus())
                .statusName(getStatusName(groupOrder.getStatus()))
                .expireTime(groupOrder.getExpireTime())
                .remainingSeconds(remainingSeconds)
                .completeTime(groupOrder.getCompleteTime())
                .initiatorUserId(groupOrder.getInitiatorUserId())
                .initiatorNickname(initiator != null ? initiator.getNickname() : null)
                .initiatorAvatar(initiator != null ? initiator.getAvatarUrl() : null)
                .isInitiator(groupOrder.getInitiatorUserId().equals(userId))
                .hasJoined(hasJoined)
                .participants(participantItems)
                .createdAt(groupOrder.getCreatedAt())
                .build();
    }

    /**
     * 获取活动的可参与拼团列表
     */
    public List<GroupDetailResponse> getActivityGroups(Long activityId, Long userId) {
        List<GroupOrder> groups = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getActivityId, activityId)
                        .eq(GroupOrder::getStatus, GroupOrder.STATUS_PENDING)
                        .gt(GroupOrder::getExpireTime, LocalDateTime.now())
                        .orderByDesc(GroupOrder::getCreatedAt)
                        .last("LIMIT 10")
        );

        return groups.stream()
                .map(g -> getGroupDetail(g.getGroupOrderId(), userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的拼团列表
     */
    public GroupListResponse getUserGroups(Long userId, Integer status, Integer page, Integer pageSize) {
        // 查询用户参与的拼团ID
        List<GroupParticipant> participations = participantMapper.selectList(
                new LambdaQueryWrapper<GroupParticipant>()
                        .eq(GroupParticipant::getUserId, userId)
        );

        if (participations.isEmpty()) {
            return GroupListResponse.builder()
                    .groups(Collections.emptyList())
                    .total(0)
                    .page(page)
                    .pageSize(pageSize)
                    .build();
        }

        List<Long> groupOrderIds = participations.stream()
                .map(GroupParticipant::getGroupOrderId)
                .collect(Collectors.toList());

        // 查询拼团记录
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<GroupOrder>()
                .in(GroupOrder::getGroupOrderId, groupOrderIds)
                .eq(status != null, GroupOrder::getStatus, status)
                .orderByDesc(GroupOrder::getCreatedAt);

        List<GroupOrder> allGroups = groupOrderMapper.selectList(wrapper);
        int total = allGroups.size();

        // 分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<GroupOrder> pagedGroups = start < total ? allGroups.subList(start, end) : Collections.emptyList();

        // 转换响应
        List<GroupListResponse.GroupItem> items = pagedGroups.stream()
                .map(g -> convertToGroupItem(g, userId))
                .collect(Collectors.toList());

        return GroupListResponse.builder()
                .groups(items)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 发起拼团支付
     */
    public PaymentResponse requestGroupPayment(Long groupOrderId, Long userId, String openid) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) {
            throw new BusinessException("拼团不存在");
        }

        // 检查用户是否为参与者
        GroupParticipant participant = participantMapper.selectOne(
                new LambdaQueryWrapper<GroupParticipant>()
                        .eq(GroupParticipant::getGroupOrderId, groupOrderId)
                        .eq(GroupParticipant::getUserId, userId)
        );
        if (participant == null) {
            throw new BusinessException("您未参与该拼团");
        }
        if (participant.getOrderId() != null) {
            throw new BusinessException("您已支付，请勿重复支付");
        }

        // 获取活动信息
        Activity activity = activityMapper.selectById(groupOrder.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        // 计算支付金额
        BigDecimal payAmount = extractDecimal(activity.getConfig(), "group_price");
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("拼团价格配置错误");
        }

        // 创建订单
        String orderNo = generateOrderNo();
        Order order = createGroupOrder(groupOrder, activity, userId, payAmount, orderNo);

        // 更新参与者的订单ID
        participant.setOrderId(order.getOrderId());
        participantMapper.updateById(participant);

        // 调用微信支付
        return wechatPayService.createPayment(orderNo, payAmount, activity.getActivityName(), openid);
    }

    /**
     * 取消拼团（仅发起人可操作，且仅在无人参与时）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelGroup(Long groupOrderId, Long userId) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) {
            throw new BusinessException("拼团不存在");
        }
        if (!groupOrder.getInitiatorUserId().equals(userId)) {
            throw new BusinessException("仅发起人可取消拼团");
        }
        if (groupOrder.getStatus() != GroupOrder.STATUS_PENDING) {
            throw new BusinessException("该拼团已结束，无法取消");
        }
        if (groupOrder.getCurrentMembers() > 1) {
            throw new BusinessException("已有其他用户参与，无法取消");
        }

        // 更新状态
        groupOrder.setStatus(GroupOrder.STATUS_CANCELLED);
        groupOrder.setUpdatedAt(LocalDateTime.now());
        groupOrderMapper.updateById(groupOrder);

        // 回滚库存
        rollbackStock(groupOrder.getActivityId());

        log.info("拼团已取消: groupNo={}, userId={}", groupOrder.getGroupNo(), userId);
    }

    /**
     * 处理超时拼团
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleExpiredGroups() {
        // 查询超时的拼团
        List<GroupOrder> expiredGroups = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getStatus, GroupOrder.STATUS_PENDING)
                        .lt(GroupOrder::getExpireTime, LocalDateTime.now())
        );

        int processedCount = 0;
        for (GroupOrder groupOrder : expiredGroups) {
            try {
                processExpiredGroup(groupOrder);
                processedCount++;
            } catch (Exception e) {
                log.error("处理超时拼团失败: groupNo={}", groupOrder.getGroupNo(), e);
            }
        }

        return processedCount;
    }

    /**
     * 处理单个超时拼团
     */
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredGroup(GroupOrder groupOrder) {
        // 更新状态为失败
        groupOrder.setStatus(GroupOrder.STATUS_FAILED);
        groupOrder.setUpdatedAt(LocalDateTime.now());
        groupOrderMapper.updateById(groupOrder);

        // 获取拼团配置
        GroupBuyConfig config = getGroupConfig(groupOrder.getActivityId());
        boolean autoRefund = config != null && config.getAutoRefund() == 1;

        // 获取所有参与者
        List<GroupParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<GroupParticipant>()
                        .eq(GroupParticipant::getGroupOrderId, groupOrder.getGroupOrderId())
        );

        // 处理退款
        for (GroupParticipant participant : participants) {
            if (participant.getOrderId() != null && autoRefund) {
                try {
                    // 发起退款
                    Order order = orderMapper.selectById(participant.getOrderId());
                    if (order != null && order.getStatus() == Constants.ORDER_STATUS_PAID) {
                        wechatPayService.refund(order.getOrderNo(), order.getTransactionId(), 
                                order.getPayAmount(), "拼团失败自动退款");
                        orderMapper.updateStatus(order.getOrderId(), Constants.ORDER_STATUS_REFUNDED);
                        log.info("拼团退款成功: orderNo={}, userId={}", order.getOrderNo(), participant.getUserId());
                    }
                } catch (Exception e) {
                    log.error("拼团退款失败: orderId={}", participant.getOrderId(), e);
                }
            }

            // 回滚库存
            rollbackStock(groupOrder.getActivityId());
        }

        log.info("超时拼团处理完成: groupNo={}, participantCount={}", 
                groupOrder.getGroupNo(), participants.size());
    }

    // ==================== 私有方法 ====================

    private void validateActivity(Activity activity) {
        if (activity.getStatus() != 1) {
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动未开始");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束");
        }
    }

    private GroupBuyConfig getGroupConfig(Long activityId) {
        return configMapper.selectOne(
                new LambdaQueryWrapper<GroupBuyConfig>()
                        .eq(GroupBuyConfig::getActivityId, activityId)
        );
    }

    private GroupOrder findUserPendingGroup(Long activityId, Long userId) {
        // 查询用户在该活动中发起的进行中拼团
        return groupOrderMapper.selectOne(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getActivityId, activityId)
                        .eq(GroupOrder::getInitiatorUserId, userId)
                        .eq(GroupOrder::getStatus, GroupOrder.STATUS_PENDING)
        );
    }

    private boolean deductStock(Long activityId, Integer totalStock) {
        if (totalStock == null || totalStock < 0) {
            return true; // 无限库存
        }

        String stockKey = STOCK_KEY_PREFIX + activityId;

        // 确保缓存中有库存值
        if (redisUtil.get(stockKey) == null) {
            Activity activity = activityMapper.selectById(activityId);
            int sold = activity.getSoldCount() != null ? activity.getSoldCount() : 0;
            int currentStock = totalStock - sold;
            redisUtil.set(stockKey, currentStock, 3600L, TimeUnit.SECONDS);
        }

        // 原子扣减
        Long remaining = redisUtil.stockDecrement(stockKey, 1);
        return remaining != null && remaining >= 0;
    }

    private void rollbackStock(Long activityId) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        Object cached = redisUtil.get(stockKey);
        if (cached != null) {
            long currentStock = ((Number) cached).longValue();
            redisUtil.set(stockKey, currentStock + 1, 3600L, TimeUnit.SECONDS);
        }
    }

    private String generateGroupNo() {
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        String random = String.format("%04d", new Random().nextInt(10000));
        return "G" + timestamp + random;
    }

    private String generateOrderNo() {
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        String random = String.format("%06d", new Random().nextInt(1000000));
        return timestamp + random;
    }

    private Order createGroupOrder(GroupOrder groupOrder, Activity activity, Long userId, 
                                   BigDecimal payAmount, String orderNo) {
        Order order = new Order();
        order.setOrderId(IdGenerator.nextId());
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(activity.getMerchantId());
        order.setActivityId(activity.getActivityId());
        order.setOrderType(Constants.ACTIVITY_TYPE_GROUP);
        order.setOrderAmount(payAmount);
        order.setPayAmount(payAmount);
        order.setStatus(Constants.ORDER_STATUS_PENDING);
        order.setGroupOrderId(groupOrder.getGroupOrderId());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(order);
        return order;
    }

    private GroupListResponse.GroupItem convertToGroupItem(GroupOrder groupOrder, Long userId) {
        Activity activity = activityMapper.selectById(groupOrder.getActivityId());
        Merchant merchant = activity != null ? merchantMapper.selectById(activity.getMerchantId()) : null;

        long remainingSeconds = 0;
        if (groupOrder.getStatus() == GroupOrder.STATUS_PENDING &&
            groupOrder.getExpireTime().isAfter(LocalDateTime.now())) {
            remainingSeconds = Duration.between(LocalDateTime.now(), groupOrder.getExpireTime()).getSeconds();
        }

        BigDecimal groupPrice = BigDecimal.ZERO;
        if (activity != null && activity.getConfig() != null) {
            groupPrice = extractDecimal(activity.getConfig(), "group_price");
        }

        return GroupListResponse.GroupItem.builder()
                .groupOrderId(groupOrder.getGroupOrderId())
                .groupNo(groupOrder.getGroupNo())
                .activityId(groupOrder.getActivityId())
                .activityName(activity != null ? activity.getActivityName() : null)
                .coverImage(activity != null ? activity.getCoverImage() : null)
                .merchantName(merchant != null ? merchant.getMerchantName() : null)
                .groupPrice(groupPrice)
                .requiredMembers(groupOrder.getRequiredMembers())
                .currentMembers(groupOrder.getCurrentMembers())
                .status(groupOrder.getStatus())
                .statusName(getStatusName(groupOrder.getStatus()))
                .isInitiator(groupOrder.getInitiatorUserId().equals(userId))
                .expireTime(groupOrder.getExpireTime())
                .remainingSeconds(remainingSeconds)
                .createdAt(groupOrder.getCreatedAt())
                .build();
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case GroupOrder.STATUS_PENDING -> "拼团中";
            case GroupOrder.STATUS_SUCCESS -> "已成团";
            case GroupOrder.STATUS_FAILED -> "拼团失败";
            case GroupOrder.STATUS_CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    private BigDecimal extractDecimal(String json, String key) {
        if (json == null) return BigDecimal.ZERO;
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            keyIndex = json.indexOf(key);
        }
        if (keyIndex < 0) return BigDecimal.ZERO;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return BigDecimal.ZERO;

        StringBuilder sb = new StringBuilder();
        for (int i = colonIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                sb.append(c);
            } else if (sb.length() > 0) {
                break;
            }
        }

        return sb.length() > 0 ? new BigDecimal(sb.toString()) : BigDecimal.ZERO;
    }
}
