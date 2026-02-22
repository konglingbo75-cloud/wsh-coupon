package com.wsh.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wsh.billing.dto.OnboardingFeePayRequest;
import com.wsh.billing.dto.OnboardingFeePayResponse;
import com.wsh.billing.dto.OnboardingFeePlanResponse;
import com.wsh.billing.dto.OnboardingFeeResponse;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantOnboardingFee;
import com.wsh.domain.entity.OnboardingFeePlan;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.MerchantOnboardingFeeMapper;
import com.wsh.domain.mapper.OnboardingFeePlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 入驻费业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingFeeService {

    private final OnboardingFeePlanMapper planMapper;
    private final MerchantOnboardingFeeMapper feeMapper;
    private final MerchantMapper merchantMapper;

    /**
     * 查询所有启用的入驻费套餐
     */
    public List<OnboardingFeePlanResponse> listPlans() {
        List<OnboardingFeePlan> plans = planMapper.selectList(
                new LambdaQueryWrapper<OnboardingFeePlan>()
                        .eq(OnboardingFeePlan::getStatus, 1)
                        .orderByAsc(OnboardingFeePlan::getPlanType)
                        .orderByAsc(OnboardingFeePlan::getFeeAmount));

        return plans.stream().map(p -> OnboardingFeePlanResponse.builder()
                .planId(p.getPlanId())
                .planName(p.getPlanName())
                .planType(p.getPlanType())
                .feeAmount(p.getFeeAmount())
                .durationMonths(p.getDurationMonths())
                .description(p.getDescription())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 创建入驻费支付订单，调用微信支付统一下单
     *
     * @return 微信支付调起参数
     */
    @Transactional
    public OnboardingFeePayResponse createPayOrder(OnboardingFeePayRequest request) {
        // 1. 校验商户存在
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        // 2. 校验套餐存在且启用
        OnboardingFeePlan plan = planMapper.selectById(request.getPlanId());
        if (plan == null || plan.getStatus() != 1) {
            throw new BusinessException(404, "套餐不存在或已停用");
        }

        // 3. 检查是否有未支付的入驻费记录，如有则关闭
        closeUnpaidOrders(request.getMerchantId());

        // 4. 创建入驻费记录
        MerchantOnboardingFee fee = new MerchantOnboardingFee();
        fee.setMerchantId(request.getMerchantId());
        fee.setPlanId(plan.getPlanId());
        fee.setFeeAmount(plan.getFeeAmount());
        fee.setPayStatus(Constants.ONBOARDING_PAY_PENDING);
        feeMapper.insert(fee);

        log.info("创建入驻费支付订单: feeId={}, merchantId={}, planId={}, amount={}",
                fee.getFeeId(), request.getMerchantId(), plan.getPlanId(), plan.getFeeAmount());

        // 5. 调用微信支付统一下单（由 WechatPayService 实现，此处预留）
        // TODO: 集成 WechatPayService.createOnboardingFeeOrder(fee)
        // 暂时返回占位响应，待阶段1-7完成后对接
        return OnboardingFeePayResponse.builder()
                .feeId(fee.getFeeId())
                .prepayId("PREPAY_PLACEHOLDER")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .nonceStr("NONCE_PLACEHOLDER")
                .packageValue("prepay_id=PREPAY_PLACEHOLDER")
                .signType("RSA")
                .paySign("SIGN_PLACEHOLDER")
                .build();
    }

    /**
     * 微信支付回调 —— 入驻费支付成功处理
     *
     * @param feeId         入驻费记录ID
     * @param transactionId 微信支付单号
     */
    @Transactional
    public void handlePaySuccess(Long feeId, String transactionId) {
        MerchantOnboardingFee fee = feeMapper.selectById(feeId);
        if (fee == null) {
            log.error("入驻费记录不存在: feeId={}", feeId);
            return;
        }

        if (fee.getPayStatus() == Constants.ONBOARDING_PAY_SUCCESS) {
            log.warn("入驻费已支付，重复回调: feeId={}", feeId);
            return;
        }

        // 1. 更新入驻费记录
        LocalDate now = LocalDate.now();
        // 查询套餐获取有效期月数
        OnboardingFeePlan plan = planMapper.selectById(fee.getPlanId());
        int months = (plan != null) ? plan.getDurationMonths() : 12;

        fee.setPayStatus(Constants.ONBOARDING_PAY_SUCCESS);
        fee.setPayTime(LocalDateTime.now());
        fee.setTransactionId(transactionId);
        fee.setValidStartDate(now);
        fee.setValidEndDate(now.plusMonths(months));
        feeMapper.updateById(fee);

        // 2. 激活商户（待审核 → 正常）
        Merchant merchant = merchantMapper.selectById(fee.getMerchantId());
        if (merchant != null && merchant.getStatus() != Constants.MERCHANT_STATUS_ACTIVE) {
            merchant.setStatus(Constants.MERCHANT_STATUS_ACTIVE);
            merchantMapper.updateById(merchant);
            log.info("商户已激活: merchantId={}", merchant.getMerchantId());
        }

        log.info("入驻费支付成功: feeId={}, merchantId={}, validUntil={}",
                feeId, fee.getMerchantId(), fee.getValidEndDate());
    }

    /**
     * 查询商户最新入驻费记录
     */
    public OnboardingFeeResponse getLatestFee(Long merchantId) {
        MerchantOnboardingFee fee = feeMapper.selectOne(
                new LambdaQueryWrapper<MerchantOnboardingFee>()
                        .eq(MerchantOnboardingFee::getMerchantId, merchantId)
                        .orderByDesc(MerchantOnboardingFee::getCreatedAt)
                        .last("LIMIT 1"));

        if (fee == null) {
            throw new BusinessException(404, "暂无入驻费记录");
        }

        return buildFeeResponse(fee);
    }

    // ==================== 私有方法 ====================

    /**
     * 关闭该商户所有未支付的入驻费订单
     */
    private void closeUnpaidOrders(Long merchantId) {
        feeMapper.update(null, new LambdaUpdateWrapper<MerchantOnboardingFee>()
                .eq(MerchantOnboardingFee::getMerchantId, merchantId)
                .eq(MerchantOnboardingFee::getPayStatus, Constants.ONBOARDING_PAY_PENDING)
                .set(MerchantOnboardingFee::getPayStatus, Constants.ONBOARDING_PAY_CLOSED));
    }

    private OnboardingFeeResponse buildFeeResponse(MerchantOnboardingFee fee) {
        // 查询套餐名称
        String planName = null;
        OnboardingFeePlan plan = planMapper.selectById(fee.getPlanId());
        if (plan != null) {
            planName = plan.getPlanName();
        }

        // 计算剩余天数
        int remainingDays = -1;
        if (fee.getPayStatus() == Constants.ONBOARDING_PAY_SUCCESS && fee.getValidEndDate() != null) {
            remainingDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), fee.getValidEndDate());
            if (remainingDays < 0) {
                remainingDays = 0;
            }
        }

        return OnboardingFeeResponse.builder()
                .feeId(fee.getFeeId())
                .merchantId(fee.getMerchantId())
                .planId(fee.getPlanId())
                .planName(planName)
                .feeAmount(fee.getFeeAmount())
                .payStatus(fee.getPayStatus())
                .payTime(fee.getPayTime())
                .validStartDate(fee.getValidStartDate())
                .validEndDate(fee.getValidEndDate())
                .remainingDays(remainingDays)
                .createdAt(fee.getCreatedAt())
                .build();
    }
}
