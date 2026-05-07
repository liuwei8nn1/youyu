package com.youyu.gateway.config;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * API 签名验证配置属性
 * <p>
 * 配置示例：
 * <pre>
 * api-sign:
 *   enabled: true
 *   secrets:
 *     web-app: web-secret-key
 *     ios-app: ios-secret-key
 *   time-window: 60
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api-sign")
public class ApiSignProperties {

    /** 是否启用签名验证（默认false） */
    private boolean enabled = false;

    /** 签名密钥映射 (AppId -> AppSecret) */
    private Map<String, String> secrets = new HashMap<>();

    /** 时间窗口（秒），请求时间与服务器时间偏差超过此值将拒绝（默认60秒） */
    private long timeWindow = 60;
}
