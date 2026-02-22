package com.wsh.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "入驻费支付响应（微信支付调起参数）")
public class OnboardingFeePayResponse {

    @Schema(description = "入驻费记录ID")
    private Long feeId;

    @Schema(description = "微信预支付交易会话标识")
    private String prepayId;

    @Schema(description = "时间戳（秒）")
    private String timeStamp;

    @Schema(description = "随机字符串")
    private String nonceStr;

    @Schema(description = "统一下单接口返回的 prepay_id 参数值，格式：prepay_id=xxx")
    private String packageValue;

    @Schema(description = "签名类型，固定 RSA")
    private String signType;

    @Schema(description = "签名")
    private String paySign;
}
