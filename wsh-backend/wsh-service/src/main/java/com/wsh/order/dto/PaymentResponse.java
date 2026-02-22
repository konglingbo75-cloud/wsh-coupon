package com.wsh.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 支付请求响应（返回微信支付参数）
 */
@Data
@Builder
@Schema(description = "支付请求响应")
public class PaymentResponse {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "预支付交易会话标识")
    private String prepayId;

    @Schema(description = "时间戳")
    private String timeStamp;

    @Schema(description = "随机字符串")
    private String nonceStr;

    @Schema(description = "签名方式，固定值RSA")
    private String signType;

    @Schema(description = "签名")
    private String paySign;

    @Schema(description = "小程序调起支付的参数包")
    private String packageValue;
}
