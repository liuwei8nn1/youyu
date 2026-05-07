package com.youyu.framework.cache.lock;

import java.util.function.Supplier;

/**
 * 分布式锁接口（基础模块 - 通用基础设施）
 * <p>
 * 职责：
 * 1. 提供统一的分布式锁抽象
 * 2. 屏蔽底层实现细节（Redisson/Redis/Zookeeper 等）
 * 3. 提供便捷的锁执行模板方法
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Autowired
 * private DistributedLock distributedLock;
 * 
 * // 方式1：手动加锁/解锁
 * RLock lock = distributedLock.getLock("myLockKey");
 * try {
 *     if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
 *         // 执行业务逻辑
 *     }
 * } finally {
 *     lock.unlock();
 * }
 * 
 * // 方式2：使用模板方法（推荐）
 * distributedLock.executeWithLock("myLockKey", () -> {
 *     // 执行业务逻辑
 *     return result;
 * });
 * }</pre>
 *
 * @since 2026/4/13
 */
public interface DistributedLock {

    /**
     * 获取锁对象
     *
     * @param lockKey 锁的 Key
     * @return 锁对象
     */
    Object getLock(String lockKey);

    /**
     * 尝试获取锁
     *
     * @param lockKey  锁的 Key
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁自动释放时间（秒）
     * @return 是否获取成功
     * @throws InterruptedException 中断异常
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime) throws InterruptedException;

    /**
     * 释放锁
     *
     * @param lockKey 锁的 Key
     */
    void unlock(String lockKey);

    /**
     * 带锁执行业务逻辑（模板方法）
     * <p>
     * 自动处理锁的获取、释放，以及异常情况
     *
     * @param lockKey   锁的 Key
     * @param waitTime  等待时间（秒）
     * @param leaseTime 锁自动释放时间（秒）
     * @param action    要执行的业务逻辑
     * @param <T>       返回值类型
     * @return 业务逻辑的返回值
     * @throws RuntimeException 获取锁失败或业务逻辑异常
     */
    default <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> action) {
        try {
            if (tryLock(lockKey, waitTime, leaseTime)) {
                try {
                    return action.get();
                } finally {
                    unlock(lockKey);
                }
            } else {
                throw new RuntimeException("获取分布式锁失败: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁被中断: " + lockKey, e);
        }
    }

    /**
     * 带锁执行业务逻辑（无返回值）
     *
     * @param lockKey   锁的 Key
     * @param waitTime  等待时间（秒）
     * @param leaseTime 锁自动释放时间（秒）
     * @param action    要执行的业务逻辑
     */
    default void executeWithLock(String lockKey, long waitTime, long leaseTime, Runnable action) {
        executeWithLock(lockKey, waitTime, leaseTime, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 带锁执行业务逻辑（使用默认超时时间）
     * <p>
     * 默认等待时间：5秒<br>
     * 默认锁释放时间：10秒
     *
     * @param lockKey 锁的 Key
     * @param action  要执行的业务逻辑
     * @param <T>     返回值类型
     * @return 业务逻辑的返回值
     */
    default <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(lockKey, 5, 10, action);
    }

    /**
     * 带锁执行业务逻辑（无返回值，使用默认超时时间）
     *
     * @param lockKey 锁的 Key
     * @param action  要执行的业务逻辑
     */
    default void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, 5, 10, action);
    }
}
