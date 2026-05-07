package com.youyu.framework.id;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.youyu.framework.cache.redis.RedisKeyBuilder;

/**
 * Snowflake WorkerId 分配器
 * <p>
 * 基于 Redis 自动分配 WorkerId，确保集群中每个实例的 WorkerId 唯一。
 * <p>
 * 分配策略：
 * 1. 使用应用名 + IP + PID 作为唯一标识
 * 2. 从 Redis 中获取或分配 WorkerId（0-31）和 DataCenterId（0-31）
 * 3. 分配的 ID 有过期时间，防止实例重启后冲突
 *
 * @since 2026-01-01
 */
@Slf4j
public class SnowflakeWorkerIdAllocator {

    private static final String WORKER_ID_KEY = "snowflake:worker_ids";
    private static final long RENEWAL_INTERVAL = 60 * 60;
    private static final long EXPIRE_SECONDS = RENEWAL_INTERVAL * 2;

    private final StringRedisTemplate redisTemplate;
    private final String redisPrefix;
    private final String instanceId;

    private final List<String> allocatorIdList = new ArrayList<>();
    private final ScheduledExecutorService renewalScheduler;

    public SnowflakeWorkerIdAllocator(String redisPrefix, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisPrefix = redisPrefix;
        this.instanceId = generateInstanceId();
        log.info("===========>>>>>>> SnowflakeWorkerIdAllocator 初始化完成 - InstanceId: {}, redisPrefix: {}",
                instanceId, redisPrefix);
        
        // 创建基于虚拟线程的定时任务调度器
        this.renewalScheduler = Executors.newSingleThreadScheduledExecutor(
            Thread.ofVirtual().name("snowflake-renewal-", 0).factory()
        );
        
        // 启动定时续期任务，每30分钟执行一次
        this.renewalScheduler.scheduleAtFixedRate(
            this::renew,
            RENEWAL_INTERVAL,
            RENEWAL_INTERVAL,
            TimeUnit.SECONDS
        );
        log.info("===========>>>>>>> 已经启动 WorkerId 定时续期任务，间隔: {} 秒", RENEWAL_INTERVAL);
    }

    /**
     * 批量续期所有分配的 ID
     */
    private void renew() {
        if (allocatorIdList.isEmpty()) {
            return;
        }
        
        try {
            List<String> keysToRenew;
            synchronized (allocatorIdList) {
                keysToRenew = new ArrayList<>(allocatorIdList);
            }
            
            int successCount = 0;
            int failCount = 0;
            
            // 批量续期，减少 Redis 交互次数
            for (String key : keysToRenew) {
                try {
                    // 检查是否仍由当前实例持有
                    String owner = redisTemplate.opsForValue().get(key);
                    if (instanceId.equals(owner)) {
                        // 续期
                        Boolean result = redisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);
                        if (Boolean.TRUE.equals(result)) {
                            successCount++;
                        } else {
                            failCount++;
                            log.warn("续期失败（可能已过期）: {}", key);
                        }
                    } else {
                        // ID 已被其他实例占用，从列表中移除
                        synchronized (allocatorIdList) {
                            allocatorIdList.remove(key);
                        }
                        log.warn("ID 已被其他实例占用，移除: {}, 新持有者: {}", key, owner);
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("续期单个 ID 失败: {}", key, e);
                }
            }
            
            if (successCount > 0 || failCount > 0) {
                log.debug("WorkerId 续期完成 - 成功: {}, 失败: {}, 总数: {}", 
                    successCount, failCount, keysToRenew.size());
            }
        } catch (Exception e) {
            log.error("WorkerId 批量续期异常", e);
        }
    }
    /**
     * 生成实例唯一标识
     */
    private String generateInstanceId() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            return ip + ":" + pid;
        } catch (Exception e) {
            log.warn("===========>>>>>>> 获取实例ID失败，使用随机ID", e);
            return "unknown-" + System.currentTimeMillis();
        }
    }

    /**
     * 分配 ID（WorkerId 或 DataCenterId）
     *
     * @param key Redis键
     * @param maxId 最大ID值（包含）
     * @return 分配的ID
     */
    private int allocateId(String key, int maxId) {
        // 尝试获取已分配的ID
        for (int i = 0; i <= maxId; i++) {
            String fieldKey = key + RedisKeyBuilder.SEPARATOR + i;
            Boolean exists = redisTemplate.hasKey(fieldKey);
            if (Boolean.FALSE.equals(exists)) {
                // 该ID未被占用，尝试分配
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(fieldKey, instanceId, EXPIRE_SECONDS, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(success)) {
                    log.info("===========>>>>>>> 成功分配ID: {} = {}", fieldKey, i);
                    return i;
                }
            } else {
                // 检查是否是自己之前分配的
                String owner = redisTemplate.opsForValue().get(fieldKey);
                if (instanceId.equals(owner)) {
                    // 续期
                    redisTemplate.expire(fieldKey, EXPIRE_SECONDS, TimeUnit.SECONDS);
                    log.info("===========>>>>>>> 续期已有ID: {} = {}", fieldKey, i);
                    return i;
                }
            }
        }
        
        // 所有ID都被占用，抛出异常
        throw new IllegalStateException("无法分配ID，所有ID（0-" + (maxId - 1) + "）都已被占用");
    }

    private String builderKey(@Nullable String businessKey, int dataCenterId){
        if(businessKey == null){
            return redisPrefix + RedisKeyBuilder.SEPARATOR + WORKER_ID_KEY + RedisKeyBuilder.SEPARATOR + dataCenterId;
        }
        return redisPrefix + RedisKeyBuilder.SEPARATOR + WORKER_ID_KEY + RedisKeyBuilder.SEPARATOR + businessKey +  RedisKeyBuilder.SEPARATOR + dataCenterId;
    }

    public int allocateWorkerId(String businessKey, int dataCenterId, int maxId) {
        String key = builderKey(businessKey, dataCenterId);
        int i = allocateId( key, maxId);
        key = key + RedisKeyBuilder.SEPARATOR + i;
        synchronized (allocatorIdList) {
            allocatorIdList.add(key);
        }
        return i;
    }

    public int allocateWorkerId(int dataCenterId, int maxId) {
        String key = builderKey(null, dataCenterId);
        int i = allocateId(key, maxId);
        key = key + RedisKeyBuilder.SEPARATOR + i;
        synchronized (allocatorIdList) {
            allocatorIdList.add(key);
        }
        return i;
    }

    /**
     * 释放分配的 ID（在应用关闭时调用）
     */
    public void release() {
        // 停止定时续期任务
        if (renewalScheduler != null && !renewalScheduler.isShutdown()) {
            renewalScheduler.shutdownNow();
            log.info("===========>>>>>>> WorkerId 定时续期任务已停止");
        }

        // 删除 Redis 中的 ID 记录
        if (!allocatorIdList.isEmpty()) {
            redisTemplate.delete(allocatorIdList);
            log.info("===========>>>>>>> 已经释放 {} 个 WorkerId", allocatorIdList.size());
        }
    }

}
