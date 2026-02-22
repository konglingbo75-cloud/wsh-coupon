package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "商户管理详情")
public class AdminMerchantDetailResponse {

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户编码")
    private String merchantCode;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactPhone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经营类目")
    private String businessCategory;

    @Schema(description = "状态：0待审核 1正常 2冻结")
    private Integer status;

    @Schema(description = "对接类型")
    private Integer integrationType;

    @Schema(description = "交易服务费率")
    private BigDecimal profitSharingRate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "门店列表")
    private List<BranchItem> branches;

    @Schema(description = "入驻费信息")
    private OnboardingFeeItem onboardingFee;

    @Schema(description = "审核日志")
    private List<AuditLogItem> auditLogs;

    @Data
    @Builder
    @Schema(description = "门店信息")
    public static class BranchItem {
        private Long branchId;
        private String branchName;
        private String address;
        private Integer status;
    }

    @Data
    @Builder
    @Schema(description = "入驻费信息")
    public static class OnboardingFeeItem {
        private Long feeId;
        private String planName;
        private BigDecimal feeAmount;
        private Integer payStatus;
        private LocalDateTime payTime;
        private String validStartDate;
        private String validEndDate;
    }

    @Data
    @Builder
    @Schema(description = "审核日志")
    public static class AuditLogItem {
        private Long logId;
        private String action;
        private Integer prevStatus;
        private Integer newStatus;
        private String reason;
        private String adminName;
        private LocalDateTime createdAt;
    }
}
