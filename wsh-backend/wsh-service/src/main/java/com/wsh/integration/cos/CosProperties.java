package com.wsh.integration.cos;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 COS 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wsh.cos")
public class CosProperties {

    /** 腾讯云 SecretId */
    private String secretId;

    /** 腾讯云 SecretKey */
    private String secretKey;

    /** 存储桶地域 */
    private String region;

    /** 存储桶名称 */
    private String bucket;
}
