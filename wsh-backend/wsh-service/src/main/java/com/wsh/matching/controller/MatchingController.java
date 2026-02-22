package com.wsh.matching.controller;

import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.domain.entity.Activity;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import com.wsh.domain.entity.User;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import com.wsh.domain.mapper.UserMapper;
import com.wsh.matching.dto.ActivityResponse;
import com.wsh.matching.dto.MemberMatchResponse;
import com.wsh.matching.dto.MerchantMemberResponse;
import com.wsh.matching.service.ActivityMatchingService;
import com.wsh.matching.service.MemberMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "03-会员匹配")
@RestController
@RequestMapping("/v1/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MemberMatchingService memberMatchingService;
    private final ActivityMatchingService activityMatchingService;
    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Operation(summary = "触发会员匹配", description = "用当前用户手机号去所有商户系统匹配会员身份")
    @PostMapping("/trigger")
    public R<MemberMatchResponse> triggerMatching() {
        Long userId = SecurityUtil.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            return R.fail(400, "请先授权手机号");
        }

        // 执行匹配
        List<Long> matchedIds = memberMatchingService.matchAllMerchants(userId, user.getPhone());

        // 匹配活动
        List<Activity> activities = activityMatchingService.matchActivitiesForUser(userId);

        return R.ok(MemberMatchResponse.builder()
                .matchedCount(matchedIds.size())
                .matchedMerchantIds(matchedIds)
                .activityCount(activities.size())
                .build());
    }

    @Operation(summary = "我的会员列表", description = "获取当前用户在所有商户的会员快照")
    @GetMapping("/members")
    public R<List<MerchantMemberResponse>> myMembers() {
        Long userId = SecurityUtil.getUserId();
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectByUserId(userId);

        // 批量查询商户信息
        List<Long> merchantIds = snapshots.stream()
                .map(MerchantMemberSnapshot::getMerchantId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Merchant> merchantMap = merchantIds.isEmpty()
                ? Map.of()
                : merchantMapper.selectBatchIds(merchantIds).stream()
                    .collect(Collectors.toMap(Merchant::getMerchantId, m -> m));

        List<MerchantMemberResponse> responses = snapshots.stream()
                .map(s -> buildMemberResponse(s, merchantMap.get(s.getMerchantId())))
                .collect(Collectors.toList());

        return R.ok(responses);
    }

    @Operation(summary = "专属活动列表", description = "获取当前用户匹配到的所有专属活动")
    @GetMapping("/activities")
    public R<List<ActivityResponse>> myActivities() {
        Long userId = SecurityUtil.getUserId();
        List<Activity> activities = activityMatchingService.matchActivitiesForUser(userId);

        // 批量查询商户信息
        List<Long> merchantIds = activities.stream()
                .map(Activity::getMerchantId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Merchant> merchantMap = merchantIds.isEmpty()
                ? Map.of()
                : merchantMapper.selectBatchIds(merchantIds).stream()
                    .collect(Collectors.toMap(Merchant::getMerchantId, m -> m));

        List<ActivityResponse> responses = activities.stream()
                .map(a -> buildActivityResponse(a, merchantMap.get(a.getMerchantId())))
                .collect(Collectors.toList());

        return R.ok(responses);
    }

    // ==================== 私有方法 ====================

    private MerchantMemberResponse buildMemberResponse(MerchantMemberSnapshot s, Merchant m) {
        return MerchantMemberResponse.builder()
                .snapshotId(s.getSnapshotId())
                .userId(s.getUserId())
                .merchantId(s.getMerchantId())
                .merchantName(m != null ? m.getMerchantName() : null)
                .merchantLogoUrl(m != null ? m.getLogoUrl() : null)
                .sourceMemberId(s.getSourceMemberId())
                .memberLevelName(s.getMemberLevelName())
                .points(s.getPoints())
                .pointsValue(s.getPointsValue())
                .pointsExpireDate(s.getPointsExpireDate())
                .balance(s.getBalance())
                .consumeCount(s.getConsumeCount())
                .totalConsumeAmount(s.getTotalConsumeAmount())
                .lastConsumeTime(s.getLastConsumeTime())
                .dormancyLevel(s.getDormancyLevel())
                .dormancyDesc(getDormancyDesc(s.getDormancyLevel()))
                .syncTime(s.getSyncTime())
                .build();
    }

    private ActivityResponse buildActivityResponse(Activity a, Merchant m) {
        return ActivityResponse.builder()
                .activityId(a.getActivityId())
                .merchantId(a.getMerchantId())
                .merchantName(m != null ? m.getMerchantName() : null)
                .activityType(a.getActivityType())
                .activityName(a.getActivityName())
                .activityDesc(a.getActivityDesc())
                .coverImage(a.getCoverImage())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .config(a.getConfig())
                .stock(a.getStock())
                .soldCount(a.getSoldCount())
                .targetMemberType(a.getTargetMemberType())
                .isPublic(a.getIsPublic())
                .build();
    }

    private String getDormancyDesc(Integer level) {
        if (level == null) return "未知";
        return switch (level) {
            case 0 -> "活跃";
            case 1 -> "轻度沉睡";
            case 2 -> "中度沉睡";
            case 3 -> "深度沉睡";
            default -> "未知";
        };
    }
}
