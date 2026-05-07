package com.youyu.framework.antishake.annotation;

/**
 * 防抖缓存类型枚举
 */
public enum CacheType {
    
    /**
     * 使用全局配置的缓存类型
     * 在 application.yml 中通过 app.anti-shake.cache-type 配置
     */
    DEFAULT,
    
    /**
     * 仅使用本地缓存（Caffeine）
     * 适合单机部署场景，性能最优
     */
    LOCAL,
    
    /**
     * 仅使用分布式缓存（Redis）
     * 适合集群部署场景，保证多实例间的一致性
     */
    REDIS
}
