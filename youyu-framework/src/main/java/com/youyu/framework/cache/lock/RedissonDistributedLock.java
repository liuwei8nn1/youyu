package com.youyu.framework.cache.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的分布式锁实现（基础模块 - 通用基础设施）
 * <p>
 * 职责：
 * 1. 封装 Redisson 的技术细节
 * 2. 提供统一的分布式锁接口实现
 * 3. 支持灵活的锁超时配置
 *
 * @since 2026/4/13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLock implements DistributedLock {

    private final RedissonClient redissonClient;

    @Override
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) throws InterruptedException {
        RLock lock = getLock(lockKey);
        boolean locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        
        if (locked) {
            log.debug("获取分布式锁成功: {}", lockKey);
        } else {
            log.warn("获取分布式锁失败: {}", lockKey);
        }
        
        return locked;
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        
        // 只有当前线程持有锁时才释放
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("释放分布式锁: {}", lockKey);
        } else {
            log.warn("尝试释放未持有的锁: {}", lockKey);
        }
    }
}
