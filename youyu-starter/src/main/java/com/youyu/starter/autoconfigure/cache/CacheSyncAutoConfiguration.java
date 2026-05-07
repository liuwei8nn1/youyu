package com.youyu.starter.autoconfigure.cache;

import com.youyu.framework.cache.sync.alert.*;
import com.youyu.framework.cache.sync.metrics.CacheSyncMetrics;
import com.youyu.framework.cache.sync.config.CacheSyncProperties;
import com.youyu.framework.cache.sync.core.*;
import com.youyu.framework.cache.sync.core.impl.RedisStreamCacheSyncPublisher;
import com.youyu.framework.warn.core.MsgWarnChannel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CacheSyncProperties.class)
@ConditionalOnProperty(prefix = "cache.sync", name = "enabled", havingValue = "true")
@AutoConfigureAfter(org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration.class)
public class CacheSyncAutoConfiguration {

    @Bean
    public CacheSyncMetrics cacheSyncMetrics() {
        return new CacheSyncMetrics();
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public CacheSyncPublisher cacheSyncPublisher(StringRedisTemplate stringRedisTemplate,
                                                 CacheSyncProperties properties,
                                                 CacheSyncMetrics metrics,
                                                 CacheSyncAlertHandler alertHandler) {
        return new RedisStreamCacheSyncPublisher(stringRedisTemplate, properties, metrics, alertHandler);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public CacheSyncConsumer cacheSyncConsumer(StringRedisTemplate stringRedisTemplate,
                                               CacheSyncProperties properties,
                                               CacheSyncMetrics metrics,
                                               CacheSyncAlertHandler alertHandler) {
        return new CacheSyncConsumer(stringRedisTemplate, properties, metrics, alertHandler);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public com.youyu.framework.cache.sync.aspect.LocalCacheEvictAspect localCacheEvictAspect(CacheSyncPublisher cacheSyncPublisher) {
        return new com.youyu.framework.cache.sync.aspect.LocalCacheEvictAspect(cacheSyncPublisher);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public CacheSyncAlertHandler cacheSyncAlertHandler(@Autowired(required = false) MsgWarnChannel msgWarnChannel) {
	    if (msgWarnChannel == null) {
		    return new DefaultCacheSyncAlertHandler();
	    }
        return new CacheSyncAlertHandler(){
            @Override
            public void handle(CacheSyncAlertEvent event) {
                msgWarnChannel.sendBugMsgAsync(event.buildLogMessage());
            }
        };
    }

    @Autowired(required = false)
    public void registerMetrics(MeterRegistry meterRegistry, CacheSyncMetrics metrics, CacheSyncProperties properties) {
        if (meterRegistry != null && properties.isEnableMetrics()) {
            metrics.bindTo(meterRegistry);
        }
    }

}
