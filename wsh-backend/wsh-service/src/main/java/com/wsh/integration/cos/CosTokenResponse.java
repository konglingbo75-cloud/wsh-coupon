package com.wsh.integration.cos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "COS 临时密钥响应（前端直传用）")
public class CosTokenResponse {

    @Schema(description = "临时密钥 SecretId")
    private String tmpSecretId;

    @Schema(description = "临时密钥 SecretKey")
    private String tmpSecretKey;

    @Schema(description = "临时 Token")
    private String sessionToken;

    @Schema(description = "过期时间（Unix 时间戳，秒）")
    private Long expiredTime;

    @Schema(description = "起始时间（Unix 时间戳，秒）")
    private Long startTime;

    @Schema(description = "Bucket 名称", example = "wsh-dev-1234567890")
    private String bucket;

    @Schema(description = "地域", example = "ap-guangzhou")
    private String region;

    @Schema(description = "允许上传的路径前缀", example = "merchant/logo/")
    private String allowPrefix;
}
