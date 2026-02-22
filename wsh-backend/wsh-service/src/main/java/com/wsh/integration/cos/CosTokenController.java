package com.wsh.integration.cos;

import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * COS 临时密钥接口 —— 前端直传文件（Logo/图片等）
 * 使用腾讯云 STS（Security Token Service）生成临时访问凭证
 */
@Slf4j
@Tag(name = "文件上传", description = "COS 临时密钥获取，前端直传文件")
@RestController
@RequestMapping("/v1/cos")
@RequiredArgsConstructor
public class CosTokenController {

    private final CosProperties cosProperties;

    @Operation(summary = "获取 COS 临时上传凭证",
            description = "返回腾讯云 STS 临时密钥，前端使用此密钥直传文件到 COS。" +
                    "密钥有效期 1800 秒（30 分钟），仅允许上传到指定路径前缀下。")
    @GetMapping("/sts-token")
    public R<CosTokenResponse> getStsToken(
            @Parameter(description = "上传路径前缀，如 merchant/logo/、activity/cover/")
            @RequestParam(defaultValue = "upload/") String pathPrefix) {

        try {
            // 规范化路径前缀
            if (!pathPrefix.endsWith("/")) {
                pathPrefix = pathPrefix + "/";
            }

            // TODO: 集成腾讯云 STS SDK (com.tencentcloudapi:tencentcloud-sdk-java-sts)
            // 实际实现示例：
            // StsClient client = new StsClient(cred, cosProperties.getRegion());
            // GetFederationTokenRequest req = new GetFederationTokenRequest();
            // req.setName("wsh-cos-sts");
            // req.setDurationSeconds(1800L);
            // req.setPolicy(buildPolicy(pathPrefix));
            // GetFederationTokenResponse resp = client.GetFederationToken(req);

            // 当前返回占位响应
            long now = System.currentTimeMillis() / 1000;
            CosTokenResponse response = CosTokenResponse.builder()
                    .tmpSecretId("STS_SECRET_ID_PLACEHOLDER")
                    .tmpSecretKey("STS_SECRET_KEY_PLACEHOLDER")
                    .sessionToken("STS_TOKEN_PLACEHOLDER")
                    .startTime(now)
                    .expiredTime(now + 1800)
                    .bucket(cosProperties.getBucket())
                    .region(cosProperties.getRegion())
                    .allowPrefix(pathPrefix)
                    .build();

            log.debug("生成 COS STS 临时凭证: pathPrefix={}", pathPrefix);
            return R.ok(response);

        } catch (Exception e) {
            log.error("获取 COS 临时密钥失败", e);
            throw new BusinessException(500, "获取上传凭证失败，请稍后重试");
        }
    }
}
