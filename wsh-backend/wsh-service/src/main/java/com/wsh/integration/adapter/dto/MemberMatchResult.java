package com.wsh.integration.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员匹配结果（适配器返回）
 */
@Data
@Builder
public class MemberMatchResult {

    /** 是否匹配到会员 */
    private boolean matched;

    /** 商户系统内会员ID */
    private String sourceMemberId;

    /** 会员状态描述：active/dormant */
    private String memberStatus;

    /** 会员详细数据（匹配成功时） */
    private MemberDataDTO memberData;
}
