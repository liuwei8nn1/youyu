package com.youyu.framework.cache.redis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.youyu.common.util.CollectionUtil;
import com.youyu.common.model.Result;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisTxCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 工具类（企业级）
 * <p>
 * 职责：
 * 1. 封装 Redis 高级操作（分布式锁、限流、布隆过滤器、Stream、事务、管道等）
 * 2. 提供静态方法，无需注入即可使用
 * 3. 处理复杂的 Redis 场景和性能优化
 * <p>
 * 初始化方式：
 * - Spring Boot 启动时通过 base-starter 模块自动初始化
 * - 或手动调用 {@link #setClient(RedissonClient)} 和 {@link #setStringRedisTemplate(StringRedisTemplate)}
 * <p>
 * 与 RedisUtils 的区别：
 * - RedisUtils: Spring Bean，适合简单缓存操作，易于测试
 * - RedisUtil: 静态工具类，功能强大，适合企业级复杂场景
 * <p>
 * 典型使用场景：
 * - 分布式锁：{@link #loadInLock(String, Supplier)}
 * - 限流防重：{@link #limitCombo(String, Supplier)}
 * - 布隆过滤器：{@link #getBloomFilter(String, long, double)}
 * - Stream 消息队列：{@link #pollMessages(String, String, int)}
 * - 事务批处理：{@link #execInTransaction(java.util.function.Consumer)}
 * - 管道批处理：{@link #execInPipeline(java.util.function.Consumer)}
 *
 * @since 2026/4/14
 */
@Slf4j
public abstract class RedisUtil {

	public static <T> Supplier<T> wrapTask(Runnable task) {
		return () -> {
			task.run();
			return null;
		};
	}

	static final Cache<String, String> localLockCache = Caffeine.newBuilder().initialCapacity(8).maximumSize(40960).expireAfterAccess(10, TimeUnit.SECONDS).build();

	@Getter
	static RedissonClient client;
	static StringRedisTemplate stringRedisTemplate;
	static HashOperations<String, String, String> redisHash;

	public static void setClient(RedissonClient client) {
		RedisUtil.client = client;
	}

	public static void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
		RedisUtil.stringRedisTemplate = stringRedisTemplate;
		RedisUtil.redisHash = stringRedisTemplate.opsForHash();
	}

	/**
	 * @deprecated 请使用 {@link #template() }替代
	 */
	@Deprecated
	public static StringRedisTemplate getStringRedisTemplate() {
		return stringRedisTemplate;
	}

	public static StringRedisTemplate template() {
		return stringRedisTemplate;
	}

	public static HashOperations<String, String, String> opsForHash() {
		return redisHash;
	}

	public static ValueOperations<String, String> opsForValue() {
		return stringRedisTemplate.opsForValue();
	}

	public static SetOperations<String, String> opsForSet() {
		return stringRedisTemplate.opsForSet();
	}

	public static ZSetOperations<String, String> opsForZSet() {
		return stringRedisTemplate.opsForZSet();
	}

	public static ListOperations<String, String> opsForList() {
		return stringRedisTemplate.opsForList();
	}

	/**
	 * 在分布式锁中执行任务（阻塞式）
	 * <p>
	 * 特点：
	 * - 本地锁 + Redis 锁双重保障，防止重入
	 * - 自动加锁和解锁
	 * - 异常时也会释放锁
	 *
	 * @param key             锁的 Key
	 * @param expireTimeInMs  锁过期时间（毫秒）
	 * @param task            要执行的任务
	 */
	public static void doInLock(String key, long expireTimeInMs, Runnable task) {
		loadInLock(key, expireTimeInMs, wrapTask(task), true);
	}

	/**
	 * 在分布式锁中执行任务（阻塞式，默认30秒超时）
	 *
	 * @param key  锁的 Key
	 * @param task 要执行的任务
	 */
	public static void doInLock(String key, Runnable task) {
		loadInLock(key, 30_000L, wrapTask(task), true);
	}

	/**
	 * 在分布式锁中加载数据（阻塞式）
	 * <p>
	 * 适用场景：缓存击穿防护、并发控制
	 *
	 * @param key  锁的 Key
	 * @param task 要执行的任务
	 * @param <T>  返回值类型
	 * @return 任务执行结果
	 */
	public static <T> T loadInLock(String key, Supplier<T> task) {
		return loadInLock(key, 30_000L, task, true);
	}

	/**
	 * @param unlockSilently 是否静默解锁
	 */
	public static <T> T loadInLock(String key, Supplier<T> task, final boolean unlockSilently) {
		return loadInLock(key, 30_000L, task, unlockSilently);
	}

	public static <T> T loadInLock(String key, long lockTimeInMs, Supplier<T> task) {
		return loadInLock(key, lockTimeInMs, task, true);
	}

	/**
	 * @param lockTimeInMs 默认加锁时间
	 * @param unlockSilently 是否静默解锁
	 */
	public static <T> T loadInLock(String key, long lockTimeInMs, Supplier<T> task, final boolean unlockSilently) {
		final String sharedLoclKey = localLockCache.get(key, Function.identity());
		synchronized (sharedLoclKey) {
			final RLock lock = client.getLock(key);
			try {
				lock.lock(lockTimeInMs, TimeUnit.MILLISECONDS);
				return task.get();
			} finally {
				unlock(lock, key, unlockSilently);
			}
		}
	}

	/**
	 * 限制连续操作（防抖/限流）
	 * <p>
	 * 适用场景：
	 * - 防止用户重复点击
	 * - API 频率限制
	 * - 防刷接口
	 *
	 * @param key             用于标识被限制的连击的 Redis key
	 * @param minIntervalInMs 两次操作的最小时间间隔（毫秒值）
	 * @param task            任务。如果任务报错，则不会视为连击。
	 * @param defaultVal      连续操作被限制时，默认的返回值提供者
	 * @param <T>             返回值类型
	 * @return 任务执行结果或默认值
	 */
	public static <T> T limitCombo(String key, long minIntervalInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		boolean locked = opsForValue().setIfAbsent(key, "1", minIntervalInMs, TimeUnit.MILLISECONDS);
		if (locked) { // 拿到了锁
			try {
				return task.get();
			} catch (RuntimeException e) {
				template().delete(key);
				throw e;
			}
		}
		return defaultVal == null ? null : defaultVal.get();
	}

	/**
	 * 限制连续操作
	 *
	 * @param key 用于标识被限制的连击的 Redis key
	 * @param minIntervalInMs 两次操作的最小时间间隔（毫秒值）
	 * @param task 任务。如果任务报错，则不会视为连击。
	 */
	public static <T> T limitCombo(String key, long minIntervalInMs, Supplier<T> task) {
		@SuppressWarnings("unchecked")
		Supplier<T> defaultLoader = (Supplier<T>) (Supplier<?>) defaultValueLoader;
		return limitCombo(key, minIntervalInMs, task, defaultLoader);
	}

	/**
	 * 3s 内限制连续操作
	 *
	 * @param key 用于标识被限制的连击的 Redis key
	 * @param task 任务。如果任务报错，则不会视为连击。
	 */
	public static <T> T limitCombo(String key, Supplier<T> task) {
		@SuppressWarnings("unchecked")
		Supplier<T> defaultLoader = (Supplier<T>) (Supplier<?>) defaultValueLoader;
		return limitCombo(key, 3000, task, defaultLoader);
	}

	/**
	 * 尝试获取分布式锁并执行任务（非阻塞）
	 * <p>
	 * 特点：
	 * - 如果获取锁失败，立即返回默认值
	 * - 适合快速失败场景
	 *
	 * @param key              锁的 Key
	 * @param waitTime         等待时间（毫秒）
	 * @param expireTimeInMs   锁过期时间（毫秒）
	 * @param task             要执行的任务
	 * @param unlockSilently   是否静默解锁（不抛异常）
	 * @param defaultVal       获取锁失败时的默认值提供者
	 * @param <T>              返回值类型
	 * @return 任务执行结果或默认值
	 */
	public static <T> T attemptInLock(String key, long waitTime, long expireTimeInMs, Supplier<T> task, boolean unlockSilently, @Nullable Supplier<T> defaultVal) {
		final RLock lock = client.getLock(key);
		boolean locked = false;
		try {
			locked = lock.tryLock(waitTime, expireTimeInMs, TimeUnit.MILLISECONDS);
			if (locked) {
				return task.get();
			}
			return defaultVal == null ? null : defaultVal.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			IllegalThreadStateException ex = new IllegalThreadStateException();
			ex.initCause(e);
			throw ex;
		} finally {
			if (locked) {
				unlock(lock, key, unlockSilently);
			}
		}
	}

	public static <T> T attemptInLock(String key, long waitTime, long expireTimeInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, waitTime, expireTimeInMs, task, true, defaultVal);
	}

	public static void unlock(RLock lock, String key, boolean unlockSilently) {
		if (unlockSilently) {
			try {
				lock.unlock();
			} catch (Throwable e) {
				log.warn("Redis分布式锁释放异常：" + key, e);
			}
		} else {
			lock.unlock();
		}
	}

	public static <T> T attemptInLock(String key, long expireTimeInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, 100, expireTimeInMs, task, defaultVal);
	}

	public static <T> T attemptInLock(String key, long expireTimeInMs, Supplier<T> task) {
		return attemptInLock(key, 100, expireTimeInMs, task, null);
	}

	public static <T> T fastAttemptInLock(String key, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, 1, 30_000L, task, defaultVal);
	}

	static Supplier<Result<?>> defaultValueLoader = () -> Result.error("busy");

	public static void setDefaultValueLoader(@NonNull Supplier<Result<?>> defaultValueLoader) {
		RedisUtil.defaultValueLoader = defaultValueLoader;
	}

	public static <T> Result<T> fastAttemptInLock(String key, Supplier<Result<T>> task) {
		@SuppressWarnings("unchecked")
		Supplier<Result<T>> defaultLoader = (Supplier<Result<T>>) (Supplier<?>) defaultValueLoader;
		return attemptInLock(key, 1, 30_000L, task, defaultLoader);
	}

	public static <T> T fastAttemptInLock(String key, long expireTimeInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, 1, expireTimeInMs, task, defaultVal);
	}

	public static <T> T fastAttemptInLock(String key, long expireTimeInMs, Supplier<T> task) {
		return attemptInLock(key, 1, expireTimeInMs, task, null);
	}

	/**
	 * 拥有指定独占期的快速失败分布式独占锁
	 */
	public static <T> T fastAttemptInExclusivePeriod(String key, long exclusivePeriodInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		if (client.getBucket(key, StringCodec.INSTANCE).setIfAbsent("1", Duration.of(exclusivePeriodInMs, ChronoUnit.MILLIS))) {
			return task.get();
		}
		return defaultVal == null ? null : defaultVal.get();
	}

	/**
	 * 拥有指定独占期的快速失败分布式独占锁
	 */
	public static <T> T fastAttemptInExclusivePeriod(String key, long exclusivePeriodInMs, Supplier<T> task) {
		return fastAttemptInExclusivePeriod(key, exclusivePeriodInMs, task, null);
	}

	/**
	 * 获取或创建布隆过滤器
	 * <p>
	 * 适用场景：
	 * - 缓存穿透防护
	 * - 海量数据去重
	 *
	 * @param filterName         过滤器名称
	 * @param expectedInsertions 预测插入数量 eg: 1000
	 * @param falseProbability   误判率 eg: 0.003
	 * @param <T>                元素类型
	 * @return 布隆过滤器实例
	 */
	public static <T> RBloomFilter<T> getBloomFilter(String filterName, long expectedInsertions, double falseProbability) {
		RBloomFilter<T> filter = client.getBloomFilter(filterName);
		filter.tryInit(expectedInsertions, falseProbability);
		return filter;
	}

	/**
	 * @param dataType 为 null 则表示所有数据类型
	 * @param maxCount -1 表示不限制
	 */
	public static Cursor<String> scanKeys(RedisOperations<String, ?> redisOps, @Nullable DataType dataType, String pattern, int maxCount) {
		final ScanOptions.ScanOptionsBuilder builder = ScanOptions.scanOptions().match(pattern);
		if (maxCount > 0) {
			builder.count(maxCount);
		}
		if (dataType != null) {
			builder.type(dataType);
		}
		ScanOptions scanOptions = builder.build();
		return scanKeys(redisOps, scanOptions);
	}

	/**
	 * @param dataType 为 null 则表示所有数据类型
	 * @param maxCount -1 表示不限制
	 */
	public static Cursor<String> scanKeys(@Nullable DataType dataType, String pattern, int maxCount) {
		return scanKeys(stringRedisTemplate, dataType, pattern, maxCount);
	}

	public static Cursor<String> scanKeys(RedisOperations<String, ?> redisOps, ScanOptions scanOptions) {
		RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisOps.getKeySerializer();
		return redisOps.executeWithStickyConnection(conn -> new ConvertingCursor<>(conn.scan(scanOptions), redisSerializer::deserialize));
	}

	public static Cursor<String> scanKeys(ScanOptions scanOptions) {
		return scanKeys(stringRedisTemplate, scanOptions);
	}

	/**
	 * @param cursor RedisCursor
	 */
	public static <T> List<T> scanToList(final Cursor<T> cursor) {
		final List<T> keys = new ArrayList<>();
		try (cursor) {
			while (cursor.hasNext()) {
				keys.add(cursor.next());
			}
		}
		return keys;
	}

	public static List<String> scanToKeys(ScanOptions scanOptions) {
		return scanToList(stringRedisTemplate.scan(scanOptions));
	}

	/**
	 * @param dataType 为 null 则表示所有数据类型
	 * @param maxCount -1 表示不限制
	 */
	public static List<String> scanToKeys(RedisTemplate<String, ?> redisTemplate, @Nullable DataType dataType, String pattern, int maxCount) {
		final Cursor<String> cursor = scanKeys(redisTemplate, dataType, pattern, maxCount);
		return scanToList(cursor);
	}

	/**
	 * @param dataType 为 null 则表示所有数据类型
	 * @param maxCount -1 表示不限制
	 */
	public static List<String> scanToKeys(@Nullable DataType dataType, String pattern, int maxCount) {
		return scanToKeys(stringRedisTemplate, dataType, pattern, maxCount);
	}

	/**
	 * 检测指定 Redis Key 是否存在过期时间
	 */
	public static boolean hasExpireTime(RedisTemplate<String, ?> redisTemplate, String redisKey) {
		// getExpire() 相当于 TTL 指令：如果指定的 key 不存在，返回 -2；如果指定的 key 不存在有效期（即永久不过期），则返回 -1
		final Long expireSecs = redisTemplate.getExpire(redisKey);
		return expireSecs != null && expireSecs > 0;
	}

	/**
	 * 如果指定的 Redis Key 存在且未设置过期时间，则进行初始化过期时间设置，否则什么都不做
	 */
	public static boolean tryInitExpire(RedisTemplate<String, ?> redisTemplate, String redisKey, final long timeout, final TimeUnit unit) {
		// getExpire() 相当于 TTL 指令：如果指定的 key 不存在，返回 -2；如果指定的 key 不存在有效期（即永久不过期），则返回 -1
		final Long expireSecs = redisTemplate.getExpire(redisKey);
		// 其实这里不可能为 null，只是为了以防 API 变动，导致向后兼容性问题
		if (expireSecs == null || expireSecs == -1L) {
			redisTemplate.expire(redisKey, timeout, unit);
			return true;
		}
		return false;
	}

	/**
	 * 如果指定的 Redis Key 存在且未设置过期时间，则进行初始化过期时间设置，否则什么都不做
	 */
	public static boolean tryInitExpire(RedisTemplate<String, ?> redisTemplate, String redisKey, Duration timeout) {
		return tryInitExpire(redisTemplate, redisKey, timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * 指示是否是 Redis 自增溢出异常
	 */
	public static boolean incrementOverflow(Throwable e) {
		if (e instanceof io.lettuce.core.RedisCommandExecutionException) {
			return "ERR increment or decrement would overflow".equals(e.getMessage());
		}
		return e.getCause() != null && incrementOverflow(e.getCause());
	}

	/**
	 * 同时获取多个 key 的数据
	 * 同时避免 IDEA NPE 提示
	 */
	@NonNull
	public static <T> List<T> multiGet(RedisTemplate<String, T> redisTemplate, Collection<String> keys) {
		//noinspection ConstantConditions
		return redisTemplate.opsForValue().multiGet(keys);
	}

	/**
	 * 同时获取多个 key 的数据
	 * 同时避免 IDEA NPE 提示
	 */
	@NonNull
	public static <T> List<T> multiGet(RedisTemplate<String, T> redisTemplate, String... keys) {
		return multiGet(redisTemplate, Arrays.asList(keys));
	}

	/**
	 * 基于指定前缀，对 ID对 创建唯一的字符串
	 */
	public static String uniquePairKey(@Nullable String prefix, long fromId, long toId) {
		if (prefix == null) {
			return pair(fromId, toId);
		}
		return fromId < toId ? prefix + fromId + "_" + toId : prefix + toId + "_" + fromId;
	}

	/**
	 * 对 ID对 创建唯一的字符串
	 */
	public static String pair(long fromId, long toId) {
		return fromId < toId ? fromId + "_" + toId : toId + "_" + fromId;
	}

	/**
	 * 当使用 StringRedisTemplate 读取使用 RedisTemplate 保存的字符串时，需要再次反序列化，才能拿到最终的字符串
	 */
	public static String deserializeCompat(String str) {
		if (str != null && str.startsWith("\"")) {
			return (String) JSON.parse(str, JSONReader.Feature.SupportAutoType);
		}
		return str;
	}

	/**
	 * 对 Redis ZSet score 返回值进行精度预处理
	 *
	 * @param scale 指定最多保留的小数位数
	 */
	@Nullable
	public static Double score(@Nullable Double val, int scale) {
		if (val != null) {
			if (val == val.longValue()) {
				return val;
			}
			BigDecimal d = new BigDecimal(val);
			if (d.scale() <= scale) {
				return val;
			}
			return d.setScale(scale, RoundingMode.HALF_UP).doubleValue();
		}
		return null;
	}

	/**
	 * 将 Redis ZSet score 返回值预处理为 高精确度 的值
	 *
	 * @param scale 指定最多保留的小数位数
	 */
	@Nullable
	public static BigDecimal scoreDecimal(@Nullable Double val, int scale) {
		if (val != null) {
			BigDecimal d = new BigDecimal(val);
			return d.scale() <= scale ? d : d.setScale(scale, RoundingMode.HALF_UP);
		}
		return null;
	}

	/**
	 * 对 Redis ZSet score 返回值进行精度预处理，如果为 null 则返回 0
	 *
	 * @param scale 指定最多保留的小数位数
	 */
	public static double scoreVal(@Nullable Double val, int scale) {
		return val != null ? score(val, scale) : 0D;
	}

	/**
	 * 取 Redis ZSet 指定成员的 score
	 */
	@Nullable
	public static Double score(String redisKey, String member) {
		return stringRedisTemplate.opsForZSet().score(redisKey, member);
	}

	/**
	 * 取 Redis ZSet 多个成员的 score
	 */
	@NonNull
	public static List<Double> score(String redisKey, String... members) {
		return stringRedisTemplate.opsForZSet().score(redisKey, (Object[]) members);
	}

	/**
	 * 取 Redis ZSet 指定成员的 score
	 * 如果成员不存在时，默认返回 0
	 *
	 * @param scale 指定最多保留的小数位数，超过将四舍五入
	 */
	public static double score(String redisKey, String member, int scale) {
		return scoreVal(score(redisKey, member), scale);
	}

	/**
	 * 取 Redis ZSet 指定成员的 score
	 * 如果成员不存在时，默认返回 0
	 */
	public static long scoreLong(String redisKey, String member) {
		Double score = score(redisKey, member);
		return scoreLong(score);
	}

	/**
	 * 将指定 Double 转为 long
	 *
	 * @return 如果为 null，则返回 0
	 */
	public static long scoreLong(@Nullable Double score) {
		return score == null ? 0L : score.longValue();
	}

	/**
	 * 取 Redis ZSet 指定成员的 score，并将存储的 long 转为对应小数位数的 BigDecimal
	 * 如果成员不存在时，默认返回 null
	 *
	 * @param unboxScale 指定拆箱的小数位数。如果为 2 则表示最后2位整数表示小数，即会将 10000 的 score 转为 100.00 并返回
	 */
	public static BigDecimal unboxScore(String redisKey, String member, int unboxScale, @Nullable BigDecimal defaultValue) {
		Double score = score(redisKey, member);
		if (score == null) {
			return defaultValue;
		}
		BigDecimal d = new BigDecimal(score);
		return d.movePointLeft(unboxScale);
	}

	/**
	 * 取 Redis ZSet 指定成员的 score，并将存储的 long 转为对应小数位数的 BigDecimal
	 * 如果成员不存在时，默认返回 0
	 *
	 * @param unboxScale 指定拆箱的小数位数。如果为 2 则表示最后2位整数表示小数，即会将 10000 的 score 转为 100.00 并返回
	 */
	@NonNull
	public static BigDecimal unboxScore(String redisKey, String member, int unboxScale) {
		return unboxScore(redisKey, member, unboxScale, BigDecimal.ZERO);
	}

	/**
	 * 在 Redis 事务中执行批处理操作（内部会自动开启、提交事务，抛异常时撤销事务）
	 */
	@Nullable
	public static <T> List<T> execInTransaction(final java.util.function.Consumer<RedisOperations<String, String>> redisOpsConsumer) {
		return stringRedisTemplate.execute(new SessionCallback<>() {
			@SuppressWarnings("unchecked")
			@Override
			public <K, V> List<T> execute(RedisOperations<K, V> operations) throws DataAccessException {
				RedisOperations<String, String> redisOps = (RedisOperations<String, String>) operations;
				redisOps.multi();
				redisOpsConsumer.accept(redisOps);
				return (List<T>) redisOps.exec();
			}
		});
	}

	/**
	 * 在 Redis 事务中执行批处理操作（内部会自动开启、提交事务，抛异常时撤销事务）
	 * 【注意】本方法【不会】处理也【不会】返回事务的执行结果
	 */
	public static void doInTransaction(final java.util.function.Consumer<RedisOperations<String, String>> redisOpsConsumer) {
		stringRedisTemplate.execute(new SessionCallback<>() {
			@Override
			public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
				RedisOperations<String, String> redisOps = (RedisOperations<String, String>) operations;
				redisOps.multi();
				redisOpsConsumer.accept(redisOps);
				// 不需要对返回数据作进一步转换处理
				return redisOps.execute(RedisTxCommands::exec);
			}
		});
	}

	/**
	 * 在 Redis 【管道】中执行批处理操作
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List<T> execInPipeline(final java.util.function.Consumer<RedisOperations<String, String>> redisOpsConsumer) {
		return (List<T>) stringRedisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public <K, V> List<Object> execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
				redisOpsConsumer.accept((RedisOperations) operations);
				return null;
			}
		});
	}

	/**
	 * 当 Redis key 存在时才设置指定值，并保持过期时间不变<p>
	 * 即执行：<code>SET key value XX KEEPTTL</code>
	 *
	 * @return 如果 key 存在则返回 true
	 */
	public static Boolean setIfPresentKeepTtl(final RedisOperations<String, ?> redisOps, String redisKey, String value) {
		// SET key value XX KEEPTTL
		byte[] keyBytes = redisKey.getBytes(StandardCharsets.UTF_8);
		byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
		byte[] xxBytes = "XX".getBytes(StandardCharsets.UTF_8);
		byte[] keepttlBytes = "KEEPTTL".getBytes(StandardCharsets.UTF_8);
		
		return redisOps.execute((RedisCallback<Boolean>) conn -> {
			Object result = conn.execute("SET", keyBytes, valueBytes, xxBytes, keepttlBytes);
			return result != null ? "OK".equals(new String((byte[]) result, StandardCharsets.UTF_8)) : false;
		});
	}

	/**
	 * 执行指定的函数调用<p>
	 * 执行命令：<code>FCALL functionName numkeys [key [key ...]] [arg [arg ...]]</code>
	 *
	 * @param mode 函数模式：READ=只读；WRITE=读写
	 * @param funcName 函数名称，例如："hello"
	 * @param keys Redis Key 集合
	 * @param args Redis 参数集合
	 * @since Redis 7.0.0
	 */
	public static Object fcall(final RedisOperations<String, ?> redisOps, FunctionMode mode, String funcName, List<String> keys, Object... args) {
		// FCALL functionName numkeys [key [key ...]] [arg [arg ...]]
		final int keySize = keys == null ? 0 : keys.size();
		final int argsSize = args == null ? 0 : args.length;
		final byte[][] byteArgs = new byte[2 + keySize + argsSize][];
		byteArgs[0] = funcName.getBytes(StandardCharsets.UTF_8); // 函数名称
		byteArgs[1] = Integer.toString(keySize).getBytes(StandardCharsets.UTF_8); // numkeys
		int pos = 2;
		for (int i = 0; i < keySize; i++) {
			String key = keys.get(i);
			if (key == null) {
				throw new NullPointerException("function keys[" + i + "] is null");
			}
			byteArgs[pos++] = key.getBytes(StandardCharsets.UTF_8);
		}
		for (int i = 0; i < argsSize; i++) {
			Object arg = args[i];
			if (arg == null) {
				throw new NullPointerException("function args[" + i + "] is null");
			}
			byteArgs[pos++] = arg.toString().getBytes(StandardCharsets.UTF_8);
		}
		return redisOps.execute((RedisCallback<Object>) conn -> conn.execute(mode == FunctionMode.READ ? "FCALL_RO" : "FCALL", byteArgs));
	}

	/**
	 * 将整数转为字符串
	 * 这里的目的主要是避免常用数字的 toString() 会 new 出新的字符串
	 */
	static String toString(final int val) {
		return switch (val) {
			case 0 -> "0";
			case 1 -> "1";
			case 2 -> "2";
			case 3 -> "3";
			case 4 -> "4";
			default -> Integer.toString(val);
		};
	}

	/**
	 * 执行指定的函数调用 <p>
	 * 执行命令：<code>FCALL function numkeys [key [key ...]] [arg [arg ...]]</code>
	 *
	 * @param funcName 函数名称，例如："hello"pi
	 * @param keys Redis Key 集合
	 * @param args Redis 参数集合
	 * @since Redis 7.0.0
	 */
	public static Object fcall(final RedisOperations<String, ?> redisOps, String funcName, List<String> keys, Object... args) {
		return fcall(redisOps, FunctionMode.WRITE, funcName, keys, args);
	}

	/**
	 * 只有在 RedisKey 没有设置过期时间时，才设置指定的过期时间
	 *
	 * @param timeoutInSeconds 过期时间，单位：秒
	 * @since Redis 7.0.0
	 */
	public static Boolean expireIfNoExpiry(final RedisOperations<String, ?> redisOps, String redisKey, int timeoutInSeconds) {
		// EXPIRE mykey 10 NX  https://redis.io/docs/latest/commands/expire/
		byte[] keyBytes = redisKey.getBytes(StandardCharsets.UTF_8);
		byte[] timeoutBytes = Integer.toString(timeoutInSeconds).getBytes(StandardCharsets.UTF_8);
		byte[] nxBytes = "NX".getBytes(StandardCharsets.UTF_8);
		
		return redisOps.execute((RedisCallback<Boolean>) conn -> {
			Object result = conn.execute("EXPIRE", keyBytes, timeoutBytes, nxBytes);
			return result != null && Long.valueOf(new String((byte[]) result, StandardCharsets.UTF_8)) == 1L;
		});
	}

	/**
	 * 从指定 Stream 中指定位置开始连续批量拉取消息
	 *
	 * @param offset 批量拉取的起始位置（请传入上一次拉取最后一项的 ID）
	 * @param readOptions 拉取消息的选项
	 * @return 拉取到的消息列表
	 */
	public static List<MapRecord<String, String, String>> pollMessages(StreamOffset<String> offset, StreamReadOptions readOptions) {
		//noinspection unchecked
		return stringRedisTemplate.<String, String>opsForStream().read(readOptions, offset);
	}

	/**
	 * 从指定 Stream 中指定位置开始连续批量拉取消息
	 *
	 * @param topic MQ 主题（即 Redis Stream Key）
	 * @param offset 开始位置（请传入上一次拉取最后一项的 ID ）
	 * @param readOptions 拉取消息的选项
	 * @return 拉取到的消息列表
	 */
	public static List<MapRecord<String, String, String>> pollMessages(String topic, ReadOffset offset, StreamReadOptions readOptions) {
		return pollMessages(StreamOffset.create(topic, offset), readOptions);
	}

	/**
	 * 从指定 Stream 中连续批量拉取消息
	 *
	 * @param topic MQ 主题（即 Redis Stream Key）
	 * @param offset 批量拉取的起始位置（请传入上一次拉取最后一项的 ID，如果是首次拉取，请传入 "0-0" ）
	 * @param readOptions 拉取消息的选项
	 * @return 拉取到的消息列表
	 */
	public static List<MapRecord<String, String, String>> pollMessages(String topic, String offset, StreamReadOptions readOptions) {
		return pollMessages(topic, ReadOffset.from(offset), readOptions);
	}

	/**
	 * 从指定 Stream 中连续批量拉取消息
	 *
	 * @param offset 批量拉取的起始位置（请传入上一次拉取最后一项的 ID ）
	 * @param readOptions 拉取消息的选项
	 * @return 拉取到的消息列表
	 */
	public static List<MapRecord<String, String, String>> pollMessages(org.springframework.data.redis.connection.stream.Record<String, ?> offset, StreamReadOptions readOptions) {
		return pollMessages(StreamOffset.from(offset), readOptions);
	}

	/**
	 * 从指定 Stream 中连续批量拉取消息
	 *
	 * @param topic MQ 主题（即 Redis Stream Key）
	 * @param offset 批量拉取的起始位置（请传入上一次拉取最后一项的 ID，如果是首次拉取，请传入 "0-0" ）
	 * @param fetchSize 批量拉取的消息数量
	 * @return 拉取到的消息列表
	 */
	public static List<MapRecord<String, String, String>> pollMessages(String topic, String offset, int fetchSize) {
		return pollMessages(topic, ReadOffset.from(offset), StreamReadOptions.empty().count(fetchSize));
	}

	// 只有 Redis 8.2+ 才支持 "ACKED" 选项，才需要单独自定义
	public static Long xTrimAcked(final RedisOperations<String, ?> redisOps, String topic, int maxLength) {
		// XTRIM $key MAXLEN ~ 10000 ACKED  https://redis.io/docs/latest/commands/xtrim/
		byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);
		byte[] maxlenBytes = "MAXLEN".getBytes(StandardCharsets.UTF_8);
		byte[] approxBytes = "~".getBytes(StandardCharsets.UTF_8);
		byte[] lengthBytes = Integer.toString(maxLength).getBytes(StandardCharsets.UTF_8);
		byte[] ackedBytes = "ACKED".getBytes(StandardCharsets.UTF_8);
		
		return redisOps.execute((RedisCallback<Long>) conn -> 
				(Long) conn.execute("XTRIM", topicBytes, maxlenBytes, approxBytes, lengthBytes, ackedBytes));
	}

	public static Long xTrimMinId(final RedisOperations<String, ?> redisOps, String topic, String minId) {
		// XTRIM $key MINID $mindId  https://redis.io/docs/latest/commands/xtrim/
		byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);
		byte[] minidBytes = "MINID".getBytes(StandardCharsets.UTF_8);
		byte[] minIdBytes = minId.getBytes(StandardCharsets.UTF_8);
		
		return redisOps.execute((RedisCallback<Long>) conn -> 
				(Long) conn.execute("XTRIM", topicBytes, minidBytes, minIdBytes));
	}

	public static <E> E executeRawCommand(final RedisOperations<String, ?> redisOps, Function<Object, E> converter, String command, Object... args) {
		return redisOps.execute((RedisCallback<E>) conn -> {
			// 将命令和参数转为 byte[]
			final byte[][] byteArgs = new byte[args.length][];
			for (int i = 0; i < args.length; i++) {
				byteArgs[i] = args[i] instanceof byte[] bytes ? bytes : args[i].toString().getBytes(StandardCharsets.UTF_8);
			}
			Object result = conn.execute(command, byteArgs);
			return converter.apply(result);
		});
	}

	public static String executeRawCommand(final RedisOperations<String, ?> redisOps, String command, Object... args) {
		return executeRawCommand(redisOps, RedisUtil::convertToString, command, args);
	}

	private static String convertToString(Object result) {
		if (result == null) {
			return null;
		}
		// 1. byte[] → string (most common: INFO, GET, PING, CONFIG GET, etc.)
		if (result instanceof byte[] bytes) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		// 2. Collection<byte[]> → multi-line string
		if (result instanceof Collection<?> c) {
			if (c.isEmpty()) {
				return "[]";
			}
			return CollectionUtil.toList(c, RedisUtil::convertToString).toString();
		}
		// 3. Map<byte[], byte[]> → multi-line string
		if (result instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				return "{}";
			}
			Map<String, String> stringMap = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				stringMap.put(convertToString(entry.getKey()), convertToString(entry.getValue()));
			}
			return stringMap.toString();
		}
		// 4. 其他情况（如 Boolean）→ toString()
		return result.toString();
	}

	@Nullable
	public static Long byte2Long(@Nullable Object bytes) {
		return bytes == null ? null : Long.valueOf(new String((byte[]) bytes, StandardCharsets.ISO_8859_1));
	}

	/**
	 * 执行 Lua 脚本（原子操作）
	 * <p>
	 * 适用场景：
	 * - 需要保证多个 Redis 操作的原子性
	 * - 防超卖、限流等并发控制场景
	 * <p>
	 * 注意：此方法只提供技术能力，具体的业务 Lua 脚本应该在领域服务中定义
	 *
	 * @param script     Lua 脚本内容（由调用方定义业务逻辑）
	 * @param keys       Redis Key 列表
	 * @param args       参数列表
	 * @param resultType 返回值类型
	 * @param <T>        返回值泛型
	 * @return 执行结果
	 */
	public static <T> T executeLuaScript(String script, List<String> keys, List<String> args, Class<T> resultType) {
		DefaultRedisScript<T> redisScript =
				new DefaultRedisScript<>(script, resultType);
		return stringRedisTemplate.execute(redisScript, keys, (Object) args.toArray(new String[0]));
	}

}