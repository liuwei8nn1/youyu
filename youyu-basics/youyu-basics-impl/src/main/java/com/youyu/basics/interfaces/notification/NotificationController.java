package com.youyu.basics.interfaces.notification;

import com.youyu.basics.api.notification.NotificationServiceApi;
import com.youyu.basics.api.notification.dto.SendNotificationRequest;
import com.youyu.basics.api.notification.dto.SendNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知服务 REST 接口
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationServiceApi {
    
    @Override
    @PostMapping("/send")
    public SendNotificationResponse sendNotification(@RequestBody SendNotificationRequest request) {
        // TODO: 实现发送通知逻辑
        return new SendNotificationResponse();
    }
    
    @Override
    @PostMapping("/batch-send")
    public List<SendNotificationResponse> batchSendNotifications(
            @RequestBody List<SendNotificationRequest> requests) {
        // TODO: 实现批量发送通知逻辑
        return List.of();
    }
}
