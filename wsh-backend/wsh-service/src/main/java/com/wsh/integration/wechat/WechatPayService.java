package com.wsh.integration.wechat;

import com.wsh.common.core.exception.BusinessException;
import com.wsh.order.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付服务 —— 基于微信支付 v3 API
 *
 * 封装：
 * 1. JSAPI 统一下单（小程序调起支付）
 * 2. 签名生成（小程序端调起支付签名）
 * 3. 回调验签 & 解密
 * 4. 分账功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayService {

    private final WechatProperties wechatProperties;

    /**
     * 创建入驻费支付订单（JSAPI 统一下单）
     *
     * @param outTradeNo  商户订单号
     * @param totalFee    金额（元）
     * @param description 商品描述
     * @param openid      支付者 openid
     * @return 小程序调起支付所需参数
     */
    public Map<String, String> createJsapiOrder(String outTradeNo, BigDecimal totalFee,
                                                  String description, String openid) {
        try {
            // 金额转分
            int totalFenInt = totalFee.multiply(new BigDecimal("100")).intValue();

            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appid", wechatProperties.getAppId());
            requestBody.put("mchid", wechatProperties.getMchId());
            requestBody.put("description", description);
            requestBody.put("out_trade_no", outTradeNo);
            requestBody.put("notify_url", wechatProperties.getNotifyUrl() + "/wx-pay-onboarding");

            Map<String, Object> amount = new HashMap<>();
            amount.put("total", totalFenInt);
            amount.put("currency", "CNY");
            requestBody.put("amount", amount);

            Map<String, String> payer = new HashMap<>();
            payer.put("openid", openid);
            requestBody.put("payer", payer);

            // TODO: 使用微信支付 SDK (wechatpay-java) 发送 JSAPI 统一下单请求
            // 实际调用示例：
            // JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(config).build();
            // PrepayWithRequestPaymentResponse resp = service.prepayWithRequestPayment(request);
            //
            // 当前返回占位数据，待对接真实 SDK
            String prepayId = "PREPAY_" + outTradeNo;
            log.info("微信支付统一下单（占位）: outTradeNo={}, totalFen={}", outTradeNo, totalFenInt);

            // 生成小程序调起支付参数
            return buildPayParams(prepayId);

        } catch (Exception e) {
            log.error("微信支付统一下单失败: outTradeNo={}", outTradeNo, e);
            throw new BusinessException(500, "支付下单失败，请稍后重试");
        }
    }

    /**
     * 构造小程序调起支付所需的签名参数
     */
    private Map<String, String> buildPayParams(String prepayId) throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageValue = "prepay_id=" + prepayId;

        // 待签名字符串
        String message = wechatProperties.getAppId() + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + packageValue + "\n";

        String paySign = sign(message);

        Map<String, String> params = new HashMap<>();
        params.put("timeStamp", timeStamp);
        params.put("nonceStr", nonceStr);
        params.put("package", packageValue);
        params.put("signType", "RSA");
        params.put("paySign", paySign);
        params.put("prepayId", prepayId);
        return params;
    }

    /**
     * RSA-SHA256 签名
     */
    private String sign(String message) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        if (privateKey == null) {
            // 本地开发无证书，返回占位签名
            return "MOCK_SIGN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 加载商户私钥（本地开发无证书时返回null而非崩溃）
     */
    private PrivateKey loadPrivateKey() {
        String keyPath = wechatProperties.getPrivateKeyPath();
        if (keyPath == null || keyPath.isBlank()) {
            log.warn("微信支付商户私钥路径未配置，使用Mock签名");
            return null;
        }

        try {
            String keyContent;
            if (keyPath.startsWith("classpath:")) {
                String resourcePath = keyPath.substring("classpath:".length());
                try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        log.warn("微信支付商户私钥文件不存在: {}，使用Mock签名", resourcePath);
                        return null;
                    }
                    keyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                var path = Paths.get(keyPath);
                if (!Files.exists(path)) {
                    log.warn("微信支付商户私钥文件不存在: {}，使用Mock签名", keyPath);
                    return null;
                }
                keyContent = Files.readString(path);
            }

            // 去除 PEM 头尾
            keyContent = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.warn("加载微信支付商户私钥失败，使用Mock签名: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证微信支付回调签名（v3）
     * TODO: 使用 wechatpay-java SDK 的 NotificationParser 进行验签和解密
     *
     * @param serialNumber 微信平台证书序列号（Header: Wechatpay-Serial）
     * @param timestamp    时间戳（Header: Wechatpay-Timestamp）
     * @param nonce        随机串（Header: Wechatpay-Nonce）
     * @param body         请求体
     * @param signature    签名值（Header: Wechatpay-Signature）
     * @return 解密后的通知内容 JSON 字符串
     */
    public String verifyAndDecryptNotification(String serialNumber, String timestamp,
                                                String nonce, String body, String signature) {
        // TODO: 实现完整的回调验签和 AES-GCM 解密
        log.info("微信支付回调验签（占位）: serialNumber={}", serialNumber);
        return body;
    }

    /**
     * 创建订单支付（返回小程序调起支付参数）
     *
     * @param orderNo     订单号
     * @param amount      金额（元）
     * @param description 商品描述
     * @param openid      支付者 openid
     * @return PaymentResponse
     */
    public PaymentResponse createPayment(String orderNo, BigDecimal amount, String description, String openid) {
        try {
            Map<String, String> params = createJsapiOrder(orderNo, amount, description, openid);
            
            return PaymentResponse.builder()
                    .orderNo(orderNo)
                    .prepayId(params.get("prepayId"))
                    .timeStamp(params.get("timeStamp"))
                    .nonceStr(params.get("nonceStr"))
                    .signType(params.get("signType"))
                    .paySign(params.get("paySign"))
                    .packageValue(params.get("package"))
                    .build();
        } catch (Exception e) {
            log.error("创建支付订单失败: orderNo={}", orderNo, e);
            throw new BusinessException("支付下单失败，请稍后重试");
        }
    }

    /**
     * 执行分账（核销时调用）
     */
    public boolean executeProfitSharing(String transactionId, String orderNo, BigDecimal merchantAmount) {
        try {
            log.info("执行分账（占位）: transactionId={}, orderNo={}, merchantAmount={}", 
                    transactionId, orderNo, merchantAmount);
            return true;
        } catch (Exception e) {
            log.error("执行分账失败: orderNo={}", orderNo, e);
            return false;
        }
    }

    /**
     * 申请退款
     */
    public boolean refund(String orderNo, String transactionId, BigDecimal refundAmount, String reason) {
        try {
            log.info("申请退款（占位）: orderNo={}, transactionId={}, refundAmount={}, reason={}", 
                    orderNo, transactionId, refundAmount, reason);
            return true;
        } catch (Exception e) {
            log.error("申请退款失败: orderNo={}", orderNo, e);
            return false;
        }
    }
}
