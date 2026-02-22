package com.wsh.integration.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序 & 微信支付配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wsh.wechat")
public class WechatProperties {

    /** 小程序 AppId */
    private String appId;

    /** 小程序 AppSecret */
    private String appSecret;

    /** 微信支付商户号 */
    private String mchId;

    /** 微信支付 API v3 密钥 */
    private String apiV3Key;

    /** 商户私钥文件路径 */
    private String privateKeyPath;

    /** 商户证书序列号 */
    private String serialNumber;

    /** 支付回调通知地址前缀 */
    private String notifyUrl;
}
