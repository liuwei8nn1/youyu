package com.youyu.seckill.interfaces.listener;

import com.alibaba.fastjson2.JSON;
import com.youyu.seckill.api.dto.SeckillStockRollbackMessage;
import com.youyu.seckill.application.service.SeckillStockRollbackApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 秒杀库存回滚消息消费者（接口层）
 * <p>
 * 职责：
 * 1. 监听订单超时未支付消息
 * 2. 解析消息并委托给应用层处理
 * 3. 异常时触发 MQ 重试
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "seckill-stock-rollback-topic",
    consumerGroup = "seckill-stock-rollback-consumer-group",
    selectorExpression = "rollback"
)
@Profile("!dev") // 关键：非 dev 环境才加载
@RequiredArgsConstructor
public class SeckillStockRollbackConsumer implements RocketMQListener<String> {

    private final SeckillStockRollbackApplicationService stockRollbackService;

    @Override
    public void onMessage(String message) {
        log.info("收到秒杀库存回滚消息: {}", message);
        
        try {
            SeckillStockRollbackMessage rollbackMessage = JSON.parseObject(message, SeckillStockRollbackMessage.class);
            stockRollbackService.rollback(rollbackMessage);
            
            log.info("秒杀库存回滚成功，orderId: {}, productId: {}, userId: {}", 
                rollbackMessage.getOrderId(), 
                rollbackMessage.getProductId(), 
                rollbackMessage.getUserId());
            
        } catch (Exception e) {
            log.error("秒杀库存回滚失败，message: {}", message, e);
            // 抛出异常，触发 MQ 重试机制
            throw new RuntimeException("库存回滚失败", e);
        }
    }
}
