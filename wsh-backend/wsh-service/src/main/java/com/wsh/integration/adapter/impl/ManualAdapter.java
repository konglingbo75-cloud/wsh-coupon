package com.wsh.integration.adapter.impl;

import com.wsh.common.core.constant.Constants;
import com.wsh.integration.adapter.MerchantDataAdapter;
import com.wsh.integration.adapter.dto.ActivityDTO;
import com.wsh.integration.adapter.dto.ConsumeRecordDTO;
import com.wsh.integration.adapter.dto.MemberDataDTO;
import com.wsh.integration.adapter.dto.MemberMatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 手动模式适配器
 * 商户未接入API时的默认适配器，数据由商户手动维护
 * 匹配/同步操作均返回空结果，依赖商户后台手动录入
 */
@Slf4j
@Component
public class ManualAdapter implements MerchantDataAdapter {

    @Override
    public MemberMatchResult matchMemberByPhone(String phone, Long merchantId) {
        log.debug("手动模式: 商户{}暂不支持自动会员匹配, phone={}", merchantId, phone);
        return MemberMatchResult.builder()
                .matched(false)
                .memberStatus("unknown")
                .build();
    }

    @Override
    public MemberDataDTO syncMemberData(Long userId, Long merchantId) {
        log.debug("手动模式: 商户{}暂不支持自动同步会员数据, userId={}", merchantId, userId);
        return null;
    }

    @Override
    public List<ConsumeRecordDTO> syncConsumeRecords(Long userId, Long merchantId) {
        log.debug("手动模式: 商户{}暂不支持自动同步消费记录, userId={}", merchantId, userId);
        return Collections.emptyList();
    }

    @Override
    public List<ActivityDTO> syncActivities(Long merchantId) {
        log.debug("手动模式: 商户{}暂不支持自动同步活动, 活动需手动创建", merchantId);
        return Collections.emptyList();
    }

    @Override
    public String getType() {
        return Constants.SYNC_SOURCE_MANUAL;
    }
}
