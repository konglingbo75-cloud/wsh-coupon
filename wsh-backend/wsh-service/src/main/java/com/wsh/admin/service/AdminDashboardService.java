package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.admin.dto.DashboardStatsResponse;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final OrderMapper orderMapper;
    private final VoucherMapper voucherMapper;
    private final MerchantOnboardingFeeMapper onboardingFeeMapper;
    private final ServiceFeeRecordMapper serviceFeeRecordMapper;

    public DashboardStatsResponse getStats() {
        // 商户统计
        Long totalMerchants = merchantMapper.selectCount(null);
        Long pendingMerchants = merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getStatus, 0));
        Long activeMerchants = merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getStatus, 1));
        Long frozenMerchants = merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getStatus, 2));

        // 用户统计
        Long totalUsers = userMapper.selectCount(null);

        // 活动统计
        Long totalActivities = activityMapper.selectCount(null);
        Long activeActivities = activityMapper.selectCount(
                new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 1));

        // 订单统计
        Long totalOrders = orderMapper.selectCount(null);
        List<Order> paidOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        BigDecimal totalOrderAmount = paidOrders.stream()
                .map(Order::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 核销统计
        Long verifiedVouchers = voucherMapper.selectCount(
                new LambdaQueryWrapper<Voucher>().eq(Voucher::getStatus, 1));

        // 入驻费收入
        List<MerchantOnboardingFee> paidFees = onboardingFeeMapper.selectList(
                new LambdaQueryWrapper<MerchantOnboardingFee>().eq(MerchantOnboardingFee::getPayStatus, 1));
        BigDecimal totalOnboardingFee = paidFees.stream()
                .map(MerchantOnboardingFee::getFeeAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 服务费收入
        List<ServiceFeeRecord> feeRecords = serviceFeeRecordMapper.selectList(null);
        BigDecimal totalServiceFee = feeRecords.stream()
                .map(ServiceFeeRecord::getServiceFee)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStatsResponse.builder()
                .totalMerchants(totalMerchants)
                .pendingMerchants(pendingMerchants)
                .activeMerchants(activeMerchants)
                .frozenMerchants(frozenMerchants)
                .totalUsers(totalUsers)
                .totalActivities(totalActivities)
                .activeActivities(activeActivities)
                .totalOrders(totalOrders)
                .totalOrderAmount(totalOrderAmount)
                .verifiedVouchers(verifiedVouchers)
                .totalOnboardingFee(totalOnboardingFee)
                .totalServiceFee(totalServiceFee)
                .build();
    }
}
