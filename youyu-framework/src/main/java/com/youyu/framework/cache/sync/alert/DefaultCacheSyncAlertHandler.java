package com.youyu.framework.cache.sync.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的告警处理器实现
 * 仅记录日志，作为兜底实现
 * 当没有其他自定义的告警处理器时，所有告警都会通过此处理器记录到日志中
 */
public class DefaultCacheSyncAlertHandler implements CacheSyncAlertHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheSyncAlertHandler.class);
    
    @Override
    public void handle(CacheSyncAlertEvent event) {
        // 根据告警级别选择不同的日志级别
        String logMessage = event.buildLogMessage();
        
        switch (event.getLevel()) {
            case INFO:
                logger.info(logMessage);
                break;
            case WARN:
                logger.warn(logMessage);
                break;
            case ERROR:
                if (event.getException() != null) {
                    logger.error(logMessage, event.getException());
                } else {
                    logger.error(logMessage);
                }
                break;
            case CRITICAL:
                if (event.getException() != null) {
                    logger.error("[CRITICAL ALERT] " + logMessage, event.getException());
                } else {
                    logger.error("[CRITICAL ALERT] " + logMessage);
                }
                break;
            default:
                logger.info(logMessage);
        }
    }

}
