package com.youyu.order.interfaces.listener;

import com.alibaba.fastjson2.JSON;
import com.youyu.order.api.dto.SeckillOrderTimeoutMessage;
import com.youyu.order.application.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单超时未支付消费者（接口层）
 * <p>
 * 职责：
 * 1. 监听订单超时未支付消息（支持普通订单和秒杀订单）
 * 2. 调用应用层服务处理订单超时逻辑
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "order-timeout-topic",
    consumerGroup = "order-timeout-consumer-group",
    selectorExpression = "timeout",
    maxReconsumeTimes = 3  // 最大重试次数（包括首次消费），超过后进入死信队列
)
@RequiredArgsConstructor
public class OrderTimeoutConsumer implements RocketMQListener<String> {

    private final OrderApplicationService orderApplicationService;

    @Override
    public void onMessage(String message) {
        log.info("收到订单超时未支付消息: {}", message);
        
        try {
            // 解析消息
            SeckillOrderTimeoutMessage timeoutMessage = JSON.parseObject(message, SeckillOrderTimeoutMessage.class);
            
            // 调用应用层服务处理订单超时（包含订单状态更新和库存回滚）
            orderApplicationService.handleOrderTimeout(timeoutMessage);
            
            log.info("订单超时处理成功，orderId: {}, orderType: {}", 
                timeoutMessage.getOrderId(), timeoutMessage.getOrderType());
            
        } catch (Exception e) {
            log.error("订单超时处理失败，message: {}", message, e);
            // 抛出异常，触发 MQ 重试机制
            throw new RuntimeException("订单超时处理失败", e);
        }
    }
}
