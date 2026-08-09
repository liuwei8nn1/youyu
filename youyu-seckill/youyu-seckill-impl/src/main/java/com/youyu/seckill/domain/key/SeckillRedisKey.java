package com.youyu.seckill.domain.key;

import com.youyu.framework.cache.redis.RedisKeys;

/**
 * 秒杀业务 Redis Key
 *
 */
public interface SeckillRedisKey {

    String PREFIX = "seckill";

    /**
     * 秒杀库存 Key（用于防超卖）
     *
     * @param productId 商品ID
     * @return seckill:stock:{productId}
     */
    static String calcStockKey(Long productId) {
        return PREFIX + RedisKeys.SEPARATOR + "stock" + RedisKeys.SEPARATOR + productId;
    }

    /**
     * 用户购买限制 Key
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return seckill:user:{userId}:{productId}
     */
    static String calcUserLimitKey(Long userId, Long productId) {
        return PREFIX + RedisKeys.SEPARATOR + "user" + RedisKeys.SEPARATOR + userId + RedisKeys.SEPARATOR + productId;
    }

    /**
     * 用户操作频率限制 Key（防止重复点击）
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return seckill:freq:{userId}:{productId}
     */
    static String calcUserFrequencyLimitKey(Long userId, Long productId) {
        return PREFIX + RedisKeys.SEPARATOR + "freq" + RedisKeys.SEPARATOR + userId + RedisKeys.SEPARATOR + productId;
    }

    /**
     * 秒杀活动详情 Key
     *
     * @param activityId 活动ID
     * @return seckill:activity:{activityId}
     */
    static String calcActivityKey(Long activityId) {
        return PREFIX + RedisKeys.SEPARATOR + "activity" + RedisKeys.SEPARATOR + activityId;
    }
}
