package com.youyu.basics.api.notification.dto;

import lombok.Data;

/**
 * 发送通知响应
 */
@Data
public class SendNotificationResponse {
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 是否发送成功
     */
    private Boolean success;
    
    /**
     * 失败原因（如果失败）
     */
    private String failReason;
}
