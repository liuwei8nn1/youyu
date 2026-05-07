package com.youyu.order.infrastructure.messaging;

import com.alibaba.fastjson2.JSON;
import com.youyu.framework.mq.compensation.application.provider.ReliableMessageProducer;
import com.youyu.seckill.api.dto.SeckillStockRollbackMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单库存回滚消息生产者（基础设施层）
 * <p>
 * 职责：
 * 1. 发送秒杀订单库存回滚消息到 MQ（使用框架层的可靠消息生产者）
 * 2. 由 seckill-service 消费并回滚 Redis 库存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillStockRollbackMessageProducer {

    /**
     * 框架层提供的可靠消息生产者
     * 自动处理消息发送失败时的补偿逻辑
     */
    private final ReliableMessageProducer reliableMessageProducer;

    /**
     * Topic：秒杀库存回滚主题
     */
    private static final String STOCK_ROLLBACK_TOPIC = "seckill-stock-rollback-topic";

    /**
     * Tag：消息标签
     */
    private static final String ROLLBACK_TAG = "rollback";

    /**
     * 发送秒杀订单库存回滚消息
     * <p>
     * 当秒杀订单超时未支付时，通知 seckill-service 回滚 Redis 中的库存和限购数量
     *
     * @param orderId    订单ID
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   购买数量
     * @param activityId 活动ID
     */
    public void sendSeckillStockRollbackMessage(Long orderId, Long userId, Long productId, 
                                                 Integer quantity, Long activityId) {
        try {
            SeckillStockRollbackMessage rollbackMessage = new SeckillStockRollbackMessage(
                orderId, userId, productId, quantity, activityId
            );

            String messageId = String.valueOf(orderId);
            String messageBody = JSON.toJSONString(rollbackMessage);
            
            // 使用框架层的同步发送，自动处理失败补偿
            reliableMessageProducer.sendSync(
                STOCK_ROLLBACK_TOPIC,
                ROLLBACK_TAG,
                messageId,
                messageBody,
                (sendFail) -> {
                    // 补偿记录保存失败时的回调
                    log.error("【严重】秒杀库存回滚补偿记录保存失败，orderId: {}", orderId, sendFail.e());
                }
            );

            log.info("秒杀库存回滚消息发送成功，orderId: {}, activityId: {}", orderId, activityId);
        } catch (Exception e) {
            log.error("秒杀库存回滚消息发送失败，orderId: {}", orderId, e);
            // 抛出异常，触发上层重试
            throw new RuntimeException("秒杀库存回滚消息发送失败", e);
        }
    }
}
