package com.youyu.order.infrastructure.messaging;

import com.alibaba.fastjson2.JSON;
import com.youyu.framework.mq.compensation.application.provider.ReliableMessageProducer;
import com.youyu.order.api.dto.SeckillOrderTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单超时延时消息生产者（基础设施层）
 * <p>
 * 职责：
 * 1. 发送订单超时延时消息到 MQ（使用框架层的可靠消息生产者）
 * 2. 由 order-timeout-consumer 消费并处理超时逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutMessageProducer {

    /**
     * 框架层提供的可靠消息生产者
     * 自动处理消息发送失败时的补偿逻辑
     */
    private final ReliableMessageProducer reliableMessageProducer;

    /**
     * Topic：订单超时主题
     */
    private static final String ORDER_TIMEOUT_TOPIC = "order-timeout-topic";

    /**
     * Tag：消息标签
     */
    private static final String TIMEOUT_TAG = "timeout";

    /**
     * 发送订单超时延时消息
     * <p>
     * 当订单创建成功后，发送延时消息，在指定时间后检查订单是否支付
     * <p>
     * RocketMQ 默认延时级别说明：
     * 1=1s, 2=5s, 3=10s, 4=30s, 5=1m, 6=2m, 7=3m, 8=4m, 9=5m, 10=6m,
     * 11=7m, 12=8m, 13=9m, 14=10m, 15=20m, 16=30m, 17=1h, 18=2h
     *
     * @param orderId    订单ID
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   购买数量
     * @param orderType  订单类型：NORMAL-普通订单，SECKILL-秒杀订单
     * @param activityId 活动ID（仅秒杀订单有值）
     * @param delayLevel 延时级别（1-18），默认 9 对应 5 分钟
     */
    public void sendOrderTimeoutMessage(Long orderId, Long userId, Long productId, 
                                         Integer quantity, String orderType, 
                                         Long activityId, int delayLevel) {
        try {
            SeckillOrderTimeoutMessage timeoutMessage = new SeckillOrderTimeoutMessage(
                orderId, userId, productId, quantity, orderType, activityId
            );

            String messageId = "timeout-" + orderId;
            String messageBody = JSON.toJSONString(timeoutMessage);
            
            // 使用框架层的同步延时发送，自动处理失败补偿
            reliableMessageProducer.sendSyncWithDelay(
                ORDER_TIMEOUT_TOPIC,
                TIMEOUT_TAG,
                messageId,
                messageBody,
                delayLevel,
                (sendFail) -> {
                    // 补偿记录保存失败时的回调
                    log.error("【严重】订单超时延时消息补偿记录保存失败，orderId: {}", orderId, sendFail.e());
                }
            );

            log.info("订单超时延时消息发送成功，orderId: {}, orderType: {}, delayLevel: {}", 
                orderId, orderType, delayLevel);
        } catch (Exception e) {
            log.error("订单超时延时消息发送失败，orderId: {}", orderId, e);
            // 抛出异常，触发上层重试
            throw new RuntimeException("订单超时延时消息发送失败", e);
        }
    }

    /**
     * 发送订单超时延时消息（默认 5 分钟）
     *
     * @param orderId    订单ID
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   购买数量
     * @param orderType  订单类型：NORMAL-普通订单，SECKILL-秒杀订单
     * @param activityId 活动ID（仅秒杀订单有值）
     */
    public void sendOrderTimeoutMessage(Long orderId, Long userId, Long productId, 
                                         Integer quantity, String orderType, Long activityId) {
        // 默认 5 分钟延时（delayLevel = 9）
        sendOrderTimeoutMessage(orderId, userId, productId, quantity, orderType, activityId, 9);
    }
}
