package com.youyu.starter.autoconfigure.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.youyu.framework.cache.redis.RedisUtil;

/**
 * Redis 自动配置类
 * <p>
 * 职责：
 * 1. 初始化 RedisUtil 静态工具类
 */
@Slf4j
@Configuration
public class CacheAutoConfiguration implements InitializingBean {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    RedissonClient redissonClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisUtil.setClient(redissonClient);
        RedisUtil.setStringRedisTemplate(stringRedisTemplate);
        log.info("===========>>>>>>> RedisUtil 初始化完成");
    }
}
