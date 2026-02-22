package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "商户入驻申请请求")
public class MerchantRegisterRequest {

    @NotBlank(message = "商户名称不能为空")
    @Size(max = 128, message = "商户名称不能超过128个字符")
    @Schema(description = "商户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "老王火锅")
    private String merchantName;

    @Schema(description = "商户Logo URL（COS 直传后填入）")
    private String logoUrl;

    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactName;

    @NotBlank(message = "联系人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系人手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String contactPhone;

    @NotBlank(message = "商户地址不能为空")
    @Schema(description = "商户地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotBlank(message = "所在城市不能为空")
    @Schema(description = "所在城市", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳")
    private String city;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @NotBlank(message = "经营类目不能为空")
    @Schema(description = "经营类目", requiredMode = Schema.RequiredMode.REQUIRED, example = "火锅")
    private String businessCategory;

    @Schema(description = "对接类型：1-API 2-RPA（二期） 3-手动", example = "3")
    private Integer integrationType;

    @NotEmpty(message = "至少填写一个门店信息")
    @Valid
    @Schema(description = "门店列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<BranchInfo> branches;

    @Data
    @Schema(description = "门店信息")
    public static class BranchInfo {

        @NotBlank(message = "门店名称不能为空")
        @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "老王火锅（南山店）")
        private String branchName;

        @NotBlank(message = "门店地址不能为空")
        @Schema(description = "门店地址", requiredMode = Schema.RequiredMode.REQUIRED)
        private String address;

        @Schema(description = "经度")
        private BigDecimal longitude;

        @Schema(description = "纬度")
        private BigDecimal latitude;
    }
}
