package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "商户信息响应")
public class MerchantResponse {

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

    @Schema(description = "对接类型：1-API 2-RPA 3-手动")
    private Integer integrationType;

    @Schema(description = "交易服务费率")
    private BigDecimal profitSharingRate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "门店列表")
    private List<MerchantBranchResponse> branches;
}
