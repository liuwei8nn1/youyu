package com.youyu.framework.cache.sync.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.youyu.framework.cache.redis.RedisKey;
import com.youyu.framework.cache.sync.alert.*;
import com.youyu.framework.cache.sync.config.CacheSyncProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.youyu.framework.cache.sync.metrics.CacheSyncMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class CacheSyncConsumer implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(CacheSyncConsumer.class);

	private final StringRedisTemplate stringRedisTemplate;
	private final CacheSyncProperties properties;
	private final CacheSyncMetrics metrics;
	private final CacheSyncAlertHandler alertHandler;

	// 存储消费线程的引用，用于停止时中断
	private final List<Thread> consumerThreads = new ArrayList<>();
	private final ScheduledExecutorService scheduledExecutorService;
	private final AtomicBoolean running = new AtomicBoolean(true);
	private String consumerGroup;
	private String consumerName;
	private ApplicationContext applicationContext;

	// 存储所有Handler的映射: type -> subType -> List<Handler>
	private final Map<String, Map<String, List<CacheCleanHandler>>> handlerMapping = new ConcurrentHashMap<>();
	
	// 缓存 findHandlers 的查询结果
	// Key: (type, subType) 配对，Value: 匹配的 Handler 列表
	// 由于 handlerMapping 初始化后不变，缓存可以永久有效
	private final Cache<Pair<String, String>, List<CacheCleanHandler>> handlerCache = 
			Caffeine.newBuilder()
					.expireAfterAccess(Duration.ofHours(10))
					.maximumSize(2048)  // 最多缓存1000种不同的(type, subType)组合
					.build();

	public CacheSyncConsumer(StringRedisTemplate stringRedisTemplate,
	                         CacheSyncProperties properties,
	                         CacheSyncMetrics metrics,
	                         CacheSyncAlertHandler alertHandler) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.properties = properties;
		this.metrics = metrics;
		this.alertHandler = alertHandler;
		// 使用虚拟线程的定时任务调度器（JDK 21+），避免浪费平台线程资源
		this.scheduledExecutorService = Executors.newScheduledThreadPool(1, 
				Thread.ofVirtual().name("cache-sync-pending-scanner", 0).factory());
	}

	public void start() {
		if (!properties.isEnabled()) {
			return;
		}
		if (!running.get()) {
			return;
		}

		// 创建消费者组
		createConsumerGroup();

		// 启动消费线程（直接创建虚拟线程，无需池化）
		running.set(true);
		for (int i = 0; i < properties.getThreadPoolSize(); i++) {
			String consumerName = this.consumerName + "-" + i;
			Thread thread = Thread.ofVirtual()
					.name("cache-sync-consumer-" + consumerName)
					.unstarted(() -> consumeMessages(consumerName));
			thread.start();
			consumerThreads.add(thread);
		}
		log.info("===========>>>>>>> 缓存同步消费者启动 {} consumer threads", consumerThreads.size());

		// 启动 Pending 消息扫描定时任务（每30秒执行一次）
		scheduledExecutorService.scheduleAtFixedRate(this::scanPendingMessages, 0, 30, TimeUnit.SECONDS);
	}

	public void stop() {
		if (!properties.isEnabled()) {
			return;
		}
		if (!running.get()) {
			return;
		}
		running.set(false);
		// 关闭定时任务线程池
		scheduledExecutorService.shutdownNow();
		// 中断所有消费线程
		for (Thread thread : consumerThreads) {
			thread.interrupt();
		}
	}

	private void createConsumerGroup() {
		try {
			// 确保 Stream 存在
			try {
				// 尝试添加一个空消息来创建 Stream（如果不存在）
				stringRedisTemplate.opsForStream().add(RedisKey.calcStreamKey(properties.getPrefixKey()), new HashMap<>());
			} catch (Exception e) {
				// Stream 可能已经存在，忽略异常
			}

			// 尝试创建消费者组
			try {
				stringRedisTemplate.opsForStream().createGroup(
						RedisKey.calcStreamKey(properties.getPrefixKey()),
						consumerGroup
				);
				logger.info("===========>>>>>>> Created consumer group: {}", consumerGroup);
			} catch (Exception e) {
				// 消费者组已存在，忽略异常
				// logger.debug("===========>>>>>>> Consumer group already exists: {}", consumerGroup);
			}
		} catch (Exception e) {
			logger.error("===========>>>>>>> Error creating consumer group: {}", consumerGroup, e);
			// 触发告警
			if (properties.isEnableAlert()) {
				CacheSyncAlertEvent event = CacheSyncAlertEvent.of(CacheSyncAlertType.CONSUMER_GROUP_CREATE_FAILED, "Failed to create consumer group: " + consumerGroup,
						null, e, LocalDateTime.now(), properties.getInstanceId(), consumerName, null);
				alertHandler.handle(event);
			}
		}
	}

	/**
	 * 消费消息的主方法
	 * 功能：从Redis Stream中读取新消息并处理
	 * <p>
	 * 工作原理：
	 * 1. 从Stream中读取消息（使用block机制，避免轮询）
	 * 2. 解析消息内容（type, subType, cacheKey, metadata）
	 * 3. 调用handleCacheClean处理缓存清理
	 * 4. 确认消息（ack），将消息从pending列表中移除
	 * 5. 更新监控指标
	 * <p>
	 * 适用场景：处理正常的、新到达的缓存清理消息
	 */
	private void consumeMessages(String consumerName) {
		while (running.get() && !Thread.currentThread().isInterrupted()) {
			try {
				String streamKey = RedisKey.calcStreamKey(properties.getPrefixKey());
				List<MapRecord<String, Object, Object>> messages = stringRedisTemplate.opsForStream().read(
						Consumer.from(consumerGroup, consumerName),
						StreamReadOptions.empty()
								.count(properties.getBatchSize())
								.block(Duration.ofSeconds(properties.getBlockSeconds())),
						StreamOffset.create(streamKey, ReadOffset.lastConsumed())
				);

				if (messages != null && !messages.isEmpty()) {
					for (MapRecord<String, Object, Object> record : messages) {
						processMessage(record, streamKey, false);
					}
				}
			} catch (Exception e) {
				if (!running.get()) {
					return;
				}
				logger.error("Error consuming messages", e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	/**
	 * 扫描Pending消息的方法
	 * 功能：扫描并处理超时的Pending消息（可能是由于消费者崩溃等原因未处理的消息）
	 * <p>
	 * 工作原理：
	 * 1. 定期（每30秒）检查Stream中的Pending消息
	 * 2. 认领（claim）超时的消息（超过messageTimeoutMs的消息）
	 * 3. 解析消息内容并处理
	 * 4. 确认消息并更新监控指标
	 * <p>
	 * 适用场景：处理因消费者崩溃、网络中断等原因导致的未处理消息
	 */
	private void scanPendingMessages() {
		if (!running.get()) {
			return;
		}

		try {
			// 确保消费者组存在
			createConsumerGroup();

			String streamKey = RedisKey.calcStreamKey(properties.getPrefixKey());
			PendingMessagesSummary pendingSummary = stringRedisTemplate.opsForStream().pending(
					streamKey,
					consumerGroup
			);
			if (pendingSummary != null) {
				long pendingCount = pendingSummary.getTotalPendingMessages();
				metrics.setPendingSize(pendingCount);

				// 更新 lag 指标
				updateLagMetrics();

				if (pendingCount > 0) {
					// 尝试认领超时消息
					List<MapRecord<String, Object, Object>> claimedMessages =
							stringRedisTemplate.opsForStream().claim(
								streamKey,
								consumerGroup,
								consumerName,
								Duration.ofSeconds(properties.getMessageTimeoutSeconds())
							);

					for (MapRecord<String, Object, Object> record : claimedMessages) {
						processMessage(record, streamKey, true);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error scanning pending messages", e);
			// 触发告警
			if (properties.isEnableAlert()) {
				CacheSyncAlertEvent event = CacheSyncAlertEvent.of(
							CacheSyncAlertType.PENDING_SCAN_ERROR,
							"Error occurred while scanning pending messages",
							null,
							e,
							LocalDateTime.now(),
							properties.getInstanceId(),
							consumerName,
							consumerGroup
					);
				alertHandler.handle(event);
			}
		}
	}

	/**
	 * 统一的消息处理方法，消除重复代码
	 *
	 * @param record     Redis Stream记录
	 * @param streamKey  Stream键名
	 * @param isClaimed  是否为认领的消息（来自pending队列）
	 */
	private void processMessage(MapRecord<String, Object, Object> record, String streamKey, boolean isClaimed) {
		Map<Object, Object> messageData = record.getValue();
		InternalMessage msg = InternalMessage.of(record);
		String messageId = msg.messageId;

		if (!msg.isValid()) {
			stringRedisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, messageId);
			logger.warn("Invalid {} message format: cacheKey={}, type={}, subType={}",
					isClaimed ? "claimed" : "", msg.cacheKey, msg.type, msg.subType);
			return;
		}

		String type = msg.type;
		String subType = msg.subType;
		String cacheKey = msg.cacheKey;
		Integer retrySize = msg.retrySize;

		try {
			// 检查重试次数是否超限
			if (retrySize > properties.getMaxRetry()) {
				discardMessage(streamKey, messageId, msg, isClaimed);
				return;
			}

			// 处理缓存清理
			handleCacheClean(msg);
			// 确认消息
			stringRedisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, messageId);
			metrics.incrementConsumedMessages();

		} catch (Exception e) {
			logger.error("Failed to handle {}cache clean message: type={}, subType={}, cacheKey={}",
					isClaimed ? "claimed " : "", type, subType, cacheKey, e);
			metrics.incrementFailedMessages();

			// 触发告警
			if (properties.isEnableAlert()) {
				CacheSyncAlertType alertType = CacheSyncAlertType.CONSUME_FAILED;
				String errorMsg = String.format("Failed to consume %smessage: type=%s, subType=%s, cacheKey=%s",
						isClaimed ? "claimed " : "", type, subType, cacheKey);
				CacheSyncAlertEvent event = CacheSyncAlertEvent.of(alertType, errorMsg, msg, e,
						LocalDateTime.now(), properties.getInstanceId(), consumerName, consumerGroup);
				alertHandler.handle(event);
			}

			// ACK旧消息并重试
			ackAndRetry(streamKey, messageId, messageData, msg, isClaimed);
		}
	}

	/**
	 * 丢弃超限重试的消息
	 */
	private void discardMessage(String streamKey, String messageId, InternalMessage msg, boolean isClaimed) {
		logger.warn("{} message exceeded max retry size, discarding: type={}, subType={}, cacheKey={}, retrySize={}",
				isClaimed ? "Claimed" : "Message", msg.type, msg.subType, msg.cacheKey, msg.retrySize);
		stringRedisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, messageId);
		metrics.incrementDiscardedMessages();

		if (properties.isEnableAlert()) {
			String errorMsg = String.format("%s discarded after exceeding max retry (%d): type=%s, subType=%s, cacheKey=%s",
					isClaimed ? "Claimed message" : "Message", properties.getMaxRetry(),
					msg.type, msg.subType, msg.cacheKey);
			CacheSyncAlertLevel level = isClaimed ? CacheSyncAlertLevel.ERROR : CacheSyncAlertLevel.WARN;
			CacheSyncAlertEvent event = CacheSyncAlertEvent.of(CacheSyncAlertType.MESSAGE_DISCARDED, level, errorMsg, msg, null, LocalDateTime.now(),
					properties.getInstanceId(), consumerName, consumerGroup);
			alertHandler.handle(event);
		}
	}

	/**
	 * ACK旧消息并重新发送带重试次数的新消息
	 */
	private void ackAndRetry(String streamKey, String messageId, Map<Object, Object> messageData,
							 InternalMessage msg, boolean isClaimed) {
		// ACK旧消息
		stringRedisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, messageId);

		// 重新发送一条新消息，带上重试次数
		try {
			messageData.put(InternalMessage.RETRY_SIZE, String.valueOf(msg.retrySize + 1));
			stringRedisTemplate.opsForStream().add(streamKey, messageData);
			logger.info("Resent {}message with increased retry size: type={}, subType={}, cacheKey={}, retrySize={}",
					isClaimed ? "claimed " : "", msg.type, msg.subType, msg.cacheKey, msg.retrySize + 1);
		} catch (Exception ex) {
			logger.error("Failed to resend {}message: type={}, subType={}, cacheKey={}",
					isClaimed ? "claimed " : "", msg.type, msg.subType, msg.cacheKey, ex);

			// 触发重新发送失败的告警
			if (properties.isEnableAlert()) {
				CacheSyncAlertType alertType = CacheSyncAlertType.MESSAGE_RESEND_FAILED;
				String errorMsg = String.format("Failed to resend %smessage: type=%s, subType=%s, cacheKey=%s",
						isClaimed ? "claimed " : "", msg.type, msg.subType, msg.cacheKey);
				CacheSyncAlertEvent resendEvent = CacheSyncAlertEvent.of(alertType, errorMsg, msg, ex,
						LocalDateTime.now(), properties.getInstanceId(), consumerName, consumerGroup);
				alertHandler.handle(resendEvent);
			}
		}
	}

	/**
	 * 更新 lag 指标
	 * 通过 XINFO GROUPS 命令获取消费者组的 lag 信息
	 */
	private void updateLagMetrics() {
		try {
			String streamKey = RedisKey.calcStreamKey(properties.getPrefixKey());
			// 使用 RedisCallback 直接访问底层连接，执行 XINFO GROUPS 命令
			stringRedisTemplate.execute((RedisCallback<?>) connection -> {
				byte[] keyBytes = stringRedisTemplate.getStringSerializer().serialize(streamKey);
				try {
					// 获取消费者组信息 - 返回 XInfoGroups 对象
					StreamInfo.XInfoGroups groups =
						connection.streamCommands().xInfoGroups(keyBytes);
					if (groups != null) {
						// 查找匹配的消费者组 - 使用迭代器
						for (StreamInfo.XInfoGroup group : groups) {
							// 尝试通过 toString 解析 group 信息
							String groupStr = group.toString();
							if (groupStr.contains("name=" + consumerGroup)) {
								// 从字符串中提取 lag 值（简单方式）
								// 格式类似：XInfoGroup[consumerGroup=group,lag=10,...]
								int lagIndex = groupStr.indexOf("lag=");
								if (lagIndex != -1) {
									int endIndex = groupStr.indexOf(',', lagIndex);
									if (endIndex == -1) {
										endIndex = groupStr.indexOf('}', lagIndex);
									}
									if (endIndex != -1) {
										String lagStr = groupStr.substring(lagIndex + 4, endIndex);
										try {
											long lag = Long.parseLong(lagStr.trim());
											metrics.setLag(lag);
										} catch (NumberFormatException e) {
											logger.debug("Could not parse lag from: {}", lagStr);
										}
									}
								}
								break;
							}
						}
					}
				} catch (Exception e) {
					logger.debug("Failed to get stream group info for lag metrics: {}", e.getMessage());
				}
				return null;
			});
		} catch (Exception e) {
			logger.debug("Error updating lag metrics: {}", e.getMessage());
		}
	}

	/**
	 * 清理离线的消费者组
	 * 通过 xInfoConsumers 命令检查每个消费者的最后活跃时间，清理超时的消费者
	 */
	private void cleanOfflineConsumers() {
		try {
			String streamKey = RedisKey.calcStreamKey(properties.getPrefixKey());
			stringRedisTemplate.execute((RedisCallback<?>) connection -> {
				byte[] keyBytes = stringRedisTemplate.getStringSerializer().serialize(streamKey);
				try {
					// 获取所有消费者信息 - 使用 XInfoConsumers 对象
					StreamInfo.XInfoConsumers consumers =
						connection.streamCommands().xInfoConsumers(keyBytes, consumerGroup);
					if (consumers != null) {
						long timeoutMillis = properties.getOfflineConsumerTimeoutMinutes() * 60 * 1000;
						// 遍历所有消费者 - 使用迭代器
						for (StreamInfo.XInfoConsumer consumer : consumers) {
							String groupName = consumer.groupName();
							String consumerNameVal = consumer.consumerName();
							Duration idleTime = consumer.idleTime();
							long idleMillis = idleTime.toMillis();
							// 如果消费者空闲时间超过阈值，则清理该消费者(是缓存应用，没必要保留)
							if (idleMillis > timeoutMillis) {
								logger.info("Cleaning offline consumer: {}, idle time: {} ms", consumerNameVal, idleMillis);
								try {
									// 直接删除该消费者
									connection.streamCommands().xGroupDelConsumer(keyBytes, groupName, consumerNameVal);
									logger.info("Successfully deleted offline consumer: {}", consumerNameVal);
								} catch (Exception e) {
									logger.error("Error deleting consumer: {}", consumerNameVal, e);
									// 触发告警
									if (properties.isEnableAlert()) {
										CacheSyncAlertEvent event = CacheSyncAlertEvent.of(CacheSyncAlertType.OFFLINE_CONSUMER_CLEANUP_ERROR, String.format("Failed to delete offline consumer: %s", consumerNameVal),
												null, e, LocalDateTime.now(), properties.getInstanceId(), consumerName, consumerGroup);
										alertHandler.handle(event);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					logger.debug("Failed to get consumer info for cleanup: {}", e.getMessage());
				}
				return null;
			});
		} catch (Exception e) {
			logger.debug("Error cleaning offline consumers: {}", e.getMessage());
			// 触发告警（仅在启用告警时）
			if (properties.isEnableAlert()) {
				CacheSyncAlertEvent event = CacheSyncAlertEvent.of(CacheSyncAlertType.OFFLINE_CONSUMER_CLEANUP_ERROR, "Error occurred while cleaning offline consumers",
						null, e, LocalDateTime.now(), properties.getInstanceId(), consumerName, consumerGroup);
				alertHandler.handle(event);
			}
		}
	}

	/**
	 * 处理缓存清理，根据type和subType查找对应的Handler执行
	 */
	private void handleCacheClean(InternalMessage msg) {
		String type = msg.type, subType = msg.subType, cacheKey = msg.cacheKey;
		HashMap<String, String> metadata = msg.metadata;
		List<CacheCleanHandler> handlers = findHandlers(type, subType);

		if (handlers.isEmpty()) {
			logger.warn("No CacheCleanHandler found for type={}, subType={}, cacheKey={}", type, subType, cacheKey);
			return;
		}

		for (CacheCleanHandler handler : handlers) {
			try {
				handler.cacheSync(type, subType, cacheKey, metadata);
				logger.debug("Cache clean handled by {}: type={}, subType={}, cacheKey={}",
						handler.getClass().getSimpleName(), type, subType, cacheKey);
			} catch (Exception e) {
				logger.error("Handler {} failed to process cache clean: type={}, subType={}, cacheKey={}", handler.getClass().getSimpleName(), type, subType, cacheKey, e);
				throw e;
			}
		}
	}

	/**
	 * 根据type和subType查找匹配的Handler列表（带缓存优化）
	 * <p>
	 * 匹配规则（按优先级从高到低）：
	 * 1. 精确匹配 (type, subType) - 最具体
	 * 2. type精确 + subType通配 (*) - 该type下的所有subType
	 * 3. type通配 (*) + subType精确 - 所有type下的该subType
	 * 4. 全部通配 (*, *) - 全局默认Handler
	 * <p>
	 * 示例：
	 * - 查询 ("user", "update") 会匹配：
	 *   1. supportType="user", supportSubType="update" 的Handler
	 *   2. supportType="user", supportSubType="*" 的Handler
	 *   3. supportType="*", supportSubType="update" 的Handler
	 *   4. supportType="*", supportSubType="*" 的Handler
	 * <p>
	 * 性能优化：使用 Caffeine 本地缓存，避免重复计算
	 * 缓存Key使用 Apache Commons Lang 的 ImmutablePair，确保线程安全和正确的哈希行为
	 *
	 * @param type    消息类型
	 * @param subType 消息子类型
	 * @return 匹配的Handler列表（按优先级排序，已去重）
	 */
	private List<CacheCleanHandler> findHandlers(String type, String subType) {
		// 使用 ImmutablePair 作为缓存 key，不可变且线程安全
		Pair<String, String> cacheKey = ImmutablePair.of(type, subType);
		
		// 从缓存中获取，如果不存在则计算并缓存
		return handlerCache.get(cacheKey, _ -> computeHandlers(type, subType));
	}
	
	/**
	 * 实际计算 Handler 列表的方法（无缓存）
	 * <p>
	 * 匹配逻辑说明：
	 * 1. 先查找 type 精确匹配的分组
	 *    - 添加该分组下 subType 精确匹配的Handler
	 *    - 添加该分组下 subType 通配(*)的Handler
	 * 2. 再查找 type 通配(*)的分组
	 *    - 添加该分组下 subType 精确匹配的Handler
	 *    - 添加该分组下 subType 通配(*)的Handler
	 * 3. 使用 LinkedHashSet 自动去重并保持插入顺序
	 * <p>
	 * 注意：
	 * - 同一个Handler可能同时匹配多个规则，但只会返回一次（去重）
	 * - 返回顺序即为匹配优先级顺序
	 *
	 * @param type    消息类型
	 * @param subType 消息子类型
	 * @return 匹配的Handler列表（已去重）
	 */
	private List<CacheCleanHandler> computeHandlers(String type, String subType) {
		// 使用LinkedHashSet保持顺序并去重，避免contains检查的性能问题
		Set<CacheCleanHandler> resultSet = new LinkedHashSet<>();

		// ===== 第一优先级：type 精确匹配 =====
		Map<String, List<CacheCleanHandler>> subTypeMap = handlerMapping.get(type);
		if (subTypeMap != null) {
			// 1.1 精确匹配 (type, subType)
			List<CacheCleanHandler> exactHandlers = subTypeMap.get(subType);
			if (exactHandlers != null) {
				resultSet.addAll(exactHandlers);
			}

			// 1.2 type精确 + subType通配 (*)
			List<CacheCleanHandler> wildcardSubTypeHandlers = subTypeMap.get("*");
			if (wildcardSubTypeHandlers != null) {
				resultSet.addAll(wildcardSubTypeHandlers);
			}
		}

		// ===== 第二优先级：type 通配(*) =====
		Map<String, List<CacheCleanHandler>> wildcardTypeMap = handlerMapping.get("*");
		if (wildcardTypeMap != null) {
			// 2.1 type通配 + subType精确
			List<CacheCleanHandler> wildcardTypeExactSubHandlers = wildcardTypeMap.get(subType);
			if (wildcardTypeExactSubHandlers != null) {
				resultSet.addAll(wildcardTypeExactSubHandlers);
			}

			// 2.2 全部通配 (*, *)
			List<CacheCleanHandler> allWildcardHandlers = wildcardTypeMap.get("*");
			if (allWildcardHandlers != null) {
				resultSet.addAll(allWildcardHandlers);
			}
		}

		// 转换为 ArrayList 返回（LinkedHashSet 保证顺序）
		return new ArrayList<>(resultSet);
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.consumerGroup = properties.getConsumerGroup();
		this.consumerName = properties.getInstanceId();
		// 清理离线的消费者组
		if (properties.isAutoCleanOfflineConsumers()) {
			cleanOfflineConsumers();
		}
		// 把所有实现了 CacheCleanHandler 接口的bean找出来
		Map<String, CacheCleanHandler> handlerBeans = applicationContext.getBeansOfType(CacheCleanHandler.class);

		if (handlerBeans.isEmpty()) {
			logger.warn("No CacheCleanHandler implementation found! Cache sync messages will not be processed.");
			return;
		}

		logger.info("===========>>>>>>> Found {} CacheCleanHandler implementations", handlerBeans.size());

		// 构建映射表
		int handlerCount = 0;
		for (Map.Entry<String, CacheCleanHandler> entry : handlerBeans.entrySet()) {
			CacheCleanHandler handler = entry.getValue();
			String beanName = entry.getKey();

			String supportType = handler.supportType();
			String supportSubType = handler.supportSubType();

			// 处理null值，默认使用*
			if (supportType == null) {
				supportType = "*";
			}
			if (supportSubType == null) {
				supportSubType = "*";
			}

			// 添加到映射表
			handlerMapping
					.computeIfAbsent(supportType, _ -> new HashMap<>())
					.computeIfAbsent(supportSubType, _ -> new ArrayList<>())
					.add(handler);

			handlerCount++;

			logger.info("===========>>>>>>> Registered CacheCleanHandler: beanName={}, supportType={}, supportSubType={}",
					beanName, supportType, supportSubType);
		}

		logger.info("===========>>>>>>> CacheCleanHandler mapping initialized with {} handlers", handlerCount);

		// 启动消费者
		start();
	}

	@Override
	public void destroy() {
		// 停止消费者
		stop();
		// 强制关闭线程池即可
		scheduledExecutorService.shutdownNow();
	}

}
