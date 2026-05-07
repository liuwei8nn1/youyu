package com.youyu.product.interfaces.listener;

import com.alibaba.fastjson2.JSON;
import com.youyu.order.api.dto.NormalStockRollbackMessage;
import com.youyu.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 普通订单库存回滚消费者（接口层）
 * <p>
 * 职责：
 * 1. 监听订单超时消息
 * 2. 调用领域服务回滚数据库库存
 * 3. 保证最终一致性
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "normal-stock-rollback-topic",
    consumerGroup = "normal-stock-rollback-consumer-group",
    selectorExpression = "rollback"
)
@RequiredArgsConstructor
public class NormalStockRollbackConsumer implements RocketMQListener<String> {

    private final ProductRepository productRepository;

    @Override
    public void onMessage(String message) {
        log.info("收到普通订单库存回滚消息，message: {}", message);
        
        try {
            // 1. 解析消息
            NormalStockRollbackMessage rollbackMessage = JSON.parseObject(message, NormalStockRollbackMessage.class);
            
            log.info("开始处理普通订单库存回滚，orderId: {}, productId: {}, quantity: {}", 
                rollbackMessage.getOrderId(), 
                rollbackMessage.getProductId(), 
                rollbackMessage.getQuantity());
            
            // 2. 调用仓储回滚库存
            productRepository.rollbackStock(rollbackMessage.getProductId(), rollbackMessage.getQuantity());
            
            log.info("普通订单库存回滚成功，orderId: {}, productId: {}, quantity: {}", 
                rollbackMessage.getOrderId(), 
                rollbackMessage.getProductId(), 
                rollbackMessage.getQuantity());
            
        } catch (Exception e) {
            log.error("普通订单库存回滚失败，message: {}", message, e);
            // 抛出异常，触发 MQ 重试机制
            throw new RuntimeException("库存回滚失败", e);
        }
    }
}
