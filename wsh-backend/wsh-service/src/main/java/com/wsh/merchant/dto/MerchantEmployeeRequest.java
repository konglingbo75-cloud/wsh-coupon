package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "添加/更新商户员工请求")
public class MerchantEmployeeRequest {

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    @Schema(description = "门店ID（不传则为总店员工）")
    private Long branchId;

    @NotBlank(message = "员工姓名不能为空")
    @Schema(description = "员工姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13900139000")
    private String phone;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色：1管理员/店长 2收银员 3服务员", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer role;
}
