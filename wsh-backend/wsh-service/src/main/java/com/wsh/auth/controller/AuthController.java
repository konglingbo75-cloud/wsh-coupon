package com.wsh.auth.controller;

import com.wsh.auth.dto.WechatLoginRequest;
import com.wsh.auth.dto.WechatLoginResponse;
import com.wsh.auth.service.WechatAuthService;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户认证", description = "微信小程序登录/授权")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WechatAuthService wechatAuthService;

    @Operation(summary = "微信小程序登录",
            description = "使用 wx.login() 获取的 code 进行登录，返回 JWT Token。" +
                    "首次登录自动注册，若传入 phoneCode 则同时获取并绑定手机号。")
    @PostMapping("/wechat/login")
    public R<WechatLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        WechatLoginResponse response = wechatAuthService.login(request);
        return R.ok(response);
    }
}
