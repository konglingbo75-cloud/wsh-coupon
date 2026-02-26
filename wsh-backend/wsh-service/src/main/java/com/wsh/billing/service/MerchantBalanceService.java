package com.wsh.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.billing.dto.MerchantBalanceResponse;
import com.wsh.billing.dto.MerchantBalanceResponse.BalanceLogItem;
import com.wsh.billing.dto.MonthlyServiceFeeResponse;
import com.wsh.domain.entity.MerchantBalance;
import com.wsh.domain.entity.MerchantBalanceLog;
import com.wsh.domain.entity.MonthlyServiceFee;
import com.wsh.domain.mapper.MerchantBalanceLogMapper;
import com.wsh.domain.mapper.MerchantBalanceMapper;
import com.wsh.domain.mapper.MonthlyServiceFeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户余额与服务费管理（预付费模式）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantBalanceService {

    private final MerchantBalanceMapper balanceMapper;
    private final MerchantBalanceLogMapper balanceLogMapper;
    private final MonthlyServiceFeeMapper monthlyFeeMapper;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取商户余额信息
     */
    public MerchantBalanceResponse getBalance(Long merchantId) {
        MerchantBalance balance = getOrCreateBalance(merchantId);

        // 查询最近20条流水
        List<MerchantBalanceLog> logs = balanceLogMapper.selectList(
                new LambdaQueryWrapper<MerchantBalanceLog>()
                        .eq(MerchantBalanceLog::getMerchantId, merchantId)
                        .orderByDesc(MerchantBalanceLog::getCreatedAt)
                        .last("LIMIT 20"));

        List<BalanceLogItem> logItems = logs.stream().map(l -> BalanceLogItem.builder()
                .logId(l.getLogId())
                .changeType(l.getChangeType())
                .amount(l.getAmount())
                .balanceBefore(l.getBalanceBefore())
                .balanceAfter(l.getBalanceAfter())
                .relatedOrderNo(l.getRelatedOrderNo())
                .remark(l.getRemark())
                .createdAt(l.getCreatedAt() != null ? l.getCreatedAt().format(DT_FMT) : null)
                .build()
        ).collect(Collectors.toList());

        return MerchantBalanceResponse.builder()
                .balance(balance.getBalance())
                .totalRecharge(balance.getTotalRecharge())
                .totalConsume(balance.getTotalConsume())
                .recentLogs(logItems)
                .build();
    }

    /**
     * 充值（预付费模式下的余额充值）
     */
    @Transactional
    public MerchantBalanceResponse recharge(Long merchantId, BigDecimal amount, String transactionId) {
        MerchantBalance balance = getOrCreateBalance(merchantId);

        BigDecimal before = balance.getBalance();
        BigDecimal after = before.add(amount);

        balance.setBalance(after);
        balance.setTotalRecharge(balance.getTotalRecharge().add(amount));
        balanceMapper.updateById(balance);

        // 记录流水
        MerchantBalanceLog balanceLog = new MerchantBalanceLog();
        balanceLog.setMerchantId(merchantId);
        balanceLog.setChangeType(1); // 充值
        balanceLog.setAmount(amount);
        balanceLog.setBalanceBefore(before);
        balanceLog.setBalanceAfter(after);
        balanceLog.setRelatedOrderNo(transactionId);
        balanceLog.setRemark("余额充值");
        balanceLogMapper.insert(balanceLog);

        log.info("商户余额充值: merchantId={}, amount={}, after={}", merchantId, amount, after);

        return getBalance(merchantId);
    }

    /**
     * 扣减余额（服务费扣减）
     */
    @Transactional
    public boolean deductBalance(Long merchantId, BigDecimal amount, String orderNo, String remark) {
        MerchantBalance balance = getOrCreateBalance(merchantId);

        if (balance.getBalance().compareTo(amount) < 0) {
            log.warn("余额不足: merchantId={}, balance={}, deduct={}", merchantId, balance.getBalance(), amount);
            return false;
        }

        BigDecimal before = balance.getBalance();
        BigDecimal after = before.subtract(amount);

        balance.setBalance(after);
        balance.setTotalConsume(balance.getTotalConsume().add(amount));
        balanceMapper.updateById(balance);

        MerchantBalanceLog balanceLog = new MerchantBalanceLog();
        balanceLog.setMerchantId(merchantId);
        balanceLog.setChangeType(2); // 扣减
        balanceLog.setAmount(amount);
        balanceLog.setBalanceBefore(before);
        balanceLog.setBalanceAfter(after);
        balanceLog.setRelatedOrderNo(orderNo);
        balanceLog.setRemark(remark != null ? remark : "服务费扣减");
        balanceLogMapper.insert(balanceLog);

        log.info("商户余额扣减: merchantId={}, amount={}, after={}", merchantId, amount, after);
        return true;
    }

    /**
     * 查询月度服务费汇总
     */
    public List<MonthlyServiceFeeResponse> listMonthlyFees(Long merchantId) {
        List<MonthlyServiceFee> fees = monthlyFeeMapper.selectList(
                new LambdaQueryWrapper<MonthlyServiceFee>()
                        .eq(MonthlyServiceFee::getMerchantId, merchantId)
                        .orderByDesc(MonthlyServiceFee::getYearMonth));

        if (fees.isEmpty()) {
            return Collections.emptyList();
        }

        return fees.stream().map(f -> MonthlyServiceFeeResponse.builder()
                .summaryId(f.getSummaryId())
                .yearMonth(f.getYearMonth())
                .orderCount(f.getOrderCount())
                .totalAmount(f.getTotalAmount())
                .serviceFee(f.getServiceFee())
                .deductStatus(f.getDeductStatus())
                .deductTime(f.getDeductTime() != null ? f.getDeductTime().format(DT_FMT) : null)
                .build()
        ).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 获取或创建商户余额记录（每个商户一行）
     */
    private MerchantBalance getOrCreateBalance(Long merchantId) {
        MerchantBalance balance = balanceMapper.selectOne(
                new LambdaQueryWrapper<MerchantBalance>()
                        .eq(MerchantBalance::getMerchantId, merchantId));

        if (balance == null) {
            balance = new MerchantBalance();
            balance.setMerchantId(merchantId);
            balance.setBalance(BigDecimal.ZERO);
            balance.setTotalRecharge(BigDecimal.ZERO);
            balance.setTotalConsume(BigDecimal.ZERO);
            balanceMapper.insert(balance);
        }

        return balance;
    }
}
