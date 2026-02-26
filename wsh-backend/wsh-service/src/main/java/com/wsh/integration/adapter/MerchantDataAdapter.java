package com.wsh.integration.adapter;

import com.wsh.integration.adapter.dto.ActivityDTO;
import com.wsh.integration.adapter.dto.ConsumeRecordDTO;
import com.wsh.integration.adapter.dto.MemberDataDTO;
import com.wsh.integration.adapter.dto.MemberMatchResult;

import java.util.List;

/**
 * 商户数据适配器统一接口
 * 策略模式：不同对接方式实现此接口（API / Manual / RPA）
 */
public interface MerchantDataAdapter {

    /**
     * 用手机号查询会员（核心匹配接口）
     */
    MemberMatchResult matchMemberByPhone(String phone, Long merchantId);

    /**
     * 同步会员数据
     */
    MemberDataDTO syncMemberData(Long userId, Long merchantId);

    /**
     * 同步消费记录
     */
    List<ConsumeRecordDTO> syncConsumeRecords(Long userId, Long merchantId);

    /**
     * 同步商户活动（包含活动的目标会员类型）
     */
    List<ActivityDTO> syncActivities(Long merchantId);

    /**
     * 获取适配器类型标识
     */
    String getType();

    /**
     * 通知商户系统核销结果（默认空实现，各适配器按需覆盖）
     *
     * @param merchantId  商户ID
     * @param voucherCode 券码
     * @param verifyTime  核销时间
     * @return true=同步成功, false=同步失败
     */
    default boolean notifyVerification(Long merchantId, String voucherCode, java.time.LocalDateTime verifyTime) {
        return true;
    }
}
