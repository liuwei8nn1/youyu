package com.youyu.framework.cache.sync.alert;

/**
 * 缓存同步告警处理器接口
 * 接入方可以实现此接口来自定义告警渠道（钉钉、企业微信、邮件等）
 * 
 * 使用示例：
 * <pre>{@code
 * @Component
 * public class DingTalkAlertHandler implements CacheSyncAlertHandler {
 *     @Override
 *     public void handle(CacheSyncAlertEvent event) {
 *         // 只处理ERROR和CRITICAL级别的告警
 *         if (event.getLevel() == AlertLevel.ERROR || event.getLevel() == AlertLevel.CRITICAL) {
 *             // 发送到钉钉
 *             sendToDingTalk(event);
 *         }
 *     }
 * }
 * }</pre>
 */
public interface CacheSyncAlertHandler {
    
    /**
     * 处理告警事件
     * 
     * @param event 告警事件，包含告警类型、级别、消息等信息
     */
    void handle(CacheSyncAlertEvent event);
}
