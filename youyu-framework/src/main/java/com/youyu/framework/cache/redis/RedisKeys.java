package com.youyu.framework.cache.redis;


public interface RedisKeys {

	String SEPARATOR = ":";
	String LOCK_PREFIX = "lock";
	String RATE_LIMIT_PREFIX = "ratelimit";

	/**
	 * 流的 key，STREAM 类型 </br>
	 *
	 * < $prefixKey:stream: > < 消息的详情 >
	 */
	String KEY_STREAM = "stream";

	/**
	 * 延时消息的 key，ZSET 类型 </br>
	 *
	 * < $prefixKey:delayed: >  < $messageId > < 延时时间戳 >
	 */
	String KEY_DELAYED = "delayed";
	/**
	 * 延时消息的 key，HASH 类型， 保存的是延时消息的详情 </br>
	 *
	 * < $prefixKey:delayed:message:messageId > < 消息的详情 KV 保存 >
	 */
	String KEY_DELAYED_MESSAGE = "delayed:message";

	/**
	 * @see #KEY_DELAYED_MESSAGE
	 */
	static String calcDelayedMessageKey(String prefixKey, String messageId) {
		return prefixKey + SEPARATOR + KEY_DELAYED_MESSAGE + SEPARATOR + messageId;
	}

	/**
	 * @see #KEY_DELAYED
	 */
	static String calcDelayedKey(String prefixKey) {
		return prefixKey + SEPARATOR + KEY_DELAYED;
	}

	/**
	 * @see #KEY_STREAM
	 */
	static String calcStreamKey(String prefixKey) {
		return prefixKey + SEPARATOR + KEY_STREAM;
	}

	/**
	 * 自定义 Key 构建
	 *
	 * @param parts Key 片段
	 * @return 拼接后的 Key
	 */
	static String custom(String... parts) {
		return String.join(SEPARATOR, parts);
	}


	/**
	 * 订单处理锁
	 *
	 * @param orderId 订单ID
	 * @return lock:order:{orderId}
	 */
	static String calcLockOrderKey(Long orderId) {
		return LOCK_PREFIX + SEPARATOR + "order" + SEPARATOR + orderId;
	}

	/**
	 * 库存扣减锁
	 *
	 * @param productId 商品ID
	 * @return lock:stock:{productId}
	 */
	static String calcLockStockKey(Long productId) {
		return LOCK_PREFIX + SEPARATOR + "stock" + SEPARATOR + productId;
	}

	/**
	 * 用户操作锁（防止重复提交）
	 *
	 * @param userId 用户ID
	 * @return lock:user:{userId}
	 */
	static String calcLockUserKey(Long userId) {
		return LOCK_PREFIX + SEPARATOR + "user" + SEPARATOR + userId;
	}

	/**
	 * 接口限流 Key
	 *
	 * @param api    接口路径
	 * @param userId 用户ID
	 * @return ratelimit:{api}:{userId}
	 */
	static String calcRateLimitByApiAndUserKey(String api, Long userId) {
		return RATE_LIMIT_PREFIX + SEPARATOR + api + SEPARATOR + userId;
	}

	/**
	 * IP 限流 Key
	 *
	 * @param ip IP地址
	 * @return ratelimit:ip:{ip}
	 */
	static String calcRateLimitByIpKey(String ip) {
		return RATE_LIMIT_PREFIX + SEPARATOR + "ip" + SEPARATOR + ip;
	}

}
