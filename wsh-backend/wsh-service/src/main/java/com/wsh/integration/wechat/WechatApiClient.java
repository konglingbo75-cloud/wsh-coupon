package com.wsh.integration.wechat;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wsh.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信 API 客户端 —— 封装小程序服务端接口调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatApiClient {

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String GET_PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    private final WechatProperties wechatProperties;

    /**
     * 调用 code2Session 接口，用 wx.login() 获得的 code 换取 openid + session_key
     *
     * @param code wx.login() 返回的临时登录凭证
     * @return JSONObject 包含 openid, session_key, unionid(可选), errcode, errmsg
     */
    public JSONObject code2Session(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wechatProperties.getAppId());
        params.put("secret", wechatProperties.getAppSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String response = HttpUtil.get(CODE2SESSION_URL, params);
        log.debug("code2Session response: {}", response);

        JSONObject json = JSONUtil.parseObj(response);
        int errCode = json.getInt("errcode", 0);
        if (errCode != 0) {
            log.error("code2Session failed: errcode={}, errmsg={}", errCode, json.getStr("errmsg"));
            throw new BusinessException(401, "微信登录失败：" + json.getStr("errmsg"));
        }
        return json;
    }

    /**
     * 通过 phoneCode（新版手机号快速验证组件）获取用户手机号
     * 文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html
     *
     * @param accessToken 接口调用凭证
     * @param phoneCode   手机号获取凭证（button 组件返回）
     * @return 手机号字符串
     */
    public String getPhoneNumber(String accessToken, String phoneCode) {
        String url = GET_PHONE_URL + "?access_token=" + accessToken;

        JSONObject body = new JSONObject();
        body.set("code", phoneCode);

        String response = HttpUtil.post(url, body.toString());
        log.debug("getPhoneNumber response: {}", response);

        JSONObject json = JSONUtil.parseObj(response);
        int errCode = json.getInt("errcode", 0);
        if (errCode != 0) {
            log.error("getPhoneNumber failed: errcode={}, errmsg={}", errCode, json.getStr("errmsg"));
            throw new BusinessException(400, "获取手机号失败：" + json.getStr("errmsg"));
        }

        JSONObject phoneInfo = json.getJSONObject("phone_info");
        return phoneInfo.getStr("purePhoneNumber");
    }

    /**
     * 获取接口调用凭证 access_token（稳定版）
     * 实际生产中应使用 Redis 缓存，避免频繁调用
     */
    public String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/stable_token";

        JSONObject body = new JSONObject();
        body.set("grant_type", "client_credential");
        body.set("appid", wechatProperties.getAppId());
        body.set("secret", wechatProperties.getAppSecret());

        String response = HttpUtil.post(url, body.toString());
        log.debug("getAccessToken response: {}", response);

        JSONObject json = JSONUtil.parseObj(response);
        int errCode = json.getInt("errcode", 0);
        if (errCode != 0) {
            log.error("getAccessToken failed: errcode={}, errmsg={}", errCode, json.getStr("errmsg"));
            throw new BusinessException(500, "获取access_token失败");
        }

        return json.getStr("access_token");
    }

    /**
     * 发送订阅消息
     * 文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
     *
     * @param toOpenid   接收者 openid
     * @param templateId 订阅消息模板ID
     * @param page       点击模板卡片后跳转的小程序页面（可选）
     * @param data       模板数据
     * @return 是否发送成功
     */
    public boolean sendSubscribeMessage(String toOpenid, String templateId, String page, Map<String, Object> data) {
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        JSONObject body = new JSONObject();
        body.set("touser", toOpenid);
        body.set("template_id", templateId);
        if (page != null && !page.isEmpty()) {
            body.set("page", page);
        }
        body.set("data", data);
        body.set("miniprogram_state", "formal"); // 正式版

        String response = HttpUtil.post(url, body.toString());
        log.debug("sendSubscribeMessage response: {}", response);

        JSONObject json = JSONUtil.parseObj(response);
        int errCode = json.getInt("errcode", 0);
        if (errCode != 0) {
            log.warn("sendSubscribeMessage failed: errcode={}, errmsg={}", errCode, json.getStr("errmsg"));
            return false;
        }
        return true;
    }
}
