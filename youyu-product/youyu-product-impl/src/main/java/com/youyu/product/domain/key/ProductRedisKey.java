package com.youyu.product.domain.key;

import com.youyu.framework.cache.redis.RedisKeys;

/**
 * 商品业务 Redis Key
 *
 * @since 2026/8/7
 */
public interface ProductRedisKey {

    String PREFIX = "product";
    /**
     * 商品库存 Key（用于防超卖）
     *
     * @param productId 商品ID
     * @return product:stock:{productId}
     */
    static String calcStockKey(Long productId) {
        return PREFIX + RedisKeys.SEPARATOR + "stock" + RedisKeys.SEPARATOR + productId;
    }
}
