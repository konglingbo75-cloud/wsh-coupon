package com.wsh.integration.adapter.impl;

import com.wsh.common.core.constant.Constants;
import com.wsh.domain.entity.MerchantBranch;
import com.wsh.domain.entity.User;
import com.wsh.domain.mapper.MerchantBranchMapper;
import com.wsh.domain.mapper.UserMapper;
import com.wsh.integration.adapter.MerchantDataAdapter;
import com.wsh.integration.adapter.dto.ActivityDTO;
import com.wsh.integration.adapter.dto.ConsumeRecordDTO;
import com.wsh.integration.adapter.dto.MemberDataDTO;
import com.wsh.integration.adapter.dto.MemberMatchResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Demo 模式适配器（本地开发 / 演示环境使用）
 * 返回模拟的会员匹配数据，便于在无真实商户系统时测试完整业务流程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoAdapter implements MerchantDataAdapter {

    private final UserMapper userMapper;
    private final MerchantBranchMapper branchMapper;

    /** 支持的测试手机号 */
    private static final List<String> DEMO_PHONES = Arrays.asList(
            "13800138001", "13800138002", "13800138003"
    );

    @Override
    public MemberMatchResult matchMemberByPhone(String phone, Long merchantId) {
        log.info("Demo模式: 匹配商户{}会员, phone={}", merchantId, phone);

        if (!DEMO_PHONES.contains(phone)) {
            return MemberMatchResult.builder()
                    .matched(false)
                    .memberStatus("unknown")
                    .build();
        }

        // 根据手机号尾号决定会员属性
        String lastDigit = phone.substring(phone.length() - 1);
        String sourceMemberId = "DEMO_M" + merchantId + "_" + phone.substring(7);

        MemberDataDTO memberData;
        String memberStatus;

        switch (lastDigit) {
            case "1": // 13800138001 -> 活跃金卡会员
                memberData = MemberDataDTO.builder()
                        .sourceMemberId(sourceMemberId)
                        .memberLevelName("金卡会员")
                        .points(2500)
                        .pointsValue(new BigDecimal("25.00"))
                        .pointsExpireDate(LocalDate.now().plusDays(30))
                        .balance(new BigDecimal("388.50"))
                        .consumeCount(15)
                        .totalConsumeAmount(new BigDecimal("3280.00"))
                        .lastConsumeTime(LocalDateTime.now().minusDays(3))
                        .dormancyLevel(Constants.DORMANCY_ACTIVE)
                        .build();
                memberStatus = "active";
                break;
            case "2": // 13800138002 -> 中度沉睡银卡
                memberData = MemberDataDTO.builder()
                        .sourceMemberId(sourceMemberId)
                        .memberLevelName("银卡会员")
                        .points(800)
                        .pointsValue(new BigDecimal("8.00"))
                        .pointsExpireDate(LocalDate.now().plusDays(10))
                        .balance(BigDecimal.ZERO)
                        .consumeCount(5)
                        .totalConsumeAmount(new BigDecimal("580.00"))
                        .lastConsumeTime(LocalDateTime.now().minusDays(75))
                        .dormancyLevel(Constants.DORMANCY_MEDIUM)
                        .build();
                memberStatus = "dormant";
                break;
            case "3": // 13800138003 -> 轻度沉睡普通会员
                memberData = MemberDataDTO.builder()
                        .sourceMemberId(sourceMemberId)
                        .memberLevelName("普通会员")
                        .points(200)
                        .pointsValue(new BigDecimal("2.00"))
                        .pointsExpireDate(LocalDate.now().plusDays(60))
                        .balance(new BigDecimal("50.00"))
                        .consumeCount(3)
                        .totalConsumeAmount(new BigDecimal("150.00"))
                        .lastConsumeTime(LocalDateTime.now().minusDays(40))
                        .dormancyLevel(Constants.DORMANCY_LIGHT)
                        .build();
                memberStatus = "dormant";
                break;
            default:
                return MemberMatchResult.builder()
                        .matched(false)
                        .memberStatus("unknown")
                        .build();
        }

        log.info("Demo模式: 匹配成功 merchant={}, phone={}, level={}, dormancy={}",
                merchantId, phone, memberData.getMemberLevelName(), memberData.getDormancyLevel());

        return MemberMatchResult.builder()
                .matched(true)
                .sourceMemberId(sourceMemberId)
                .memberStatus(memberStatus)
                .memberData(memberData)
                .build();
    }

    @Override
    public MemberDataDTO syncMemberData(Long userId, Long merchantId) {
        log.info("Demo模式: 同步商户{}会员数据, userId={}", merchantId, userId);

        User user = userMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            return null;
        }

        MemberMatchResult result = matchMemberByPhone(user.getPhone(), merchantId);
        return result.isMatched() ? result.getMemberData() : null;
    }

    @Override
    public List<ConsumeRecordDTO> syncConsumeRecords(Long userId, Long merchantId) {
        log.info("Demo模式: 同步商户{}消费记录, userId={}", merchantId, userId);

        // 查询该商户的第一个门店
        MerchantBranch branch = branchMapper.selectOne(
                new LambdaQueryWrapper<MerchantBranch>()
                        .eq(MerchantBranch::getMerchantId, merchantId)
                        .last("LIMIT 1"));
        Long branchId = branch != null ? branch.getBranchId() : null;

        List<ConsumeRecordDTO> records = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        records.add(ConsumeRecordDTO.builder()
                .sourceOrderNo("DEMO_" + merchantId + "_" + userId + "_001")
                .branchId(branchId)
                .consumeTime(now.minusDays(5).withHour(12).withMinute(30))
                .consumeAmount(new BigDecimal("128.00"))
                .invoiceStatus(1)
                .invoiceNo("INV20260220001")
                .build());

        records.add(ConsumeRecordDTO.builder()
                .sourceOrderNo("DEMO_" + merchantId + "_" + userId + "_002")
                .branchId(branchId)
                .consumeTime(now.minusDays(12).withHour(18).withMinute(0))
                .consumeAmount(new BigDecimal("98.00"))
                .invoiceStatus(0)
                .build());

        records.add(ConsumeRecordDTO.builder()
                .sourceOrderNo("DEMO_" + merchantId + "_" + userId + "_003")
                .branchId(branchId)
                .consumeTime(now.minusDays(25).withHour(19).withMinute(30))
                .consumeAmount(new BigDecimal("256.00"))
                .invoiceStatus(1)
                .invoiceNo("INV20260201001")
                .build());

        return records;
    }

    @Override
    public List<ActivityDTO> syncActivities(Long merchantId) {
        log.info("Demo模式: 同步商户{}活动列表", merchantId);

        LocalDateTime now = LocalDateTime.now();
        List<ActivityDTO> activities = new ArrayList<>();

        // 模拟一个面向全部会员的代金券活动
        activities.add(ActivityDTO.builder()
                .sourceActivityId("DEMO_ACT_" + merchantId + "_001")
                .activityType(Constants.ACTIVITY_TYPE_VOUCHER)
                .activityName("新春特惠8折券")
                .activityDesc("消费满200元可使用，立减40元")
                .startTime(now.minusDays(10))
                .endTime(now.plusDays(30))
                .config("{\"voucher_value\":40,\"min_consume\":200,\"valid_days\":30}")
                .stock(100)
                .soldCount(20)
                .targetMemberType(Constants.TARGET_MEMBER_ALL)
                .isPublic(1)
                .sortOrder(1)
                .build());

        // 模拟一个面向沉睡会员的唤醒活动
        activities.add(ActivityDTO.builder()
                .sourceActivityId("DEMO_ACT_" + merchantId + "_002")
                .activityType(Constants.ACTIVITY_TYPE_VOUCHER)
                .activityName("老会员回归5折券")
                .activityDesc("沉睡会员专属，半价享美食")
                .startTime(now.minusDays(5))
                .endTime(now.plusDays(60))
                .config("{\"voucher_value\":50,\"selling_price\":25,\"valid_days\":14}")
                .stock(50)
                .soldCount(5)
                .targetMemberType(Constants.TARGET_MEMBER_DORMANT)
                .isPublic(0)
                .sortOrder(2)
                .build());

        return activities;
    }

    @Override
    public String getType() {
        return Constants.SYNC_SOURCE_DEMO;
    }
}
