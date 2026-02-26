package com.wsh.billing.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.util.IdGenerator;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 月度服务费扣除定时任务
 * 每月1号凌晨03:00执行：
 * 1. 汇总所有商户上月的服务费（按单扣费模式）
 * 2. 生成月度服务费汇总记录
 * 3. 从商户余额中扣除服务费
 * 4. 余额不足的标记待扣状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyServiceFeeJob {

    private final ServiceFeeRecordMapper serviceFeeRecordMapper;
    private final MonthlyServiceFeeMapper monthlyServiceFeeMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantBalanceMapper balanceMapper;
    private final MerchantBalanceLogMapper balanceLogMapper;
    private final RedisUtil redisUtil;

    private static final String LOCK_KEY = "lock:monthly_service_fee";

    /**
     * 每月1号凌晨03:00执行
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void execute() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisUtil.tryLock(LOCK_KEY, lockValue, 60, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(locked)) {
            log.info("MonthlyServiceFeeJob: 未获取到锁，跳过执行");
            return;
        }

        try {
            log.info("MonthlyServiceFeeJob: 开始月度服务费扣除");
            
            // 计算上个月的年月
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            String yearMonth = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // 获取所有按单扣费模式的商户
            List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                    .eq(Merchant::getServiceFeeMode, Constants.SERVICE_FEE_MODE_PER_ORDER)
                    .eq(Merchant::getStatus, Constants.MERCHANT_STATUS_ACTIVE)
            );
            
            int successCount = 0;
            int insufficientCount = 0;
            
            for (Merchant merchant : merchants) {
                try {
                    boolean result = processMonthlyFee(merchant, yearMonth, lastMonth);
                    if (result) {
                        successCount++;
                    } else {
                        insufficientCount++;
                    }
                } catch (Exception e) {
                    log.error("处理商户月度服务费异常: merchantId={}", merchant.getMerchantId(), e);
                }
            }
            
            log.info("MonthlyServiceFeeJob: 执行完成，成功扣费{}家，余额不足{}家", successCount, insufficientCount);
        } catch (Exception e) {
            log.error("MonthlyServiceFeeJob: 执行异常", e);
        } finally {
            redisUtil.releaseLock(LOCK_KEY, lockValue);
        }
    }

    /**
     * 处理单个商户的月度服务费
     * @return true-扣费成功, false-余额不足
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean processMonthlyFee(Merchant merchant, String yearMonth, YearMonth ym) {
        Long merchantId = merchant.getMerchantId();
        
        // 检查是否已有该月记录
        MonthlyServiceFee existing = monthlyServiceFeeMapper.selectOne(
            new LambdaQueryWrapper<MonthlyServiceFee>()
                .eq(MonthlyServiceFee::getMerchantId, merchantId)
                .eq(MonthlyServiceFee::getYearMonth, yearMonth)
        );
        
        if (existing != null && existing.getDeductStatus() == 1) {
            log.info("商户{}的{}月服务费已扣除，跳过", merchantId, yearMonth);
            return true;
        }
        
        // 计算该月服务费
        LocalDateTime startTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endTime = ym.plusMonths(1).atDay(1).atStartOfDay();
        
        List<ServiceFeeRecord> feeRecords = serviceFeeRecordMapper.selectByMerchantIdAndTimeRange(
            merchantId, startTime, endTime
        );
        
        if (feeRecords.isEmpty()) {
            log.info("商户{}的{}月无服务费记录", merchantId, yearMonth);
            return true;
        }
        
        // 汇总
        int orderCount = feeRecords.size();
        BigDecimal totalAmount = feeRecords.stream()
            .map(ServiceFeeRecord::getOrderAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal serviceFee = feeRecords.stream()
            .map(ServiceFeeRecord::getServiceFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 创建或更新月度汇总记录
        MonthlyServiceFee summary = existing != null ? existing : new MonthlyServiceFee();
        summary.setMerchantId(merchantId);
        summary.setYearMonth(yearMonth);
        summary.setOrderCount(orderCount);
        summary.setTotalAmount(totalAmount);
        summary.setServiceFee(serviceFee);
        
        // 获取商户余额
        MerchantBalance balance = balanceMapper.selectOne(
            new LambdaQueryWrapper<MerchantBalance>()
                .eq(MerchantBalance::getMerchantId, merchantId)
        );
        
        if (balance == null) {
            // 创建余额账户
            balance = new MerchantBalance();
            balance.setBalanceId(IdGenerator.nextId());
            balance.setMerchantId(merchantId);
            balance.setBalance(BigDecimal.ZERO);
            balance.setTotalRecharge(BigDecimal.ZERO);
            balance.setTotalConsume(BigDecimal.ZERO);
            balanceMapper.insert(balance);
        }
        
        // 检查余额是否充足
        if (balance.getBalance().compareTo(serviceFee) < 0) {
            // 余额不足
            summary.setDeductStatus(2); // 2-余额不足
            if (existing != null) {
                monthlyServiceFeeMapper.updateById(summary);
            } else {
                summary.setSummaryId(IdGenerator.nextId());
                monthlyServiceFeeMapper.insert(summary);
            }
            log.warn("商户{}余额不足，无法扣除{}月服务费: 余额={}, 应扣={}", 
                merchantId, yearMonth, balance.getBalance(), serviceFee);
            return false;
        }
        
        // 扣除余额
        BigDecimal balanceBefore = balance.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(serviceFee);
        
        balance.setBalance(balanceAfter);
        balance.setTotalConsume(balance.getTotalConsume().add(serviceFee));
        balanceMapper.updateById(balance);
        
        // 记录流水
        MerchantBalanceLog balanceLog = new MerchantBalanceLog();
        balanceLog.setLogId(IdGenerator.nextId());
        balanceLog.setMerchantId(merchantId);
        balanceLog.setChangeType(2); // 2-扣费
        balanceLog.setAmount(serviceFee.negate()); // 负数表示扣除
        balanceLog.setBalanceBefore(balanceBefore);
        balanceLog.setBalanceAfter(balanceAfter);
        balanceLog.setRelatedOrderNo(yearMonth + "-SERVICE-FEE");
        balanceLog.setRemark("月度服务费扣除：" + yearMonth);
        balanceLogMapper.insert(balanceLog);
        
        // 更新汇总状态
        summary.setDeductStatus(1); // 1-已扣
        summary.setDeductTime(LocalDateTime.now());
        if (existing != null) {
            monthlyServiceFeeMapper.updateById(summary);
        } else {
            summary.setSummaryId(IdGenerator.nextId());
            monthlyServiceFeeMapper.insert(summary);
        }
        
        log.info("商户{}的{}月服务费扣除成功: 订单数={}, 交易额={}, 服务费={}", 
            merchantId, yearMonth, orderCount, totalAmount, serviceFee);
        return true;
    }
}
