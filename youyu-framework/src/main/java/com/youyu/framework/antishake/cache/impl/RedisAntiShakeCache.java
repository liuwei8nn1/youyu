package com.youyu.framework.antishake.cache.impl;

import com.youyu.framework.antishake.cache.AntiShakeCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式防抖缓存实现
 * <p>
 * 特点：
 * - 支持集群部署：多实例间共享防抖状态
 * - 使用 SETNX 原子操作保证并发安全
 * - 适合分布式场景
 */
@Slf4j
public class RedisAntiShakeCache implements AntiShakeCache {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public RedisAntiShakeCache(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public boolean tryAcquire(String key, long intervalMs) {
        Boolean isAbsent = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", intervalMs, TimeUnit.MILLISECONDS);
        
        if (Boolean.TRUE.equals(isAbsent)) {
            // 设置成功，允许执行
            log.debug("防抖放行（Redis）：key={}, 过期时间={}ms", key, intervalMs);
            return true;
        } else {
            // key 已存在，被限流
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            log.debug("防抖限制（Redis）：key={}, 剩余时间={}ms", key, ttl);
            return false;
        }
    }
}
