package com.youyu.framework.antishake.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复点击注解（防抖）
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 方式1：使用 SpEL 表达式（推荐）
 * @AntiShake(
 *     intervalMs = 1000,
 *     keyPrefix = "order:create",
 *     keyExpression = "#request.userId + ':' + #request.productId"
 * )
 * 
 * // 方式2：自动生成 key（基于方法签名 + 参数 hash）
 * @AntiShake(
 *     intervalMs = 1000,
 *     keyPrefix = "order:create",
 *     useAutoKey = true
 * )
 * 
 * // 方式3：指定缓存类型
 * @AntiShake(
 *     intervalMs = 1000,
 *     keyPrefix = "order:create",
 *     keyExpression = "#request.userId",
 *     cacheType = CacheType.LOCAL  // 强制使用本地缓存
 * )
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AntiShake {
    
    /**
     * 防抖时间间隔（毫秒）
     * 默认 1000ms，表示 1 秒内不允许重复调用
     */
    long intervalMs() default 1000;
    
    /**
     * Key 的前缀
     * 例如："order:create"、"user:login"
     */
    String keyPrefix();
    
    /**
     * SpEL 表达式，用于从方法参数中提取 key
     * <p>
     * 示例：
     * - "#request.userId" ：提取 request 对象的 userId 字段
     * - "#userId + ':' + #productId" ：组合多个参数
     * - "#request.userId + ':' + #request.productId + ':' + #request.quantity"
     * <p>
     * 如果为空且 useAutoKey=false，则会抛出异常
     */
    String keyExpression() default "";
    
    /**
     * 是否使用方法签名 + 参数 hash 自动生成 key
     * <p>
     * 当 keyExpression 为空时，如果此值为 true，则自动基于方法全限定名和参数生成 key
     * 优点：无需手动写表达式
     * 缺点：key 不可读，调试困难
     */
    boolean useAutoKey() default false;
    
    /**
     * 缓存类型
     * <ul>
     *   <li>DEFAULT：使用全局配置的缓存类型（默认）</li>
     *   <li>LOCAL：仅使用本地缓存（Caffeine），适合单机场景</li>
     *   <li>REDIS：仅使用分布式缓存（Redis），适合集群场景</li>
     * </ul>
     */
    CacheType cacheType() default CacheType.DEFAULT;
}
