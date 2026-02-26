package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员拼团服务
 */
@Service
@RequiredArgsConstructor
public class AdminGroupOrderService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupParticipantMapper participantMapper;
    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    /**
     * 查询拼团列表
     */
    public PageResult<GroupOrderItem> getGroupOrders(Integer page, Integer size, String keyword, Integer status, Long activityId) {
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<GroupOrder>()
                .eq(status != null, GroupOrder::getStatus, status)
                .eq(activityId != null, GroupOrder::getActivityId, activityId)
                .orderByDesc(GroupOrder::getCreatedAt);

        List<GroupOrder> allOrders = groupOrderMapper.selectList(wrapper);

        // 关键字过滤（拼团编号/活动名称）
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            allOrders = allOrders.stream()
                    .filter(o -> {
                        if (o.getGroupNo().toLowerCase().contains(kw)) return true;
                        Activity activity = activityMapper.selectById(o.getActivityId());
                        return activity != null && activity.getActivityName().toLowerCase().contains(kw);
                    })
                    .collect(Collectors.toList());
        }

        int total = allOrders.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<GroupOrder> pagedOrders = start < total ? allOrders.subList(start, end) : Collections.emptyList();

        List<GroupOrderItem> items = pagedOrders.stream()
                .map(this::convertToItem)
                .collect(Collectors.toList());

        return PageResult.of(items, total, page, size);
    }

    /**
     * 查询拼团详情
     */
    public GroupOrderDetail getGroupOrderDetail(Long groupOrderId) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) {
            return null;
        }

        GroupOrderItem item = convertToItem(groupOrder);

        // 获取参与者列表
        List<GroupParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<GroupParticipant>()
                        .eq(GroupParticipant::getGroupOrderId, groupOrderId)
                        .orderByAsc(GroupParticipant::getJoinTime)
        );

        List<ParticipantItem> participantItems = participants.stream()
                .map(p -> {
                    User user = userMapper.selectById(p.getUserId());
                    Order order = p.getOrderId() != null ? orderMapper.selectById(p.getOrderId()) : null;
                    return ParticipantItem.builder()
                            .participantId(p.getParticipantId())
                            .userId(p.getUserId())
                            .nickname(user != null ? user.getNickname() : "用户" + p.getUserId())
                            .avatarUrl(user != null ? user.getAvatarUrl() : null)
                            .isInitiator(p.getIsInitiator() == 1)
                            .orderId(p.getOrderId())
                            .orderStatus(order != null ? order.getStatus() : null)
                            .joinTime(p.getJoinTime())
                            .build();
                })
                .collect(Collectors.toList());

        return GroupOrderDetail.builder()
                .groupOrderId(item.getGroupOrderId())
                .groupNo(item.getGroupNo())
                .activityId(item.getActivityId())
                .activityName(item.getActivityName())
                .merchantName(item.getMerchantName())
                .initiatorUserId(item.getInitiatorUserId())
                .initiatorNickname(item.getInitiatorNickname())
                .requiredMembers(item.getRequiredMembers())
                .currentMembers(item.getCurrentMembers())
                .status(item.getStatus())
                .statusName(item.getStatusName())
                .expireTime(item.getExpireTime())
                .completeTime(item.getCompleteTime())
                .createdAt(item.getCreatedAt())
                .participants(participantItems)
                .build();
    }

    private GroupOrderItem convertToItem(GroupOrder groupOrder) {
        Activity activity = activityMapper.selectById(groupOrder.getActivityId());
        Merchant merchant = activity != null ? merchantMapper.selectById(activity.getMerchantId()) : null;
        User initiator = userMapper.selectById(groupOrder.getInitiatorUserId());

        return GroupOrderItem.builder()
                .groupOrderId(groupOrder.getGroupOrderId())
                .groupNo(groupOrder.getGroupNo())
                .activityId(groupOrder.getActivityId())
                .activityName(activity != null ? activity.getActivityName() : null)
                .merchantName(merchant != null ? merchant.getMerchantName() : null)
                .initiatorUserId(groupOrder.getInitiatorUserId())
                .initiatorNickname(initiator != null ? initiator.getNickname() : "用户" + groupOrder.getInitiatorUserId())
                .requiredMembers(groupOrder.getRequiredMembers())
                .currentMembers(groupOrder.getCurrentMembers())
                .status(groupOrder.getStatus())
                .statusName(getStatusName(groupOrder.getStatus()))
                .expireTime(groupOrder.getExpireTime())
                .completeTime(groupOrder.getCompleteTime())
                .createdAt(groupOrder.getCreatedAt())
                .build();
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "拼团中";
            case 1 -> "已成团";
            case 2 -> "已失败";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    @Data
    @Builder
    public static class GroupOrderItem {
        private Long groupOrderId;
        private String groupNo;
        private Long activityId;
        private String activityName;
        private String merchantName;
        private Long initiatorUserId;
        private String initiatorNickname;
        private Integer requiredMembers;
        private Integer currentMembers;
        private Integer status;
        private String statusName;
        private LocalDateTime expireTime;
        private LocalDateTime completeTime;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class GroupOrderDetail {
        private Long groupOrderId;
        private String groupNo;
        private Long activityId;
        private String activityName;
        private String merchantName;
        private Long initiatorUserId;
        private String initiatorNickname;
        private Integer requiredMembers;
        private Integer currentMembers;
        private Integer status;
        private String statusName;
        private LocalDateTime expireTime;
        private LocalDateTime completeTime;
        private LocalDateTime createdAt;
        private List<ParticipantItem> participants;
    }

    @Data
    @Builder
    public static class ParticipantItem {
        private Long participantId;
        private Long userId;
        private String nickname;
        private String avatarUrl;
        private Boolean isInitiator;
        private Long orderId;
        private Integer orderStatus;
        private LocalDateTime joinTime;
    }
}
