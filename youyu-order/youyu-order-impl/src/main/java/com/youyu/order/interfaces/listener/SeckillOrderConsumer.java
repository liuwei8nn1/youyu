package com.youyu.order.interfaces.listener;

import com.alibaba.fastjson2.JSON;
import com.youyu.order.api.dto.SeckillOrderMessage;
import com.youyu.order.application.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消息消费者（接口层）
 * <p>
 * 职责：
 * 1. 监听秒杀订单消息
 * 2. 调用订单服务创建订单
 * 3. 处理消费失败的情况
 * <p>
 * 重试机制：
 * - 通过 maxReconsumeTimes 配置最大重试次数
 * - 每次重试都会重新进入 onMessage() 方法
 * - 超过最大重试次数后，消息进入死信队列（DLQ）
 * <p>
 * 如果需要获取重试次数，可以将参数改为 MessageExt 类型：
 * public void onMessage(MessageExt messageExt) {
 *     int reconsumeTimes = messageExt.getReconsumeTimes(); // 获取重试次数
 *     String body = new String(messageExt.getBody());      // 获取消息体
 * }
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "seckill-order-topic",
    consumerGroup = "seckill-order-consumer-group",
    selectorExpression = "create-order",
    maxReconsumeTimes = 3  // 最大重试次数（包括首次消费），超过后进入死信队列
)
@RequiredArgsConstructor
public class SeckillOrderConsumer implements RocketMQListener<String> {

    private final OrderApplicationService orderApplicationService;

    @Override
    public void onMessage(String message) {
        log.info("收到秒杀订单消息: {}", message);
        
        try {
            // 解析消息
            SeckillOrderMessage seckillMessage = JSON.parseObject(message, SeckillOrderMessage.class);
            
            // 创建订单
            orderApplicationService.createSeckillOrder(
                seckillMessage.getUserId(),
                seckillMessage.getProductId(),
                seckillMessage.getQuantity(),
                seckillMessage.getAmount(),
                seckillMessage.getActivityId()
            );
            
            log.info("秒杀订单创建成功，orderId: {}", seckillMessage.getOrderId());
            
        } catch (Exception e) {
            // 注意：使用 String 类型的 onMessage 无法获取 MessageExt，也就无法获取重试次数
            // 如果需要获取重试次数，需要将参数改为 MessageExt 类型
            log.error("秒杀订单创建失败，message: {}", message, e);
            // 抛出异常，触发 MQ 重试机制
            throw new RuntimeException("订单创建失败", e);
        }
    }
}
