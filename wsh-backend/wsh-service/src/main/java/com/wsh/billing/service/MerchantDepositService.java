package com.wsh.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.billing.dto.DepositPayRequest;
import com.wsh.billing.dto.MerchantDepositResponse;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.domain.entity.MerchantDeposit;
import com.wsh.domain.mapper.MerchantDepositMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户保证金管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantDepositService {

    private final MerchantDepositMapper depositMapper;

    /**
     * 获取商户保证金信息
     */
    public MerchantDepositResponse getDeposit(Long merchantId) {
        MerchantDeposit deposit = depositMapper.selectOne(
                new LambdaQueryWrapper<MerchantDeposit>()
                        .eq(MerchantDeposit::getMerchantId, merchantId)
                        .orderByDesc(MerchantDeposit::getCreatedAt)
                        .last("LIMIT 1"));

        if (deposit == null) {
            // 无保证金记录，返回零保证金状态
            return MerchantDepositResponse.builder()
                    .depositAmount(BigDecimal.ZERO)
                    .payStatus(-1) // -1 表示尚未设置
                    .build();
        }

        return toResponse(deposit);
    }

    /**
     * 缴纳保证金
     */
    @Transactional
    public MerchantDepositResponse payDeposit(Long merchantId, DepositPayRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            // 保证金为零，直接标记为已支付
            MerchantDeposit deposit = new MerchantDeposit();
            deposit.setMerchantId(merchantId);
            deposit.setDepositAmount(BigDecimal.ZERO);
            deposit.setPayStatus(1);
            deposit.setPayTime(LocalDateTime.now());
            depositMapper.insert(deposit);

            log.info("商户保证金设为零: merchantId={}", merchantId);
            return toResponse(deposit);
        }

        // 关闭之前未支付的保证金记录
        MerchantDeposit existing = depositMapper.selectOne(
                new LambdaQueryWrapper<MerchantDeposit>()
                        .eq(MerchantDeposit::getMerchantId, merchantId)
                        .eq(MerchantDeposit::getPayStatus, 0)
                        .last("LIMIT 1"));
        if (existing != null) {
            existing.setPayStatus(2); // 关闭
            depositMapper.updateById(existing);
        }

        // 创建新保证金记录
        MerchantDeposit deposit = new MerchantDeposit();
        deposit.setMerchantId(merchantId);
        deposit.setDepositAmount(request.getAmount());
        deposit.setPayStatus(0); // 待支付

        depositMapper.insert(deposit);
        log.info("创建保证金支付订单: depositId={}, merchantId={}, amount={}",
                deposit.getDepositId(), merchantId, request.getAmount());

        // TODO: 集成微信支付统一下单
        // 暂模拟直接支付成功
        deposit.setPayStatus(1);
        deposit.setPayTime(LocalDateTime.now());
        deposit.setTransactionId("DEP_SIM_" + System.currentTimeMillis());
        depositMapper.updateById(deposit);

        return toResponse(deposit);
    }

    /**
     * 检查商户是否已缴纳保证金
     */
    public boolean hasValidDeposit(Long merchantId) {
        Long count = depositMapper.selectCount(
                new LambdaQueryWrapper<MerchantDeposit>()
                        .eq(MerchantDeposit::getMerchantId, merchantId)
                        .eq(MerchantDeposit::getPayStatus, 1));
        return count != null && count > 0;
    }

    private MerchantDepositResponse toResponse(MerchantDeposit d) {
        return MerchantDepositResponse.builder()
                .depositId(d.getDepositId())
                .depositAmount(d.getDepositAmount())
                .payStatus(d.getPayStatus())
                .payTime(d.getPayTime())
                .refundTime(d.getRefundTime())
                .refundReason(d.getRefundReason())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
