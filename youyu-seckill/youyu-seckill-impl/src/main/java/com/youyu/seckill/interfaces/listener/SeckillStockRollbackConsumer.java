package com.youyu.seckill.interfaces.listener;

import com.alibaba.fastjson2.JSON;
import com.youyu.seckill.api.dto.SeckillStockRollbackMessage;
import com.youyu.seckill.domain.service.SeckillStockDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 秒杀库存回滚消费者（接口层）
 * <p>
 * 职责：
 * 1. 监听订单超时未支付消息
 * 2. 回滚Redis库存
 * 3. 回滚用户限购数量
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

    private final SeckillStockDomainService stockDomainService;

    @Override
    public void onMessage(String message) {
        log.info("收到秒杀库存回滚消息: {}", message);
        
        try {
            // 解析消息
            SeckillStockRollbackMessage rollbackMessage = JSON.parseObject(message, SeckillStockRollbackMessage.class);
            
            // 回滚库存
            stockDomainService.rollbackStock(rollbackMessage.getProductId(), rollbackMessage.getQuantity());
            
            // 回滚用户限购数量
            stockDomainService.rollbackUserPurchase(
                rollbackMessage.getUserId(), 
                rollbackMessage.getProductId(), 
                rollbackMessage.getQuantity()
            );
            
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
