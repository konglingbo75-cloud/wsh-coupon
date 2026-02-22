package com.wsh.matching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "商户会员快照响应")
public class MerchantMemberResponse {

    @Schema(description = "快照ID")
    private Long snapshotId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户Logo")
    private String merchantLogoUrl;

    @Schema(description = "商户系统内会员ID")
    private String sourceMemberId;

    @Schema(description = "会员等级名称")
    private String memberLevelName;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "积分可兑换价值（元）")
    private BigDecimal pointsValue;

    @Schema(description = "积分过期日期")
    private LocalDate pointsExpireDate;

    @Schema(description = "储值余额")
    private BigDecimal balance;

    @Schema(description = "消费次数")
    private Integer consumeCount;

    @Schema(description = "累计消费金额")
    private BigDecimal totalConsumeAmount;

    @Schema(description = "最近消费时间")
    private LocalDateTime lastConsumeTime;

    @Schema(description = "沉睡等级：0活跃 1轻度 2中度 3深度")
    private Integer dormancyLevel;

    @Schema(description = "沉睡等级描述")
    private String dormancyDesc;

    @Schema(description = "最近同步时间")
    private LocalDateTime syncTime;
}
