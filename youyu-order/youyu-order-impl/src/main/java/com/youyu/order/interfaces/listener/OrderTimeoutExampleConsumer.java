package com.youyu.order.interfaces.listener;

import com.alibaba.fastjson2.JSON;
import com.youyu.order.api.dto.SeckillOrderTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单超时消费者（带重试次数控制示例）
 * <p>
 * 演示如何获取和控制 MQ 重试次数
 * <p>
 * 关键点：
 * 1. 使用 MessageExt 作为参数类型（而非 String）
 * 2. 通过 messageExt.getReconsumeTimes() 获取重试次数
 * 3. 可以根据重试次数做不同的业务处理
 * 4. 超过指定次数后可以主动停止重试
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "order-timeout-demo-topic",
    consumerGroup = "order-timeout-demo-consumer-group",
    selectorExpression = "*",
    maxReconsumeTimes = 5  // 最大重试 5 次
)
@RequiredArgsConstructor
public class OrderTimeoutExampleConsumer implements RocketMQListener<MessageExt> {

    /**
     * 自定义最大重试次数（可以小于注解配置的 maxReconsumeTimes）
     */
    private static final int MAX_RETRY_TIMES = 3;

    @Override
    public void onMessage(MessageExt messageExt) {
        // 1. 获取重试次数（从 0 开始，0 表示首次消费）
        int reconsumeTimes = messageExt.getReconsumeTimes();
        
        // 2. 获取消息 ID（用于日志追踪）
        String msgId = messageExt.getMsgId();
        
        // 3. 获取消息体
        String body = new String(messageExt.getBody());
        
        log.info("收到订单超时消息 [msgId: {}, 重试次数: {}]: {}", msgId, reconsumeTimes, body);
        
        try {
            // 4. 解析消息
            SeckillOrderTimeoutMessage timeoutMessage = JSON.parseObject(body, SeckillOrderTimeoutMessage.class);
            
            // 5. 根据重试次数做不同处理
            handleWithRetryControl(timeoutMessage, reconsumeTimes, msgId);
            
            log.info("订单超时消息处理成功 [msgId: {}, orderId: {}]", msgId, timeoutMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("订单超时消息处理失败 [msgId: {}, 重试次数: {}]", msgId, reconsumeTimes, e);
            
            // 6. 判断是否超过自定义的最大重试次数
            if (reconsumeTimes >= MAX_RETRY_TIMES) {
                log.warn("达到最大重试次数 {}，不再重试，记录到人工处理队列 [msgId: {}]", MAX_RETRY_TIMES, msgId);
                
                // 这里可以：
                // - 记录到数据库的人工处理表
                // - 发送告警通知
                // - 写入死信日志
                
                // 注意：如果不抛异常，消息会被认为消费成功，不会继续重试
                // 如果希望进入 RocketMQ 的死信队列，需要继续抛异常
                return;  // 不抛异常，停止重试
            }
            
            // 7. 未达到最大重试次数，抛出异常触发重试
            log.info("第 {} 次重试失败，将触发下次重试 [msgId: {}]", reconsumeTimes + 1, msgId);
            throw new RuntimeException("订单超时处理失败，等待重试", e);
        }
    }

    /**
     * 根据重试次数做不同的业务处理
     *
     * @param timeoutMessage 超时消息
     * @param reconsumeTimes 当前重试次数
     * @param msgId          消息 ID
     */
    private void handleWithRetryControl(SeckillOrderTimeoutMessage timeoutMessage, 
                                        int reconsumeTimes, String msgId) {
        log.info("处理订单超时 [orderId: {}, 重试次数: {}]", timeoutMessage.getOrderId(), reconsumeTimes);
        
        // 示例：根据重试次数执行不同的逻辑
        switch (reconsumeTimes) {
            case 0:
                // 首次消费
                log.info("首次处理订单超时，orderId: {}", timeoutMessage.getOrderId());
                processOrderTimeout(timeoutMessage);
                break;
                
            case 1:
                // 第 1 次重试
                log.warn("第 1 次重试处理订单超时，orderId: {}", timeoutMessage.getOrderId());
                processOrderTimeout(timeoutMessage);
                break;
                
            case 2:
                // 第 2 次重试
                log.error("第 2 次重试处理订单超时，可能需要检查系统状态，orderId: {}", timeoutMessage.getOrderId());
                // 可以添加额外的检查逻辑
                checkSystemStatus();
                processOrderTimeout(timeoutMessage);
                break;
                
            default:
                // 更多次重试
                log.error("多次重试仍失败，记录详细诊断信息，orderId: {}", timeoutMessage.getOrderId());
                recordDiagnosticInfo(timeoutMessage, msgId, reconsumeTimes);
                processOrderTimeout(timeoutMessage);
                break;
        }
    }

    /**
     * 处理订单超时核心逻辑
     *
     * @param timeoutMessage 超时消息
     */
    private void processOrderTimeout(SeckillOrderTimeoutMessage timeoutMessage) {
        // TODO: 实际的订单超时处理逻辑
        // 例如：
        // - 更新订单状态
        // - 回滚库存
        // - 发送通知等
        
        log.info("执行订单超时处理逻辑，orderId: {}, orderType: {}", 
            timeoutMessage.getOrderId(), timeoutMessage.getOrderType());
        
        // 模拟业务处理
        // orderApplicationService.handleOrderTimeout(timeoutMessage);
    }

    /**
     * 检查系统状态（示例）
     */
    private void checkSystemStatus() {
        log.info("检查系统状态：数据库连接、Redis 连接、下游服务状态等");
        // TODO: 实现系统状态检查逻辑
    }

    /**
     * 记录诊断信息（示例）
     *
     * @param timeoutMessage   超时消息
     * @param msgId            消息 ID
     * @param reconsumeTimes   重试次数
     */
    private void recordDiagnosticInfo(SeckillOrderTimeoutMessage timeoutMessage, 
                                      String msgId, int reconsumeTimes) {
        log.warn("记录诊断信息 - msgId: {}, orderId: {}, 重试次数: {}, 时间: {}", 
            msgId, timeoutMessage.getOrderId(), reconsumeTimes, System.currentTimeMillis());
        
        // TODO: 可以将诊断信息记录到：
        // - 数据库的诊断表
        // - ELK 日志系统
        // - 监控系统
    }
}
