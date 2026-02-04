package com.mca.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger 配置类
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("请输入 JWT Token，格式：Bearer {token} 或直接输入 {token}")))
                .info(new Info()
                        .title("MCA Server API")
                        .description("Multi-Caster Assistant 后端服务 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MCA Team")
                                .email("support@mca.dev"))
                        .license(new License()
                                .name("MIT License")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("认证模块")
                .pathsToMatch("/auth/**", "/activation/**", "/api/auth/**", "/api/activation/**")
                .build();
    }

    @Bean
    public GroupedOpenApi liveApi() {
        return GroupedOpenApi.builder()
                .group("live")
                .displayName("直播模块")
                .pathsToMatch("/sessions/**", "/gifts/**", "/reports/**", "/api/sessions/**", "/api/gifts/**", "/api/reports/**")
                .build();
    }

    @Bean
    public GroupedOpenApi roomApi() {
        return GroupedOpenApi.builder()
                .group("room")
                .displayName("房间模块")
                .pathsToMatch("/rooms/**", "/sessions/room/**", "/ws-monitor/room/**", "/api/rooms/**", "/api/sessions/room/**", "/api/ws-monitor/room/**")
                .build();
    }

    @Bean
    public GroupedOpenApi widgetApi() {
        return GroupedOpenApi.builder()
                .group("widget")
                .displayName("挂件模块")
                .pathsToMatch("/widget/**", "/api/widget/**")
                .build();
    }

    @Bean
    public GroupedOpenApi configApi() {
        return GroupedOpenApi.builder()
                .group("config")
                .displayName("配置模块")
                .pathsToMatch("/presets/**", "/api/presets/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("管理与监控模块")
                .pathsToMatch("/ws-monitor/**", "/actuator/**", "/api/ws-monitor/**", "/api/actuator/**")
                .build();
    }

    @Bean
    public GroupedOpenApi miscApi() {
        return GroupedOpenApi.builder()
                .group("misc")
                .displayName("其他接口")
                .pathsToMatch("/**")
                .pathsToExclude(
                        "/auth/**",
                        "/activation/**",
                        "/sessions/**",
                        "/gifts/**",
                        "/reports/**",
                        "/presets/**",
                        "/widget/**",
                        "/ws-monitor/**",
                        "/actuator/**",
                        "/api/auth/**",
                        "/api/activation/**",
                        "/api/sessions/**",
                        "/api/gifts/**",
                        "/api/reports/**",
                        "/api/presets/**",
                        "/api/widget/**",
                        "/api/ws-monitor/**",
                        "/api/actuator/**"
                )
                .build();
    }
}
