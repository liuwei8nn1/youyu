package com.youyu.framework.warn.distinct;

import java.util.concurrent.TimeUnit;

import com.youyu.framework.warn.core.WarnDistinctManager;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于Redis缓存的告警去重实现
 * <p>
 * 适用于集群部署场景，使用Redis SETNX实现分布式去重
 */
@Setter
public class RedisWarnDistinctManager implements WarnDistinctManager {

	final StringRedisTemplate redisTemplate;
	/** 过期时间（秒） */
	int timeout = 60;

	public RedisWarnDistinctManager(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public RedisWarnDistinctManager(StringRedisTemplate redisTemplate, int timeout) {
		this.redisTemplate = redisTemplate;
		this.timeout = timeout;
		if (timeout <= 0) {
			throw new IllegalArgumentException("超时时间必须大于0");
		}
	}

	@Override
	public boolean canSend(String msgKey) {
		final Boolean can = redisTemplate.opsForValue().setIfAbsent(CACHE_NAME + msgKey, "1", timeout, TimeUnit.SECONDS);
		return can != null && can;
	}

}
