package com.youyu.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 统一配置类
 * <p>
 * 功能：
 * 1. 自动生成 OpenAPI 3.0 文档
 * 2. 提供 Knife4j UI 界面（/doc.html）
 * 3. 支持标准 Swagger UI（/swagger-ui/index.html）
 * 4. 支持环境开关（通过 knife4j.enable 控制）
 * <p>
 * 使用方式：
 * - 在 Nacos 配置中添加：knife4j.enable=true（dev/test环境）
 * - 生产环境配置：knife4j.enable=false
 * <p>
 * 访问地址：
 * - Knife4j UI: http://localhost:9001/doc.html
 * - Swagger UI: http://localhost:9001/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:9001/v3/api-docs
 *
 * @author YouYu Team
 * @since 2026/04/30
 */
@Configuration
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = false)
public class Knife4jConfig {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * 配置 OpenAPI 文档信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API文档")
                        .version("3.2.0")
                        .description("基于 SpringDoc + Knife4j 的 RESTful API 文档")
                        .contact(new Contact()
                                .name("YouYu Team")
                                .email("support@youyu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}
