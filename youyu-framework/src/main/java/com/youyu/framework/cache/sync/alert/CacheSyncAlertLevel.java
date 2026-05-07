package com.youyu.framework.cache.sync.alert;

/**
 * 告警级别枚举
 * 用于区分不同严重程度的告警事件
 */
public enum CacheSyncAlertLevel {
    
    /**
     * 信息级别 - 正常操作记录，无需关注
     */
    INFO,
    
    /**
     * 警告级别 - 需要注意但可自动恢复的情况
     */
    WARN,
    
    /**
     * 错误级别 - 发生错误，需要人工介入检查
     */
    ERROR,
    
    /**
     * 严重级别 - 严重故障，需要立即处理
     */
    CRITICAL
}
