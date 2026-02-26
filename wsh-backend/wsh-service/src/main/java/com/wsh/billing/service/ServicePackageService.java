package com.wsh.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.billing.dto.MerchantPackageInfoResponse;
import com.wsh.billing.dto.PackagePurchaseRequest;
import com.wsh.billing.dto.ServicePackageResponse;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.domain.entity.MerchantPackagePurchase;
import com.wsh.domain.entity.ServicePackage;
import com.wsh.domain.mapper.MerchantPackagePurchaseMapper;
import com.wsh.domain.mapper.ServicePackageMapper;
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
 * 服务套餐管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicePackageService {

    private final ServicePackageMapper packageMapper;
    private final MerchantPackagePurchaseMapper purchaseMapper;

    /**
     * 查询所有启用的服务套餐
     */
    public List<ServicePackageResponse> listPackages() {
        List<ServicePackage> packages = packageMapper.selectList(
                new LambdaQueryWrapper<ServicePackage>()
                        .eq(ServicePackage::getStatus, 1)
                        .orderByAsc(ServicePackage::getSortOrder));

        return packages.stream().map(p -> ServicePackageResponse.builder()
                .packageId(p.getPackageId())
                .packageName(p.getPackageName())
                .packageType(p.getPackageType())
                .price(p.getPrice())
                .durationMonths(p.getDurationMonths())
                .serviceFeeRate(p.getServiceFeeRate())
                .features(p.getFeatures())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 获取商户当前有效的套餐信息
     */
    public MerchantPackageInfoResponse getCurrentPackage(Long merchantId) {
        MerchantPackagePurchase purchase = purchaseMapper.selectOne(
                new LambdaQueryWrapper<MerchantPackagePurchase>()
                        .eq(MerchantPackagePurchase::getMerchantId, merchantId)
                        .eq(MerchantPackagePurchase::getPayStatus, 1)
                        .orderByDesc(MerchantPackagePurchase::getValidEndDate)
                        .last("LIMIT 1"));

        if (purchase == null) {
            return null;
        }

        ServicePackage pkg = packageMapper.selectById(purchase.getPackageId());

        int remainingDays = 0;
        if (purchase.getValidEndDate() != null) {
            remainingDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), purchase.getValidEndDate());
            if (remainingDays < 0) remainingDays = 0;
        }

        return MerchantPackageInfoResponse.builder()
                .purchaseId(purchase.getPurchaseId())
                .packageId(purchase.getPackageId())
                .packageName(pkg != null ? pkg.getPackageName() : null)
                .packageType(pkg != null ? pkg.getPackageType() : null)
                .pricePaid(purchase.getPricePaid())
                .serviceFeeRate(pkg != null ? pkg.getServiceFeeRate() : null)
                .payStatus(purchase.getPayStatus())
                .validStartDate(purchase.getValidStartDate())
                .validEndDate(purchase.getValidEndDate())
                .remainingDays(remainingDays)
                .features(pkg != null ? pkg.getFeatures() : null)
                .createdAt(purchase.getCreatedAt())
                .build();
    }

    /**
     * 购买套餐（创建支付订单）
     */
    @Transactional
    public MerchantPackageInfoResponse purchasePackage(Long merchantId, PackagePurchaseRequest request) {
        ServicePackage pkg = packageMapper.selectById(request.getPackageId());
        if (pkg == null || pkg.getStatus() != 1) {
            throw new BusinessException(404, "套餐不存在或已停用");
        }

        // 关闭之前未支付的购买记录
        List<MerchantPackagePurchase> unpaid = purchaseMapper.selectList(
                new LambdaQueryWrapper<MerchantPackagePurchase>()
                        .eq(MerchantPackagePurchase::getMerchantId, merchantId)
                        .eq(MerchantPackagePurchase::getPayStatus, 0));
        for (MerchantPackagePurchase p : unpaid) {
            p.setPayStatus(2); // 关闭
            purchaseMapper.updateById(p);
        }

        // 创建新的购买记录
        MerchantPackagePurchase purchase = new MerchantPackagePurchase();
        purchase.setMerchantId(merchantId);
        purchase.setPackageId(pkg.getPackageId());
        purchase.setPricePaid(pkg.getPrice());
        purchase.setPayStatus(0); // 待支付

        purchaseMapper.insert(purchase);
        log.info("创建套餐购买订单: purchaseId={}, merchantId={}, packageId={}",
                purchase.getPurchaseId(), merchantId, pkg.getPackageId());

        // TODO: 集成微信支付统一下单
        // 暂模拟直接支付成功
        LocalDate now = LocalDate.now();
        purchase.setPayStatus(1);
        purchase.setPayTime(LocalDateTime.now());
        purchase.setTransactionId("SIM_" + System.currentTimeMillis());
        purchase.setValidStartDate(now);
        purchase.setValidEndDate(now.plusMonths(pkg.getDurationMonths()));
        purchaseMapper.updateById(purchase);

        return getCurrentPackage(merchantId);
    }
}
