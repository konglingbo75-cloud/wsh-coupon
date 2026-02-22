package com.wsh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置 —— Swagger 先行模式
 * 访问地址：http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("微生活券吧 API")
                        .description("会员权益聚合平台 —— RESTful API 文档")
                        .version("1.0.0")
                        .contact(new Contact().name("WSH Team")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .schemaRequirement("Bearer",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token 认证，格式：Bearer {token}"));
    }

    // ========== API 分组 ==========

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-用户认证")
                .pathsToMatch("/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equityApi() {
        return GroupedOpenApi.builder()
                .group("02-权益中心")
                .pathsToMatch("/v1/equity/**", "/v1/users/me/**", "/v1/matching/**")
                .build();
    }

    @Bean
    public GroupedOpenApi activityApi() {
        return GroupedOpenApi.builder()
                .group("03-活动")
                .pathsToMatch("/v1/activities/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("04-公共接口（无需登录）")
                .pathsToMatch("/v1/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("05-订单与券")
                .pathsToMatch("/v1/orders/**", "/v1/vouchers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi verificationApi() {
        return GroupedOpenApi.builder()
                .group("06-核销")
                .pathsToMatch("/v1/verification/**")
                .build();
    }

    @Bean
    public GroupedOpenApi merchantApi() {
        return GroupedOpenApi.builder()
                .group("07-商户管理")
                .pathsToMatch("/v1/merchant/**", "/v1/cos/**")
                .build();
    }

    @Bean
    public GroupedOpenApi callbackApi() {
        return GroupedOpenApi.builder()
                .group("08-回调")
                .pathsToMatch("/v1/callback/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("09-平台运营后台")
                .pathsToMatch("/v1/admin/**")
                .build();
    }
}
