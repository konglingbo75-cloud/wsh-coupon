package com.wsh.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信小程序登录请求")
public class WechatLoginRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "wx.login() 获取的临时登录凭证", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "getPhoneNumber 加密数据（首次登录获取手机号时传入）")
    private String encryptedData;

    @Schema(description = "加密算法初始向量（与 encryptedData 配对）")
    private String iv;

    @Schema(description = "getPhoneNumber 动态令牌（新版手机号快速验证组件使用）")
    private String phoneCode;
}
