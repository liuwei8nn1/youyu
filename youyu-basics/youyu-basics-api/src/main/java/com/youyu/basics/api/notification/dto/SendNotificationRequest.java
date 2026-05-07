package com.youyu.basics.api.notification.dto;

import com.youyu.basics.api.notification.enums.NotificationType;
import lombok.Data;

/**
 * 发送通知请求
 */
@Data
public class SendNotificationRequest {
    /**
     * 通知类型（SMS/EMAIL/IN_APP）
     */
    private NotificationType type;
    
    /**
     * 接收者（手机号/邮箱/用户ID）
     */
    private String receiver;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 模板代码（可选）
     */
    private String templateCode;
    
    /**
     * 模板参数（可选）
     */
    private Object templateParams;
}
