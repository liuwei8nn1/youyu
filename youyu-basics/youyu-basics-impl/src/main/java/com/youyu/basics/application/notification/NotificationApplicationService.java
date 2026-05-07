package com.youyu.basics.application.notification;

import com.youyu.basics.api.notification.dto.SendNotificationRequest;
import com.youyu.basics.api.notification.dto.SendNotificationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知应用服务
 */
@Service
public class NotificationApplicationService {
    
    /**
     * 发送通知
     */
    public SendNotificationResponse sendNotification(SendNotificationRequest request) {
        // TODO: 根据通知类型调用不同的领域服务
        return new SendNotificationResponse();
    }
    
    /**
     * 批量发送通知
     */
    public List<SendNotificationResponse> batchSendNotifications(
            List<SendNotificationRequest> requests) {
        // TODO: 实现批量发送逻辑
        return List.of();
    }
}
