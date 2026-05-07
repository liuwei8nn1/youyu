package com.youyu.framework.mq.compensation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQ 消息补偿配置属性
 * <p>
 * 配置示例：
 * <pre>
 * mq:
 *   compensation:
 *     enabled: true
 *     table-name: mq_message_compensation
 *     max-retry-count: 3
 *     retry-interval-seconds: 60
 *     batch-size: 100
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "mq.compensation")
public class MqCompensationProperties {

    /** 表名（默认 mq_message_compensation） */
    private String tableName = "mq_message_compensation";

    /** 最大重试次数（默认3次） */
    private int maxRetryCount = 3;

    /** 重试间隔（秒，默认60秒） */
    private int retryIntervalSeconds = 60;

    /** 每次批量处理的记录数（默认100条） */
    private int batchSize = 100;
}
