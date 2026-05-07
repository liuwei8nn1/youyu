package com.youyu.seckill.domain.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.youyu.framework.cache.redis.RedisUtil;
import com.youyu.framework.cache.redis.RedisKeyBuilder;
import com.youyu.seckill.domain.model.SeckillActivityAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 秒杀库存领域服务（领域层）
 * <p>
 * 职责：
 * 1. 使用 Lua 脚本保证库存扣减的原子性（防超卖）
 * 2. 提供库存查询和回滚能力
 * 3. 管理用户购买记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillStockDomainService {

    /**
     * 本地缓存，用于缓存秒杀活动信息
     * 过期时间：10分钟
     * 最大容量：1000
     */
    private final Cache<Long, SeckillActivityAggregate> activityLocalCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    /**
     * 本地缓存，用于用户频率限制（第1层快速拦截）
     * 过期时间：5秒
     * 最大容量：100000（支持1000个商品 × 100个并发用户）
     * <p>
     * 内存优化策略：
     * - Key使用短前缀 "f:" 代替 "seckill:freq:"
     * - Value使用 Byte 代替 Boolean
     * - 单条记录约120字节，10万条约12MB
     * - 5秒自动过期，不会累积
     */
    private final Cache<String, Byte> userFrequencyLocalCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(100000)
            .build();

    /**
     * 库存扣减和用户购买记录 Lua 脚本（原子操作）
     * <p>
     * 脚本逻辑：
     * 1. 检查库存是否充足
     * 2. 检查用户购买是否超过限制
     * 3. 如果都满足，则扣减库存并记录用户购买数量
     * 4. 返回剩余库存
     * <p>
     * KEYS[1]: 库存 Key
     * KEYS[2]: 用户购买记录 Key
     * ARGV[1]: 扣减数量
     * ARGV[2]: 用户限购数量
     */
    private static final String DEDUCT_STOCK_AND_RECORD_PURCHASE_LUA_SCRIPT = """
            local stockKey = KEYS[1]
            local userKey = KEYS[2]
            local quantity = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            
            -- 检查库存
            local currentStock = tonumber(redis.call('GET', stockKey) or '0')
            if currentStock < quantity then
                return -1
            end
            
            -- 检查用户购买数量
            local purchased = tonumber(redis.call('GET', userKey) or '0')
            if purchased + quantity > limit then
                return -2
            end
            
            -- 扣减库存
            redis.call('DECRBY', stockKey, quantity)
            
            -- 记录用户购买数量
            redis.call('INCRBY', userKey, quantity)
            
            -- 设置用户购买记录过期时间（24小时）
            redis.call('EXPIRE', userKey, 86400)
            
            return currentStock - quantity
            """;

    /**
     * 原子扣减库存和记录用户购买（使用 Lua 脚本防超卖）
     * <p>
     * 核心优势：
     * - Lua 脚本在 Redis 中原子执行，避免并发竞争
     * - 检查和扣减在同一事务中，不会出现超卖
     * - 一次IO操作完成库存扣减和用户购买记录，提高性能
     * - 性能极高，支持高并发场景
     *
     * @param productId 商品ID
     * @param userId    用户ID
     * @param quantity  扣减数量
     * @param limit     用户限购数量
     * @return 剩余库存，-1 表示库存不足，-2 表示超过限购
     */
    public Long deductStockAndRecordPurchase(Long productId, Long userId, Integer quantity, Integer limit) {
        log.info("开始扣减秒杀库存并记录购买，productId: {}, userId: {}, quantity: {}", productId, userId, quantity);

        String stockKey = RedisKeyBuilder.Seckill.stock(productId);
        String userKey = RedisKeyBuilder.Seckill.userLimit(userId, productId);
        List<String> keys = List.of(stockKey, userKey);
        List<String> args = List.of(quantity.toString(), limit.toString());

        Long remainingStock = RedisUtil.executeLuaScript(
                DEDUCT_STOCK_AND_RECORD_PURCHASE_LUA_SCRIPT,
                keys,
                args,
                Long.class
        );

        if (remainingStock == null) {
            log.warn("秒杀操作失败，productId: {}, userId: {}", productId, userId);
            return -1L;
        }

        if (remainingStock == -1) {
            log.warn("秒杀库存不足，productId: {}, userId: {}", productId, userId);
            return -1L;
        }

        if (remainingStock == -2) {
            log.warn("用户已达限购数量，productId: {}, userId: {}", productId, userId);
            return -2L;
        }

        log.info("秒杀库存扣减和购买记录成功，productId: {}, userId: {}, remainingStock: {}", productId, userId, remainingStock);
        return remainingStock;
    }

    /**
     * 回滚库存扣减（补偿机制）
     * <p>
     * 适用场景：订单创建失败时，需要恢复已扣减的库存
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    public void rollbackStock(Long productId, Integer quantity) {
        log.info("开始回滚秒杀库存，productId: {}, quantity: {}", productId, quantity);
        String stockKey = RedisKeyBuilder.Seckill.stock(productId);
        RedisUtil.opsForValue().increment(stockKey, quantity);
        log.info("秒杀库存回滚成功，productId: {}, quantity: {}", productId, quantity);
    }

    /**
     * 查询商品库存
     *
     * @param productId 商品ID
     * @return 当前库存数量
     */
    public Long getStock(Long productId) {
        String stockKey = RedisKeyBuilder.Seckill.stock(productId);
        String stock = RedisUtil.opsForValue().get(stockKey);
        return stock != null ? Long.parseLong(stock) : 0L;
    }

    /**
     * 初始化商品库存（用于活动开始前预热）
     *
     * @param productId 商品ID
     * @param stock     初始库存
     */
    public void initStock(Long productId, Long stock) {
        String stockKey = RedisKeyBuilder.Seckill.stock(productId);
        RedisUtil.opsForValue().set(stockKey, stock.toString());
        log.info("秒杀商品库存初始化成功，productId: {}, stock: {}", productId, stock);
    }

    /**
     * 记录用户购买数量
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  购买数量
     */
    public void recordUserPurchase(Long userId, Long productId, Integer quantity) {
        String userKey = RedisKeyBuilder.Seckill.userLimit(userId, productId);
        RedisUtil.opsForValue().increment(userKey, quantity);
        // 设置过期时间为活动结束后 24 小时
        RedisUtil.template().expire(userKey, 86400, java.util.concurrent.TimeUnit.SECONDS);
        log.info("记录用户购买数量，userId: {}, productId: {}, quantity: {}", userId, productId, quantity);
    }

    /**
     * 回滚用户购买数量（补偿机制）
     * <p>
     * 适用场景：订单超时未支付时，需要恢复用户的限购数量
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    public void rollbackUserPurchase(Long userId, Long productId, Integer quantity) {
        log.info("开始回滚用户购买数量，userId: {}, productId: {}, quantity: {}", userId, productId, quantity);
        String userKey = RedisKeyBuilder.Seckill.userLimit(userId, productId);
        RedisUtil.opsForValue().decrement(userKey, quantity);
        log.info("用户购买数量回滚成功，userId: {}, productId: {}, quantity: {}", userId, productId, quantity);
    }

    /**
     * 检查用户操作频率限制（防止重复点击）
     * <p>
     * 双层限流策略（大厂标准做法）：
     * 1. 第1层：本地缓存快速拦截（90%的请求在此拦截，无IO）
     * 2. 第2层：Redis严格保证（防止多实例下的漏网之鱼）
     * <p>
     * 性能优势：
     * - 大部分请求在本地拦截，无需访问Redis
     * - Redis作为兜底，保证严格的限流效果
     * - 即使本地缓存不一致，也不会影响业务正确性
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return true-允许操作，false-操作过于频繁
     */
    public boolean checkUserFrequencyLimit(Long userId, Long productId) {
        // Redis Key使用完整格式（便于管理和维护）
        String redisLimitKey = RedisKeyBuilder.Seckill.userFrequencyLimit(userId, productId);
        
        // 本地缓存Key使用短格式（节省内存，仅内部使用）
        String localCacheKey = "f:" + userId + ":" + productId;
        
        // 第1层：本地缓存快速检查
        if (userFrequencyLocalCache.getIfPresent(localCacheKey) != null) {
            log.debug("本地缓存拦截重复请求，userId: {}, productId: {}", userId, productId);
            return false;
        }
        
        // 第2层：Redis严格检查（使用完整Key）
        Boolean success = RedisUtil.template().opsForValue()
                .setIfAbsent(redisLimitKey, "1", 5, TimeUnit.SECONDS);
        
        // 如果Redis设置成功，同步到本地缓存（使用短Key节省内存）
        if (Boolean.TRUE.equals(success)) {
            userFrequencyLocalCache.put(localCacheKey, (byte) 1);
        }
        
        return Boolean.TRUE.equals(success);
    }

    /**
     * 查询用户已购买数量
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 已购买数量
     */
    public Integer getUserPurchasedCount(Long userId, Long productId) {
        String userKey = RedisKeyBuilder.Seckill.userLimit(userId, productId);
        String count = RedisUtil.opsForValue().get(userKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    // ==================== 活动缓存管理 ====================

    /**
     * 缓存活动信息到 Redis（用于高并发读取）
     * <p>
     * 使用管道批量处理，减少IO操作
     *
     * @param activity 活动聚合根
     */
    public void cacheActivity(SeckillActivityAggregate activity) {
        Long productId = activity.getProductId();
        String activityKey = RedisKeyBuilder.Seckill.activity(productId);
        String activityJson = JSON.toJSONString(activity);
        String stockKey = RedisKeyBuilder.Seckill.stock(productId);
        
        // 使用管道批量处理
        RedisUtil.execInPipeline(redisOps -> {
            // 缓存活动信息
            redisOps.opsForValue().set(activityKey, activityJson);
            redisOps.expire(activityKey, 86400, java.util.concurrent.TimeUnit.SECONDS);
            
            // 初始化库存
            redisOps.opsForValue().set(stockKey, activity.getStock().toString());
        });
        
        log.info("活动信息和库存缓存到 Redis（管道批量处理），productId: {}", productId);
    }

    /**
     * 从本地缓存和 Redis 获取活动信息
     * <p>
     * 优先级：本地缓存 > Redis > 数据库
     *
     * @param productId 商品ID
     * @return 活动聚合根，不存在返回 null
     */
    public SeckillActivityAggregate getCachedActivity(Long productId) {
        // 1. 先从本地缓存获取
        SeckillActivityAggregate activity = activityLocalCache.getIfPresent(productId);
        if (activity != null) {
            log.info("从本地缓存获取活动信息，productId: {}", productId);
            return activity;
        }
        
        // 2. 从 Redis 获取
        String activityKey = RedisKeyBuilder.Seckill.activity(productId);
        String activityJson = RedisUtil.opsForValue().get(activityKey);
        if (activityJson == null) {
            return null;
        }
        
        activity = JSON.parseObject(activityJson, SeckillActivityAggregate.class);
        
        // 3. 缓存到本地
        if (activity != null) {
            activityLocalCache.put(productId, activity);
            log.info("从Redis获取活动信息并缓存到本地，productId: {}", productId);
        }
        
        return activity;
    }

    /**
     * 删除活动缓存（Redis + 本地缓存）
     *
     * @param productId 商品ID
     */
    public void removeCachedActivity(Long productId) {
        // 删除 Redis 缓存
        String activityKey = RedisKeyBuilder.Seckill.activity(productId);
        RedisUtil.template().delete(activityKey);
        
        // 删除本地缓存
        activityLocalCache.invalidate(productId);
        
        // TODO: 后续集成缓存同步组件时，需要在此处发布缓存失效事件
        // 例如：通过 MQ 或 Redis Pub/Sub 通知其他实例清理本地缓存
        // eventPublisher.publishEvent(new CacheInvalidationEvent(productId));
        
        log.info("活动缓存已删除（Redis + 本地缓存），productId: {}", productId);
    }
}
