package com.youyu.starter.autoconfigure.id;

import com.youyu.common.model.SnowflakeIdGenerator;
import com.youyu.common.util.StringUtil;
import com.youyu.framework.context.Env;
import com.youyu.framework.id.SnowflakeWorkerIdAllocator;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * ID 生成器自动配置
 * <p>
 * 职责：
 * 1. Snowflake WorkerId 分配器
 * 2. Snowflake ID 生成器
 */
@Configuration
@AutoConfigureAfter(org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration.class)
@Slf4j
public class IdGeneratorAutoConfiguration {

    /**
     * redis中雪花相关key的前缀，如不填，默认使用appName,
     * 使用场景：（要通过配置该值来实现，前提是redis也是同一个）
     *   不同的服务下要使workerId也要不能一样的场景
     */
    @Value("${snowflake.prefix:}")
    String snowflakePrefix;

    @Value("${snowflake.dataCenterId:0}")
    Integer dataCenterId;
    /**
     * null或者小于0则通过 {@link SnowflakeWorkerIdAllocator} 去获取，否则使用配置值（不能大于最大值）
     */
    @Value("${snowflake.workerId:}")
    Integer workerId;

    SnowflakeWorkerIdAllocator snowflakeWorkerIdAllocator;

    /**
     * 创建 SnowflakeWorkerIdAllocator 实例
     * <p>
     * 负责从 Redis 中自动分配 WorkerId
     *
     * @param stringRedisTemplate Redis模板
     * @return SnowflakeWorkerIdAllocator实例
     */
    @ConditionalOnBean(StringRedisTemplate.class)
    @Bean
    public SnowflakeWorkerIdAllocator snowflakeWorkerIdAllocator(StringRedisTemplate stringRedisTemplate) {
        log.info("===========>>>>>>> 初始化 SnowflakeWorkerIdAllocator");
        if(StringUtil.isEmpty(snowflakePrefix)){
            return snowflakeWorkerIdAllocator = new SnowflakeWorkerIdAllocator(Env.getAppName(), stringRedisTemplate);
        }
        return snowflakeWorkerIdAllocator = new SnowflakeWorkerIdAllocator(snowflakePrefix, stringRedisTemplate);
    }

    /**
     * 创建雪花算法ID生成器实例
     * <p>
     * 依赖 SnowflakeWorkerIdAllocator 提供的 WorkerId 和 DataCenterId
     *
     * @param snowflakeWorkerIdAllocator WorkerId分配器
     * @return SnowflakeIdGenerator实例
     */
    @ConditionalOnBean(StringRedisTemplate.class)
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeWorkerIdAllocator snowflakeWorkerIdAllocator) {
        int wId = workerId == null || workerId < 0 ?  snowflakeWorkerIdAllocator.allocateWorkerId(dataCenterId, SnowflakeIdGenerator.getMaxWorkerId()) : workerId;
        log.info("===========>>>>>>> 初始化 SnowflakeIdGenerator - WorkerId: {}, DataCenterId: {}", wId, dataCenterId);
        return new SnowflakeIdGenerator(wId, dataCenterId);
    }

    @PreDestroy
    public void destroy(){
        if (snowflakeWorkerIdAllocator != null) {
            snowflakeWorkerIdAllocator.release();
        }
    }

}
