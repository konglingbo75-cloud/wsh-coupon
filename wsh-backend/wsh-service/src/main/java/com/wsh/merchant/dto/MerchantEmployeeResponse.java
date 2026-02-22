package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "商户员工信息响应")
public class MerchantEmployeeResponse {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "门店ID")
    private Long branchId;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "微信openid（已绑定时返回）")
    private String openid;

    @Schema(description = "角色：1管理员/店长 2收银员 3服务员")
    private Integer role;

    @Schema(description = "状态：0停用 1正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
