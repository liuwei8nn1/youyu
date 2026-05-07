package com.youyu.framework.warn.distinct;

import com.youyu.framework.warn.core.WarnDistinctManager;
import org.springframework.cache.CacheManager;

/**
 * 基于本地内存Cache的告警去重实现
 * <p>
 * 适用于单机部署场景，使用Caffeine缓存实现60秒去重
 */
public class LocalWarnDistinctManager implements WarnDistinctManager {

	final CacheManager cacheManager;

	public LocalWarnDistinctManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean canSend(String msgKey) {
		return cacheManager.getCache(CACHE_NAME).putIfAbsent(msgKey, "1") == null;
	}

}
