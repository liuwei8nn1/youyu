package com.youyu.basics.sdk.notification;

import com.youyu.basics.api.notification.NotificationServiceApi;
import com.youyu.basics.api.notification.dto.SendNotificationRequest;
import com.youyu.basics.api.notification.dto.SendNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知服务 Feign 客户端
 */
@FeignClient(name = "youyu-basics", path = "/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationServiceClient implements NotificationServiceApi {
    
    @Override
    @PostMapping("/send")
    public SendNotificationResponse sendNotification(@RequestBody SendNotificationRequest request) {
        // TODO: Feign 自动实现
        return new SendNotificationResponse();
    }
    
    @Override
    @PostMapping("/batch-send")
    public List<SendNotificationResponse> batchSendNotifications(
            @RequestBody List<SendNotificationRequest> requests) {
        // TODO: Feign 自动实现
        return List.of();
    }
}
