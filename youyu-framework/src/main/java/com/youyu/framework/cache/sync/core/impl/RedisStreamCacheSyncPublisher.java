package com.youyu.framework.cache.sync.core.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.youyu.framework.cache.sync.alert.*;
import com.youyu.framework.cache.sync.config.CacheSyncProperties;
import com.youyu.framework.cache.redis.RedisKeys;
import com.youyu.framework.cache.sync.metrics.CacheSyncMetrics;
import com.youyu.framework.cache.sync.core.CacheSyncPublisher;
import com.youyu.framework.cache.sync.core.InternalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.*;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class RedisStreamCacheSyncPublisher implements CacheSyncPublisher, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(RedisStreamCacheSyncPublisher.class);

	private final StringRedisTemplate stringRedisTemplate;
	private final CacheSyncProperties properties;
	private final CacheSyncMetrics metrics;
	private final CacheSyncAlertHandler alertHandler;

	// 用于批量裁剪的计数器
	private volatile int messageCount = 0;
	// 批量裁剪的阈值
	private static final int TRIM_THRESHOLD = 100;
	// 最大重试次数
	private static final int MAX_RETRY_COUNT = 3;
	// 重试延迟（毫秒），每次重试递增
	private static final long RETRY_DELAY_MS = 1000;
	
	// 延时消息处理线程引用
	private Thread delayedMessageProcessorThread;
	// 运行状态标志
	private final AtomicBoolean running = new AtomicBoolean(true);

	public RedisStreamCacheSyncPublisher(StringRedisTemplate stringRedisTemplate,
	                                     CacheSyncProperties properties,
	                                     CacheSyncMetrics metrics,
	                                     CacheSyncAlertHandler alertHandler) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.properties = properties;
		this.metrics = metrics;
		this.alertHandler = alertHandler;
		if(properties.isEnableDelayedHandler()){
			// 启动延时消息处理线程
			startDelayedMessageProcessor(properties.getDelayedHandlerTimeoutMs());
		}
	}

	@Override
	public void publishCacheClean(String type, String subType, String cacheKey, int delayedMillis, Map<String, String> metadata, boolean afterTransaction) {
		if (!properties.isEnabled()) {
			return;
		}
		Map<String, String> message = new HashMap<>(metadata);
		message.put(InternalMessage.CACHE_KEY, cacheKey);
		message.put(InternalMessage.TYPE, type);
		message.put(InternalMessage.SUB_TYPE, subType);
		if(delayedMillis > 0){
			message.put(InternalMessage.DELAY, String.valueOf(delayedMillis));
		}
		message.put(InternalMessage.TIMESTAMP, String.valueOf(System.currentTimeMillis()));

		try {
			if (afterTransaction) {
				boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
				if (synchronizationActive) {
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
						@Override
						public void afterCommit() {
							handleCacheClean(message);
						}
					});
				} else {
					handleCacheClean(message);
				}
			} else {
				handleCacheClean(message);
			}

		} catch (Exception e) {
			metrics.incrementFailedMessages();
			// 触发告警
			if (properties.isEnableAlert()) {
				CacheSyncAlertEvent event = CacheSyncAlertEvent.of(
						CacheSyncAlertType.PUBLISH_FAILED,
						String.format("Failed to publish cache clean message: type=%s, subType=%s, cacheKey=%s", type, subType, cacheKey),
						InternalMessage.of(message),
						e,
						LocalDateTime.now(),
						properties.getInstanceId(),
						null,
						null
				);
				alertHandler.handle(event);
			}
			throw new RuntimeException("Failed to publish cache clean message", e);
		}
	}

	private void handleCacheClean(Map<String, String> message) {
		// 检查是否有延时参数
		String delayStr = message.get(InternalMessage.DELAY);
		if (delayStr != null) {
			long delay = Long.parseLong(delayStr);
			if (delay > 0) {
				// 计算到期时间
				long expireTime = System.currentTimeMillis() + delay;
				// 生成唯一的消息ID（去掉UUID中的横杠）
				String messageId = UUID.randomUUID().toString().replace("-", "");
				// 将消息存储到 Redis Hash 中
				String hashKey = RedisKeys.calcDelayedMessageKey(properties.getPrefixKey(), messageId);
				stringRedisTemplate.opsForHash().putAll(hashKey, message);
				// 将消息ID添加到 zset 中
				stringRedisTemplate.opsForZSet().add(RedisKeys.calcDelayedKey(properties.getPrefixKey()), messageId, expireTime);
				return;
			}
		}
		// 立即执行
		cacheClean(RedisKeys.calcStreamKey(properties.getPrefixKey()), message);
	}

	private void cacheClean(String streamKey, Map<String, String> message) {
		// 使用 RedisTemplate 发布消息
		stringRedisTemplate.opsForStream().add(streamKey, message);
		metrics.incrementPublishedMessages();

		// 当消息计数达到阈值时，执行裁剪
		if (++messageCount >= TRIM_THRESHOLD) {
			trimStream(streamKey);
			messageCount = 0;
		}
	}

	/**
	 * 修剪 Stream 到配置的最大长度
	 * 使用近似修剪（~）以提高性能
	 */
	private void trimStream(String streamKey) {
		try {
			stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
				byte[] keyBytes = stringRedisTemplate.getStringSerializer().serialize(streamKey);
				if (keyBytes != null) {
					// 使用 XTRIM 命令，APPROXIMATE 模式提高性能
					connection.streamCommands().xTrim(
							keyBytes,
							properties.getMaxLen(),
							true  // true 表示使用 APPROXIMATE 模式
					);
				}
				return null;
			});
		} catch (Exception e) {
			// 修剪失败不影响主流程，记录日志即可
			// 可以考虑增加 metrics 统计
		}
	}

	/**
	 * 启动延时消息处理线程
	 * 使用 Lua 脚本保证多实例并发安全，使用虚拟线程（JDK 21+）
	 */
	private void startDelayedMessageProcessor(int timeoutMs) {
		Runnable processor = () -> {
			while (running.get() && !Thread.currentThread().isInterrupted()) {
				try {
					if (!handlerDelayedMessage()) {
						// 没有到期消息，短暂休眠后继续
						Thread.sleep(timeoutMs);
					}
				} catch (InterruptedException e) {
					// 线程被中断，退出循环
					Thread.currentThread().interrupt();
					break;
				} catch (Exception e) {
					// 其他异常也不影响线程运行
				}
			}
			logger.info("Delayed message processor thread stopped");
		};

		// 使用虚拟线程（JDK 21+）
		delayedMessageProcessorThread = Thread.ofVirtual()
				.name("delayed-message-processor")
				.unstarted(processor);
		delayedMessageProcessorThread.start();
		logger.info("===========>>>>>>> Delayed message message processor thread started");
	}

	private boolean handlerDelayedMessage() {
		long currentTime = System.currentTimeMillis();
		// 使用 Lua 脚本原子地获取并移除到期消息，避免多实例重复处理
		String luaScript = """
				local msgs = redis.call('zrangebyscore', KEYS[1], '-inf', ARGV[1], 'LIMIT', 0, 50)
				if #msgs > 0 then
				  for i, msgId in ipairs(msgs) do
				    redis.call('zrem', KEYS[1], msgId)
				  end
				end
				return msgs
				""";
		String delayedKey = RedisKeys.calcDelayedKey(properties.getPrefixKey());
		List<String> messageIds = stringRedisTemplate.execute(
				connection -> {
					byte[] keyBytes = stringRedisTemplate.getStringSerializer().serialize(delayedKey);
					byte[] currentTimeBytes = stringRedisTemplate.getStringSerializer().serialize(String.valueOf(currentTime));

					Object result = connection.eval(
							luaScript.getBytes(),
							ReturnType.fromJavaType(List.class),
							1,
							keyBytes,
							currentTimeBytes
					);

					if (result instanceof List) {
						@SuppressWarnings("unchecked")
						List<byte[]> byteList = (List<byte[]>) result;
						List<String> stringList = new ArrayList<>();
						for (byte[] bytes : byteList) {
							if (bytes != null) {
								stringList.add(new String(bytes));
							}
						}
						return stringList;
					}
					return Collections.emptyList();
				},
				true
		);

		if (messageIds != null && !messageIds.isEmpty()) {
			for (String messageId : messageIds) {
				processSingleDelayedMessage(messageId, delayedKey);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 处理单条延时消息
	 */
	private void processSingleDelayedMessage(String messageId, String delayedKey) {
		try {
			String baseMessageId = extractBaseMessageId(messageId);
			// 从 Redis Hash 中获取消息内容
			String messageHashKey = RedisKeys.calcDelayedMessageKey(properties.getPrefixKey(), baseMessageId);
			Map<Object, Object> hashEntries = stringRedisTemplate.opsForHash().entries(messageHashKey);
			if (!hashEntries.isEmpty()) {
				// 转换为 Map<String, String>
				Map<String, String> message = new HashMap<>();
				for (Map.Entry<Object, Object> entry : hashEntries.entrySet()) {
					if (entry.getKey() instanceof String key && entry.getValue() instanceof String value) {
						message.put(key, value);
					}
				}
				// 发布消息
				cacheClean(RedisKeys.calcStreamKey(properties.getPrefixKey()), message);
			}
			// 成功后删除临时消息，避免 Redis 数据积累
			stringRedisTemplate.delete(messageHashKey);
			logger.debug("延时消息 {} 处理成功", messageId);
		} catch (Exception e) {
			// 处理失败，进行重试
			handleDelayedMessageFailure(messageId, delayedKey, e);
		}
	}

	/**
	 * 处理延时消息失败的情况
	 */
	private void handleDelayedMessageFailure(String messageId, String delayedKey, Exception e) {
		// 解析重试次数
		int retryCount = extractRetryCount(messageId);
		String baseMessageId = extractBaseMessageId(messageId);
		
		logger.error("处理延时消息 {} 失败（第 {} 次重试）", messageId, retryCount, e);
		
		if (retryCount >= MAX_RETRY_COUNT) {
			// 超过最大重试次数，放弃处理
			logger.error("延时消息 {} 已达到最大重试次数 {}，放弃处理", messageId, MAX_RETRY_COUNT);
			metrics.incrementFailedMessages();
			
			// 触发告警
			if (properties.isEnableAlert()) {
				CacheSyncAlertEvent event = CacheSyncAlertEvent.of(
								CacheSyncAlertType.DELAYED_MESSAGE_RETRY_EXHAUSTED,
								String.format("Delayed message retry exhausted after %d attempts: %s", MAX_RETRY_COUNT, messageId),
								InternalMessage.of(baseMessageId),
								e,
								LocalDateTime.now(),
								properties.getInstanceId(),
								null,
								null
						);
				alertHandler.handle(event);
			}
		} else {
			// 生成新的 messageId，追加重试计数
			int newRetryCount = retryCount + 1;
			String newMessageId = baseMessageId + "-" + newRetryCount;
			
			// 计算重试时间（指数退避：1秒、2秒、3秒）
			long retryTime = System.currentTimeMillis() + RETRY_DELAY_MS * newRetryCount;
			
			// 重新加入 ZSET
			stringRedisTemplate.opsForZSet().add(delayedKey, newMessageId, retryTime);
			
			logger.warn("延时消息将在 {}ms 后重试（新ID: {}，第 {}/{} 次）",
					RETRY_DELAY_MS * newRetryCount, newMessageId, newRetryCount, MAX_RETRY_COUNT);
		}
	}

	/**
	 * 从 messageId 中提取重试次数
	 * @param messageId 消息ID，格式：baseId 或 baseId-N
	 * @return 重试次数，0 表示首次尝试
	 */
	private int extractRetryCount(String messageId) {
		int lastDashIndex = messageId.lastIndexOf('-');
		if (lastDashIndex == -1) {
			// 没有横杠，说明是首次尝试
			return 0;
		}
		
		// 提取横杠后面的数字
		String retryPart = messageId.substring(lastDashIndex + 1);
		try {
			return Integer.parseInt(retryPart);
		} catch (NumberFormatException e) {
			// 解析失败，视为首次尝试
			logger.warn("无法解析消息ID {} 的重试次数，视为首次尝试", messageId);
			return 0;
		}
	}

	/**
	 * 从 messageId 中提取基础 ID（不含重试计数）
	 * @param messageId 消息ID，格式：baseId 或 baseId-N
	 * @return 基础消息ID
	 */
	private String extractBaseMessageId(String messageId) {
		int lastDashIndex = messageId.lastIndexOf('-');
		if (lastDashIndex == -1) {
			// 没有横杠，整个就是基础ID
			return messageId;
		}
		
		// 检查横杠后面是否是数字
		String suffix = messageId.substring(lastDashIndex + 1);
		try {
			Integer.parseInt(suffix);
			// 是数字，返回横杠前面的部分
			return messageId.substring(0, lastDashIndex);
		} catch (NumberFormatException e) {
			// 不是数字，说明横杠是基础ID的一部分（理论上不会发生）
			return messageId;
		}
	}

	/**
	 * 应用关闭时停止延时消息处理线程
	 */
	@Override
	public void destroy() {
		logger.info("Stopping delayed message processor...");
		
		// 设置运行状态为 false
		running.set(false);
		
		// 中断线程（虚拟线程会快速响应中断）
		if (delayedMessageProcessorThread != null) {
			delayedMessageProcessorThread.interrupt();
			logger.info("Delayed message processor thread interrupted");
		}
	}

}
