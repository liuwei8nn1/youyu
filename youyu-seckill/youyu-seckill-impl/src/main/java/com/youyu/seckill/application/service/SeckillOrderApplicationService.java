package com.youyu.seckill.application.service;

import java.math.BigDecimal;

import com.youyu.common.model.Result;
import com.youyu.seckill.application.dto.SeckillOrderResponse;
import com.youyu.seckill.domain.model.SeckillActivityAggregate;
import com.youyu.seckill.domain.repository.SeckillActivityRepository;
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
    private SeckillActivityRepository activityRepository;
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

        // 0. 用户级别限流检查（防止5秒内重复提交）
        if (!stockDomainService.checkUserFrequencyLimit(userId, productId)) {
            log.warn("用户操作过于频繁，userId: {}, productId: {}", userId, productId);
            return Result.error("操作过于频繁，请稍后重试");
        }

        // 1. 从 Redis 获取活动信息（高性能）
        SeckillActivityAggregate activity = stockDomainService.getCachedActivity(productId);
        
        // 如果 Redis 中没有，降级查数据库
        if (activity == null) {
            log.warn("Redis 中未找到活动信息，降级查数据库，productId: {}", productId);
            activity = activityRepository.findByProductId(productId);
            if (activity == null) {
                log.warn("秒杀活动不存在，productId: {}", productId);
                return Result.error("秒杀活动不存在");
            }
            // 缓存到 Redis
            stockDomainService.cacheActivity(activity);
        }

        // 2. 校验活动时间
        if (!activity.isActive()) {
            if (activity.isNotStarted()) {
                return Result.error("秒杀活动未开始");
            } else {
                return Result.error("秒杀活动已结束");
            }
        }

        // 3. 从活动对象中获取秒杀价格（已在 Redis 缓存中）
        BigDecimal seckillPrice = activity.getSeckillPrice();
        if (seckillPrice == null) {
            log.error("活动价格为空，productId: {}", productId);
            return Result.error("活动配置错误");
        }
        log.info("从活动缓存中获取秒杀价格，productId: {}, price: {}", productId, seckillPrice);

        // 4. Redis Lua 原子扣减库存和记录购买（同步，快速失败）
        Long remainingStock = stockDomainService.deductStockAndRecordPurchase(
                productId, userId, quantity, activity.getLimitPerUser());
        
        if (remainingStock == -1) {
            log.warn("秒杀库存不足，userId: {}, productId: {}", userId, productId);
            return Result.error("秒杀失败，库存不足");
        }
        
        if (remainingStock == -2) {
            log.warn("用户已达限购数量，userId: {}, productId: {}, limit: {}",
                    userId, productId, activity.getLimitPerUser());
            return Result.error("已达限购数量");
        }

        // 7. 生成订单ID
        String orderId = messageProducer.generateOrderId();

        // 8. 发送 MQ 消息（异步创建订单）
        try {
            messageProducer.send(orderId, userId, productId, quantity, seckillPrice, activity.getId());
            log.info("秒杀订单消息发送成功，orderId: {}", orderId);
            SeckillOrderResponse response = SeckillOrderResponse.of(orderId, "排队中，请稍后查询结果");
            return Result.success(response);
        } catch (Exception e) {
            log.error("秒杀订单消息发送失败，回滚库存，orderId: {}", orderId, e);
            // 补偿：回滚 Redis 库存
            stockDomainService.rollbackStock(productId, quantity);
            return Result.error("系统繁忙，请稍后重试");
        }
    }
}
