package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "门店信息响应")
public class MerchantBranchResponse {

    @Schema(description = "门店ID")
    private Long branchId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "门店名称")
    private String branchName;

    @Schema(description = "门店地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "状态：0停用 1正常")
    private Integer status;
}
