package com.youyu.starter.autoconfigure.warn;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.youyu.framework.warn.config.WarnProperties;
import com.youyu.framework.warn.core.*;
import com.youyu.framework.warn.distinct.LocalWarnDistinctManager;
import com.youyu.framework.warn.distinct.RedisWarnDistinctManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 告警模块自动配置类
 * <p>
 * 职责：
 * 1. 通过Spring Boot SPI机制自动装配
 * 2. 根据配置条件化创建Bean
 * 3. 支持本地缓存和Redis两种去重方式
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WarnProperties.class)
@ConditionalOnProperty(prefix = "warn", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WarnAutoConfiguration {

	/**
	 * 创建告警通道Bean
	 */
	@Bean
	@ConditionalOnMissingBean(MsgWarnChannel.class)
	public MsgWarnChannel msgWarnChannel(WarnProperties properties, WarnDistinctManager distinctManager) {
		log.info("===========>>>>>>> 初始化告警通道：平台={}, URL={}", properties.getPlatform(), properties.getUrl());
		return WarnChannelFactory.create(properties, distinctManager);
	}

	/**
	 * 默认使用本地缓存去重
	 */
	@Bean
	@ConditionalOnClass(CacheManager.class)
	@ConditionalOnMissingBean(WarnDistinctManager.class)
	public WarnDistinctManager warnDistinctManager(@Autowired(required = false) CacheManager cacheManager) {
		if (cacheManager == null) {
			log.info("===========>>>>>>> 未找到CacheManager，创建Caffeine本地缓存用于告警去重");
			CaffeineCacheManager cm = new CaffeineCacheManager();
			final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
					.initialCapacity(32)
					.maximumSize(1024)
					.expireAfterWrite(60, TimeUnit.SECONDS);
			cm.setCaffeine(caffeine);
			cacheManager = cm;
		}
		return new LocalWarnDistinctManager(cacheManager);
	}

	/**
	 * Redis去重实现（需显式配置warn.distinct.type=redis）
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(name = "warn.distinct.type", havingValue = "redis")
	@ConditionalOnClass(StringRedisTemplate.class)
	static class RedisDistinctConfig {

		@Bean
		public WarnDistinctManager redisWarnDistinctManager(StringRedisTemplate redisTemplate, WarnProperties properties) {
			int timeout = properties.getDistinct().getTimeout();
			log.info("使用Redis告警去重，超时时间：{}秒", timeout);
			return new RedisWarnDistinctManager(redisTemplate, timeout);
		}
	}
}
