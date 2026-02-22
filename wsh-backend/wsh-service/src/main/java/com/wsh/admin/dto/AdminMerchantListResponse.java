package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "商户管理列表项")
public class AdminMerchantListResponse {

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户编码")
    private String merchantCode;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactPhone;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "经营类目")
    private String businessCategory;

    @Schema(description = "状态：0待审核 1正常 2冻结")
    private Integer status;

    @Schema(description = "交易服务费率")
    private BigDecimal profitSharingRate;

    @Schema(description = "对接类型：1-API 2-RPA 3-手动")
    private Integer integrationType;

    @Schema(description = "门店数量")
    private Integer branchCount;

    @Schema(description = "活动数量")
    private Integer activityCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
