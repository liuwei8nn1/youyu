package com.youyu.framework.antishake.cache;

/**
 * 防抖缓存抽象接口
 * <p>
 * 提供统一的防抖能力，支持多种实现：
 * - CaffeineAntiShakeCache：基于本地缓存，适合单机场景
 * - RedisAntiShakeCache：基于分布式缓存，适合集群场景
 */
public interface AntiShakeCache {
    
    /**
     * 尝试获取执行权限
     * <p>
     * 如果 key 不存在或已过期，则设置 key 并返回 true（允许执行）
     * 如果 key 存在且未过期，则返回 false（被限流）
     *
     * @param key        防抖 key
     * @param intervalMs 防抖间隔（毫秒）
     * @return true=可以执行，false=被限流
     */
    boolean tryAcquire(String key, long intervalMs);
}
