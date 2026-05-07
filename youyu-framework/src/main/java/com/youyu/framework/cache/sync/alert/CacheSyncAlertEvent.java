package com.youyu.framework.cache.sync.alert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.youyu.framework.cache.sync.core.InternalMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * 缓存同步告警事件
 * 封装告警的详细信息，供告警处理器使用
 */
@Getter
@Setter
public class CacheSyncAlertEvent {
    
    /**
     * 告警类型
     */
    private CacheSyncAlertType type;
    
    /**
     * 告警级别
     */
    private CacheSyncAlertLevel level;
    
    /**
     * 告警消息
     */
    private String message;

    /**
     * 原始消息
     */
    private InternalMessage originalMessage;
    
    /**
     * 异常信息（可选）
     */
    private Throwable exception;
    
    /**
     * 告警发生时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 实例ID
     */
    private String instanceId;
    /**
     * 消费者名称
     */
    private String consumerName;
    /**
     * 分组名称
     */
    private String groupName;

    public static CacheSyncAlertEvent of(CacheSyncAlertType type, String message, InternalMessage originalMessage, Throwable exception,
                                         LocalDateTime timestamp, String instanceId, String consumerName, String groupName) {
        return of(type, type.getDefaultLevel(), message, originalMessage, exception, timestamp, instanceId, consumerName, groupName);
    }
    public static CacheSyncAlertEvent of(CacheSyncAlertType type, CacheSyncAlertLevel level, String message, InternalMessage originalMessage, Throwable exception,
                                         LocalDateTime timestamp, String instanceId, String consumerName, String groupName) {
        CacheSyncAlertEvent cacheSyncAlertEvent = new CacheSyncAlertEvent();
        cacheSyncAlertEvent.type = type;
        cacheSyncAlertEvent.level = level;
        cacheSyncAlertEvent.message = message;
        cacheSyncAlertEvent.originalMessage = originalMessage;
        cacheSyncAlertEvent.exception = exception;
        cacheSyncAlertEvent.timestamp = timestamp;
        cacheSyncAlertEvent.instanceId = instanceId;
        cacheSyncAlertEvent.consumerName = consumerName;
        cacheSyncAlertEvent.groupName = groupName;
        return cacheSyncAlertEvent;
    }

    /**
     * 构建日志消息
     */
    public String buildLogMessage() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("[").append(type).append("] ");
        sb.append("[").append(level).append("] ");
        sb.append(message);

        if (instanceId != null) {
            sb.append(" | instanceId=").append(instanceId);
        }

        if(originalMessage != null) {
            sb.append(" | originalMessage=").append(originalMessage.toString());
        }

        return sb.toString();
    }

}
