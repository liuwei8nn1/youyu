package com.youyu.basics.api.notification;

import com.youyu.basics.api.notification.dto.SendNotificationRequest;
import com.youyu.basics.api.notification.dto.SendNotificationResponse;

/**
 * 通知服务接口
 */
public interface NotificationServiceApi {
    
    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 发送响应
     */
    SendNotificationResponse sendNotification(SendNotificationRequest request);
    
    /**
     * 批量发送通知
     *
     * @param requests 发送请求列表
     * @return 发送响应列表
     */
    java.util.List<SendNotificationResponse> batchSendNotifications(
        java.util.List<SendNotificationRequest> requests
    );
}
