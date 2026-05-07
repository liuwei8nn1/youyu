package com.youyu.framework.warn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 告警配置属性
 * <p>
 * 配置示例：
 * <pre>
 * warn:
 *   enabled: true
 *   platform: wechat
 *   url: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
 *   secret: xxx  # 可选
 *   package: com.youyu
 *   distinct:
 *     type: redis  # local 或 redis
 *     timeout: 60
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "warn")
public class WarnProperties {

    /** 是否启用告警功能（默认true） */
    private boolean enabled = true;

    /** 平台类型：wechat、dingtalk */
    private String platform = "wechat";

    /** Webhook URL */
    private String url;

    /** 密钥（可选，用于签名验证） */
    private String secret;

    /** 包名前缀（用于过滤堆栈跟踪） */
    private String packageName = "com.youyu";

    /** 去重配置 */
    private DistinctConfig distinct = new DistinctConfig();

    @Data
    public static class DistinctConfig {
        /** 去重实现类型：local（本地缓存）、redis（Redis） */
        private String type = "local";
        /** 去重超时时间（秒） */
        private int timeout = 60;
    }
}
