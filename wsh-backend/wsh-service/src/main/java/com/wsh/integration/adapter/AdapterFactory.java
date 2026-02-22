package com.wsh.integration.adapter;

import com.wsh.common.core.constant.Constants;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 适配器工厂：根据商户的 integration_type 返回对应适配器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdapterFactory {

    private final MerchantMapper merchantMapper;
    private final List<MerchantDataAdapter> adapters;

    /**
     * 根据商户ID获取对应的数据适配器
     *
     * @param merchantId 商户ID
     * @return 对应的适配器实例
     */
    public MerchantDataAdapter getAdapter(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            log.warn("商户不存在: merchantId={}, 使用手动适配器", merchantId);
            return getAdapterByType(Constants.SYNC_SOURCE_MANUAL);
        }

        Integer integrationType = merchant.getIntegrationType();
        String type;
        if (integrationType != null && integrationType == 1) {
            type = Constants.SYNC_SOURCE_API;
        } else {
            // 默认使用手动适配器（integrationType=3 或 null）
            type = Constants.SYNC_SOURCE_MANUAL;
        }

        return getAdapterByType(type);
    }

    private MerchantDataAdapter getAdapterByType(String type) {
        return adapters.stream()
                .filter(a -> a.getType().equals(type))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("未找到类型为 {} 的适配器，回退到手动适配器", type);
                    return adapters.stream()
                            .filter(a -> a.getType().equals(Constants.SYNC_SOURCE_MANUAL))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("手动适配器未注册"));
                });
    }
}
