package com.youyu.framework.antishake.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.youyu.framework.antishake.cache.AntiShakeCache;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 Caffeine 的本地防抖缓存实现
 * <p>
 * 特点：
 * - 高性能：无网络开销，纯内存操作
 * - 适合单机部署场景
 * - 使用 expireAfterWrite 策略，写入后固定时间过期
 */
@Slf4j
public class CaffeineAntiShakeCache implements AntiShakeCache {
    
    /**
     * Caffeine 缓存实例
     * - initialCapacity: 初始容量 1024
     * - maximumSize: 最大容量 10000
     * - expireAfterWrite: 写入后过期（实际过期时间由 tryAcquire 动态控制）
     */
    private final Cache<String, Long> cache = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10000)
            .build();
    
    @Override
    public boolean tryAcquire(String key, long intervalMs) {
        Long expireTime = cache.getIfPresent(key);
        long now = System.currentTimeMillis();
        
        if (expireTime != null && expireTime > now) {
            // key 存在且未过期，被限流
            log.debug("防抖限制：key={}, 剩余时间={}ms", key, expireTime - now);
            return false;
        }
        
        // 设置新的过期时间
        long newExpireTime = now + intervalMs;
        cache.put(key, newExpireTime);
        log.debug("防抖放行：key={}, 过期时间={}ms", key, intervalMs);
        return true;
    }
}
