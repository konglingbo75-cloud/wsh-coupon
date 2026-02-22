package com.wsh.integration.wechat;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wsh.billing.service.OnboardingFeeService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 微信支付回调控制器
 * 路径在 SecurityConfig 中已配置白名单（/v1/callback/**）
 */
@Slf4j
@Hidden // 不在 Swagger 中暴露回调接口
@RestController
@RequestMapping("/v1/callback")
@RequiredArgsConstructor
public class CallbackController {

    private final WechatPayService wechatPayService;
    private final OnboardingFeeService onboardingFeeService;

    /**
     * 微信支付回调 —— 入驻费支付结果通知
     */
    @PostMapping("/wx-pay-onboarding")
    public ResponseEntity<Map<String, String>> onboardingPayNotify(
            HttpServletRequest httpRequest,
            @RequestBody String body) {

        try {
            // 1. 验签 + 解密
            String serialNumber = httpRequest.getHeader("Wechatpay-Serial");
            String timestamp = httpRequest.getHeader("Wechatpay-Timestamp");
            String nonce = httpRequest.getHeader("Wechatpay-Nonce");
            String signature = httpRequest.getHeader("Wechatpay-Signature");

            String decryptedBody = wechatPayService.verifyAndDecryptNotification(
                    serialNumber, timestamp, nonce, body, signature);

            // 2. 解析通知内容
            JSONObject notification = JSONUtil.parseObj(decryptedBody);
            String outTradeNo = notification.getStr("out_trade_no");
            String transactionId = notification.getStr("transaction_id");
            String tradeState = notification.getStr("trade_state");

            log.info("收到入驻费支付回调: outTradeNo={}, tradeState={}, transactionId={}",
                    outTradeNo, tradeState, transactionId);

            // 3. 支付成功处理
            if ("SUCCESS".equals(tradeState)) {
                // outTradeNo 格式约定：OBF_{feeId}，解析出 feeId
                Long feeId = parseFeeIdFromTradeNo(outTradeNo);
                onboardingFeeService.handlePaySuccess(feeId, transactionId);
            }

            // 4. 返回成功响应（微信要求）
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));

        } catch (Exception e) {
            log.error("入驻费支付回调处理异常", e);
            return ResponseEntity.ok(Map.of("code", "FAIL", "message", "处理失败"));
        }
    }

    /**
     * 从商户订单号解析入驻费记录ID
     * 商户订单号格式约定：OBF_{feeId}_{timestamp}
     */
    private Long parseFeeIdFromTradeNo(String outTradeNo) {
        try {
            String[] parts = outTradeNo.split("_");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            log.error("无法解析商户订单号: {}", outTradeNo, e);
            throw new RuntimeException("无效的商户订单号: " + outTradeNo);
        }
    }
}
