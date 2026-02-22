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
 * API 模式适配器
 * 通过商户开放的 API 接口进行会员匹配、数据同步
 * 一期为骨架实现，需根据实际对接的商户系统API完善具体逻辑
 */
@Slf4j
@Component
public class ApiAdapter implements MerchantDataAdapter {

    @Override
    public MemberMatchResult matchMemberByPhone(String phone, Long merchantId) {
        // TODO: 调用商户开放API，用手机号查询会员
        // 不同商户的API协议不同，后续可按 merchantId 路由到不同的子适配器
        log.info("API模式: 尝试在商户{}中匹配会员, phone={}", merchantId, phone);

        // 骨架实现：返回未匹配
        return MemberMatchResult.builder()
                .matched(false)
                .memberStatus("unknown")
                .build();
    }

    @Override
    public MemberDataDTO syncMemberData(Long userId, Long merchantId) {
        // TODO: 调用商户API同步会员积分、储值、等级等数据
        log.info("API模式: 同步商户{}会员数据, userId={}", merchantId, userId);
        return null;
    }

    @Override
    public List<ConsumeRecordDTO> syncConsumeRecords(Long userId, Long merchantId) {
        // TODO: 调用商户API同步消费记录
        log.info("API模式: 同步商户{}消费记录, userId={}", merchantId, userId);
        return Collections.emptyList();
    }

    @Override
    public List<ActivityDTO> syncActivities(Long merchantId) {
        // TODO: 调用商户API同步活动列表
        log.info("API模式: 同步商户{}活动列表", merchantId);
        return Collections.emptyList();
    }

    @Override
    public String getType() {
        return Constants.SYNC_SOURCE_API;
    }
}
