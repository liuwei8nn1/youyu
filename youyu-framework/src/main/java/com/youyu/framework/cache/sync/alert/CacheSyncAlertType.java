package com.youyu.framework.cache.sync.alert;

/**
 * 告警类型枚举
 * 定义缓存同步过程中可能发生的各种告警场景
 * 每个类型都有默认的告警级别，接入方可以根据需要自定义
 */
public enum CacheSyncAlertType {
    
    /**
     * 消息发布失败 - 发布到Redis Stream时失败
     */
    PUBLISH_FAILED(CacheSyncAlertLevel.ERROR),
    
    /**
     * 消息消费失败 - 处理缓存清理消息时发生异常
     */
    CONSUME_FAILED(CacheSyncAlertLevel.ERROR),
    
    /**
     * 消息被丢弃 - 重试次数超限后被丢弃
     */
    MESSAGE_DISCARDED(CacheSyncAlertLevel.CRITICAL),
    
    /**
     * 延时消息重试耗尽 - 延时消息达到最大重试次数仍失败
     */
    DELAYED_MESSAGE_RETRY_EXHAUSTED(CacheSyncAlertLevel.CRITICAL),
    
    /**
     * 消费者组创建失败 - 启动时创建消费者组异常
     */
    CONSUMER_GROUP_CREATE_FAILED(CacheSyncAlertLevel.WARN),
    
    /**
     * Pending消息扫描异常 - 定期扫描Pending消息时发生错误
     */
    PENDING_SCAN_ERROR(CacheSyncAlertLevel.ERROR),
    
    /**
     * 消息重新发送失败 - 消费失败后重新发送消息失败
     */
    MESSAGE_RESEND_FAILED(CacheSyncAlertLevel.ERROR),
    
    /**
     * 离线消费者清理异常 - 清理离线消费者时发生错误
     */
    OFFLINE_CONSUMER_CLEANUP_ERROR(CacheSyncAlertLevel.WARN),
    
    /**
     * Lag指标更新失败 - 更新消费延迟指标时失败
     */
    LAG_METRICS_UPDATE_FAILED(CacheSyncAlertLevel.INFO);
    
    private final CacheSyncAlertLevel defaultLevel;
    
    CacheSyncAlertType(CacheSyncAlertLevel defaultLevel) {
        this.defaultLevel = defaultLevel;
    }
    
    public CacheSyncAlertLevel getDefaultLevel() {
        return defaultLevel;
    }
}
