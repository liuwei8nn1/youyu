package com.youyu.seckill.application.service;

import java.math.BigDecimal;

import com.youyu.common.model.Result;
import com.youyu.seckill.application.dto.SeckillOrderResponse;
import com.youyu.seckill.domain.aggregate.SeckillActivity;
import com.youyu.seckill.domain.service.SeckillStockDomainService;
import com.youyu.seckill.infrastructure.messaging.SeckillOrderMessageProducer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 秒杀订单应用服务（应用层）
 * <p>
 * 职责：
 * 1. 处理秒杀商品的下单流程
 * 2. 使用 Redis Lua 脚本原子扣减库存（同步）
 * 3. Feign 同步调用订单服务创建订单
 * 4. 适用于高并发秒杀场景，注重性能和快速响应
 */
@Slf4j
@Service
public class SeckillOrderApplicationService {

    @Resource
    private SeckillStockDomainService stockDomainService;
    @Resource
    private SeckillOrderMessageProducer messageProducer;

    /**
     * 秒杀商品下单（Redis + MQ 异步）
     * <p>
     * 核心流程：
     * 1. 用户级别限流检查（防止重复点击）
     * 2. 查询秒杀活动并校验
     * 3. 缓存秒杀价格到 Redis（供订单消费者使用）
     * 4. Redis Lua 脚本原子扣减库存（同步，快速失败）
     * 5. 记录用户购买数量
     * 6. 生成订单ID
     * 7. 发送 MQ 消息（异步创建订单）
     * 8. 立即返回排队结果
     * <p>
     * 优势：
     * - 高性能：Redis 扣减库存，QPS 可达数万
     * - 快速响应：用户无需等待订单创建完成
     * - 削峰填谷：MQ 异步处理，保护数据库
     * - 防重放：用户级别限流，避免重复请求
     * <p>
     * 注意：
     * - 最终一致性：订单可能延迟创建
     * - 需要补偿机制：处理 MQ 消费失败的情况
     *
     * @param userId     用户ID
     * @param productId  商品ID
     * @param quantity   购买数量
     * @return 秒杀订单响应（包含订单ID和状态）
     */
    public Result<SeckillOrderResponse> createSeckillOrder(Long userId, Long productId, Integer quantity) {
        log.info("秒杀订单创建开始，userId: {}, productId: {}, quantity: {}", userId, productId, quantity);

        // 1. 用户级别限流
        if (!stockDomainService.checkUserFrequencyLimit(userId, productId)) {
            return Result.error("操作过于频繁，请稍后重试");
        }

        // 2. 获取活动并校验时间
        SeckillActivity activity = stockDomainService.getCachedActivity(productId);
        activity.assertActive();

        // 3. 获取秒杀价格（聚合根自校验）
        BigDecimal seckillPrice = activity.getValidatedSeckillPrice();

        // 4. 原子扣减库存（失败由全局异常处理器统一转 Result）
        stockDomainService.deductStockAndRecordPurchase(productId, userId, quantity, activity.getLimitPerUser());

        // 5. 生成订单ID + 发送 MQ
        String orderId = messageProducer.generateOrderId();
        try {
            messageProducer.send(orderId, userId, productId, quantity, seckillPrice, activity.getId());
            log.info("秒杀订单消息发送成功，orderId: {}", orderId);
            return Result.success(SeckillOrderResponse.of(orderId, "排队中，请稍后查询结果"));
        } catch (Exception e) {
            log.error("秒杀订单消息发送失败，回滚库存，orderId: {}", orderId, e);
            stockDomainService.rollbackStockAndPurchase(productId, userId, quantity);
            return Result.error("系统繁忙，请稍后重试");
        }
    }
}
