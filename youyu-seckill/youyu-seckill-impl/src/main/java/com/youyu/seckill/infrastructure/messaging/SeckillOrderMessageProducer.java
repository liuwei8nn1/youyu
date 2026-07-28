package com.youyu.seckill.infrastructure.messaging;

import com.alibaba.fastjson2.JSON;
import com.youyu.framework.mq.compensation.application.provider.ReliableMessageProducer;
import com.youyu.order.api.dto.SeckillOrderMessage;
import com.youyu.common.model.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 秒杀订单消息生产者（基础设施层）
 * <p>
 * 职责：
 * 1. 发送秒杀订单消息到 RocketMQ（使用框架层的可靠消息生产者）
 * 2. 支持异步发送模式（高性能）
 * 3. 发送失败自动记录到补偿表，由框架层的定时任务重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderMessageProducer {

    private final SnowflakeIdGenerator snowflakeIdGenerator;
    
    /**
     * 框架层提供的可靠消息生产者
     * 自动处理消息发送失败时的补偿逻辑
     */
    private final ReliableMessageProducer reliableMessageProducer;

    /**
     * Topic：秒杀订单主题
     */
    private static final String TOPIC = "seckill-order-topic";

    /**
     * Tag：消息标签
     */
    private static final String TAG = "create-order";

    /**
     * 生成订单ID
     *
     * @return 订单ID
     */
    public String generateOrderId() {
        return String.valueOf(snowflakeIdGenerator.nextId());
    }

    /**
     * 发送秒杀订单消息（异步发送，高性能）
     * <p>
     * 优势：
     * - 不阻塞主线程，提高吞吐量
     * - 使用框架层的 ReliableMessageProducer，自动处理失败补偿
     * <p>
     * 注意：
     * - MQ发送失败时不回滚库存（避免少卖）
     * - 失败消息自动记录到补偿表，由框架层的定时任务重试
     *
     * @param orderId    订单ID
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   购买数量
     * @param amount     订单金额
     * @param activityId 活动ID
     */
    public void send(String orderId, Long userId, Long productId, Integer quantity, BigDecimal amount, Long activityId) {
        SeckillOrderMessage message = new SeckillOrderMessage(orderId, userId, productId, quantity, amount, activityId);
        String messageBody = JSON.toJSONString(message);
        
        // 使用框架层的异步发送，自动处理失败补偿
        // - 生产环境：真实发送到 MQ，失败时记录补偿表
        // - 本地开发：Mock 实现，只记录日志
        reliableMessageProducer.sendAsync(
            TOPIC,
            TAG,
            orderId,  // 使用 orderId 作为 messageId
            messageBody,
            (sendFail) -> {
                // 补偿记录保存失败时的回调
                log.error("【严重】秒杀订单补偿记录保存失败，orderId: {}", orderId, sendFail.e());
                // TODO: 可以触发告警或人工介入
            }
        );
    }
}
