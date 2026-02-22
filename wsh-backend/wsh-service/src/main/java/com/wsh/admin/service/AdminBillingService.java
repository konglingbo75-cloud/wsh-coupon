package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.AdminBillingListResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantOnboardingFee;
import com.wsh.domain.entity.OnboardingFeePlan;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.MerchantOnboardingFeeMapper;
import com.wsh.domain.mapper.OnboardingFeePlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBillingService {

    private final MerchantOnboardingFeeMapper onboardingFeeMapper;
    private final MerchantMapper merchantMapper;
    private final OnboardingFeePlanMapper planMapper;

    public PageResult<AdminBillingListResponse> listBillings(PageQueryRequest query) {
        LambdaQueryWrapper<MerchantOnboardingFee> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(MerchantOnboardingFee::getPayStatus, query.getStatus());
        }
        wrapper.orderByDesc(MerchantOnboardingFee::getCreatedAt);

        Page<MerchantOnboardingFee> page = new Page<>(query.getPage(), query.getSize());
        Page<MerchantOnboardingFee> result = onboardingFeeMapper.selectPage(page, wrapper);

        List<AdminBillingListResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    private AdminBillingListResponse toResponse(MerchantOnboardingFee fee) {
        Merchant merchant = merchantMapper.selectById(fee.getMerchantId());
        OnboardingFeePlan plan = planMapper.selectById(fee.getPlanId());

        return AdminBillingListResponse.builder()
                .feeId(fee.getFeeId())
                .merchantId(fee.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : "未知商户")
                .planName(plan != null ? plan.getPlanName() : "未知套餐")
                .feeAmount(fee.getFeeAmount())
                .payStatus(fee.getPayStatus())
                .payTime(fee.getPayTime())
                .validStartDate(fee.getValidStartDate() != null ? fee.getValidStartDate().toString() : null)
                .validEndDate(fee.getValidEndDate() != null ? fee.getValidEndDate().toString() : null)
                .createdAt(fee.getCreatedAt())
                .build();
    }
}
