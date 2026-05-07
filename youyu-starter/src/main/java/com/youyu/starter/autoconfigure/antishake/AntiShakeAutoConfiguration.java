package com.youyu.starter.autoconfigure.antishake;

import com.youyu.framework.antishake.aspect.AntiShakeAspect;
import com.youyu.framework.antishake.cache.AntiShakeCache;
import com.youyu.framework.antishake.cache.impl.CaffeineAntiShakeCache;
import com.youyu.framework.antishake.cache.impl.RedisAntiShakeCache;
import com.youyu.framework.antishake.annotation.CacheType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 防抖组件自动配置类
 * <p>
 * 配置示例（application.yml）：
 * <pre>{@code
 * app:
 *   anti-shake:
 *     cache-type: LOCAL  # DEFAULT | LOCAL | REDIS
 * }</pre>
 */
@Data
@Configuration
@EnableAspectJAutoProxy
@ConfigurationProperties(prefix = "app.anti-shake")
public class AntiShakeAutoConfiguration {
    
    /**
     * 全局默认的缓存类型
     * - DEFAULT：需要显式指定，不能直接设置为 DEFAULT
     * - LOCAL：使用 Caffeine 本地缓存
     * - REDIS：使用 Redis 分布式缓存
     */
    private CacheType cacheType = CacheType.LOCAL;
    
    /**
     * 创建 Caffeine 防抖缓存 Bean
     */
    @Bean
    @ConditionalOnMissingBean(CaffeineAntiShakeCache.class)
    public CaffeineAntiShakeCache caffeineAntiShakeCache() {
        return new CaffeineAntiShakeCache();
    }
    
    /**
     * 创建 Redis 防抖缓存 Bean
     */
    @Bean
    @ConditionalOnMissingBean(RedisAntiShakeCache.class)
    @ConditionalOnClass(StringRedisTemplate.class)
    public RedisAntiShakeCache redisAntiShakeCache(StringRedisTemplate redisTemplate) {
        return new RedisAntiShakeCache(redisTemplate);
    }
    
    /**
     * 创建默认的防抖缓存 Bean
     * <p>
     * 根据配置的 cacheType 决定使用哪种实现
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultAntiShakeCache")
    public AntiShakeCache defaultAntiShakeCache(
            CaffeineAntiShakeCache caffeineAntiShakeCache,
            RedisAntiShakeCache redisAntiShakeCache) {
        
        return switch (cacheType) {
            case LOCAL -> caffeineAntiShakeCache;
            case REDIS -> redisAntiShakeCache;
            case DEFAULT -> {
                // DEFAULT 不应该出现在这里，兜底使用 LOCAL
                yield caffeineAntiShakeCache;
            }
        };
    }
    
    /**
     * 创建防抖 AOP 切面 Bean
     */
    @Bean
    @ConditionalOnMissingBean(AntiShakeAspect.class)
    public AntiShakeAspect antiShakeAspect(
            @Qualifier("caffeineAntiShakeCache") CaffeineAntiShakeCache caffeineAntiShakeCache,
            @Qualifier("redisAntiShakeCache") RedisAntiShakeCache redisAntiShakeCache,
            @Qualifier("defaultAntiShakeCache") AntiShakeCache defaultAntiShakeCache) {
        return new AntiShakeAspect(caffeineAntiShakeCache, redisAntiShakeCache, defaultAntiShakeCache);
    }
}
