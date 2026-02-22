package com.wsh.equity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import com.wsh.domain.entity.UserEquitySummary;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import com.wsh.domain.mapper.UserEquitySummaryMapper;
import com.wsh.equity.dto.EquitySummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 权益汇总服务
 * 计算用户全部积分价值 + 储值余额 + 券价值
 * 维护 tb_user_equity_summary 缓存表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquitySummaryService {

    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final UserEquitySummaryMapper summaryMapper;
    private final RedisUtil redisUtil;

    /** 缓存有效期：2小时 */
    private static final long CACHE_TTL_HOURS = 2;

    /**
     * 获取用户权益汇总（优先从缓存读取，过期则重新计算）
     */
    public EquitySummaryResponse getSummary(Long userId) {
        // 1. 尝试从 Redis 缓存读取
        String cacheKey = Constants.CACHE_USER_EQUITY + userId;
        EquitySummaryResponse cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 查询 DB 缓存表
        UserEquitySummary dbSummary = summaryMapper.selectOne(
                new LambdaQueryWrapper<UserEquitySummary>()
                        .eq(UserEquitySummary::getUserId, userId));

        // 判断是否需要重新计算（无记录或超过2小时未更新）
        boolean needRecalculate = dbSummary == null
                || dbSummary.getLastUpdated() == null
                || dbSummary.getLastUpdated().plusHours(CACHE_TTL_HOURS).isBefore(LocalDateTime.now());

        if (needRecalculate) {
            dbSummary = recalculate(userId, dbSummary);
        }

        // 3. 构建响应并缓存
        EquitySummaryResponse response = buildResponse(userId, dbSummary);
        redisUtil.set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        return response;
    }

    /**
     * 强制重新计算并刷新缓存
     */
    @Transactional
    public EquitySummaryResponse refreshSummary(Long userId) {
        UserEquitySummary existing = summaryMapper.selectOne(
                new LambdaQueryWrapper<UserEquitySummary>()
                        .eq(UserEquitySummary::getUserId, userId));

        UserEquitySummary updated = recalculate(userId, existing);
        EquitySummaryResponse response = buildResponse(userId, updated);

        // 刷新 Redis 缓存
        String cacheKey = Constants.CACHE_USER_EQUITY + userId;
        redisUtil.set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return response;
    }

    // ==================== 私有方法 ====================

    /**
     * 重新计算权益汇总
     */
    @Transactional
    protected UserEquitySummary recalculate(Long userId, UserEquitySummary existing) {
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectByUserId(userId);

        BigDecimal totalPointsValue = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal expiringPointsValue = BigDecimal.ZERO;
        int merchantCount = 0;

        LocalDate now = LocalDate.now();
        LocalDate sevenDaysLater = now.plusDays(7);

        for (MerchantMemberSnapshot s : snapshots) {
            // 累加积分价值
            if (s.getPointsValue() != null && s.getPointsValue().compareTo(BigDecimal.ZERO) > 0) {
                totalPointsValue = totalPointsValue.add(s.getPointsValue());

                // 检查7天内过期的积分
                if (s.getPointsExpireDate() != null
                        && !s.getPointsExpireDate().isBefore(now)
                        && !s.getPointsExpireDate().isAfter(sevenDaysLater)) {
                    expiringPointsValue = expiringPointsValue.add(s.getPointsValue());
                }
            }

            // 累加储值余额
            if (s.getBalance() != null && s.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                totalBalance = totalBalance.add(s.getBalance());
            }

            // 有效商户计数（有积分或有余额）
            if ((s.getPointsValue() != null && s.getPointsValue().compareTo(BigDecimal.ZERO) > 0)
                    || (s.getBalance() != null && s.getBalance().compareTo(BigDecimal.ZERO) > 0)) {
                merchantCount++;
            }
        }

        // TODO: 后续从 tb_voucher 表统计未使用券的总价值和即将过期券数量
        BigDecimal totalVoucherValue = BigDecimal.ZERO;
        int expiringVoucherCount = 0;

        // 保存或更新汇总
        if (existing != null) {
            existing.setTotalPointsValue(totalPointsValue);
            existing.setTotalBalance(totalBalance);
            existing.setTotalVoucherValue(totalVoucherValue);
            existing.setExpiringPointsValue(expiringPointsValue);
            existing.setExpiringVoucherCount(expiringVoucherCount);
            existing.setMerchantCount(merchantCount);
            existing.setLastUpdated(LocalDateTime.now());
            summaryMapper.updateById(existing);
            return existing;
        } else {
            UserEquitySummary summary = new UserEquitySummary();
            summary.setUserId(userId);
            summary.setTotalPointsValue(totalPointsValue);
            summary.setTotalBalance(totalBalance);
            summary.setTotalVoucherValue(totalVoucherValue);
            summary.setExpiringPointsValue(expiringPointsValue);
            summary.setExpiringVoucherCount(expiringVoucherCount);
            summary.setMerchantCount(merchantCount);
            summary.setLastUpdated(LocalDateTime.now());
            summaryMapper.insert(summary);
            return summary;
        }
    }

    private EquitySummaryResponse buildResponse(Long userId, UserEquitySummary s) {
        BigDecimal totalAsset = BigDecimal.ZERO;
        if (s.getTotalPointsValue() != null) totalAsset = totalAsset.add(s.getTotalPointsValue());
        if (s.getTotalBalance() != null) totalAsset = totalAsset.add(s.getTotalBalance());
        if (s.getTotalVoucherValue() != null) totalAsset = totalAsset.add(s.getTotalVoucherValue());

        return EquitySummaryResponse.builder()
                .userId(userId)
                .totalPointsValue(s.getTotalPointsValue())
                .totalBalance(s.getTotalBalance())
                .totalVoucherValue(s.getTotalVoucherValue())
                .totalAssetValue(totalAsset)
                .expiringPointsValue(s.getExpiringPointsValue())
                .expiringVoucherCount(s.getExpiringVoucherCount())
                .merchantCount(s.getMerchantCount())
                .lastUpdated(s.getLastUpdated())
                .build();
    }
}
