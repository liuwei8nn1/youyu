package com.youyu.framework.antishake.aspect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.youyu.common.constant.BaseI18nKey;
import com.youyu.common.util.JsonUtil;
import com.youyu.framework.antishake.cache.AntiShakeCache;
import com.youyu.framework.antishake.cache.impl.CaffeineAntiShakeCache;
import com.youyu.framework.antishake.cache.impl.RedisAntiShakeCache;
import com.youyu.framework.context.I18N;
import com.youyu.framework.antishake.annotation.AntiShake;
import com.youyu.framework.antishake.annotation.CacheType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 防重复点击 AOP 切面
 * <p>
 * 功能：
 * 1. 解析 @AntiShake 注解
 * 2. 生成防抖 key（支持 SpEL 表达式和自动生成）
 * 3. 根据配置的缓存类型执行防抖检查
 * 4. 被限流时抛出国际化异常
 */
@Slf4j
@Aspect
public class AntiShakeAspect {

    private final CaffeineAntiShakeCache caffeineAntiShakeCache;
    private final RedisAntiShakeCache redisAntiShakeCache;
    private final AntiShakeCache defaultAntiShakeCache;
    
    public AntiShakeAspect(
            @Qualifier("caffeineAntiShakeCache") CaffeineAntiShakeCache caffeineAntiShakeCache,
            @Qualifier("redisAntiShakeCache") RedisAntiShakeCache redisAntiShakeCache,
            @Qualifier("defaultAntiShakeCache") AntiShakeCache defaultAntiShakeCache) {
        this.caffeineAntiShakeCache = caffeineAntiShakeCache;
        this.redisAntiShakeCache = redisAntiShakeCache;
        this.defaultAntiShakeCache = defaultAntiShakeCache;
    }
    
    /**
     * SpEL 表达式解析器缓存（避免重复创建）
     */
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>();
    private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
    
    @Around("@annotation(antiShake)")
    public Object around(ProceedingJoinPoint joinPoint, AntiShake antiShake) throws Throwable {
        // 1. 生成防抖 key
        String key = generateKey(joinPoint, antiShake);
        
        // 2. 获取缓存实现
        AntiShakeCache cache = getAntiShakeCache(antiShake.cacheType());
        
        // 3. 执行防抖检查
        boolean acquired = cache.tryAcquire(key, antiShake.intervalMs());
        
        if (!acquired) {
            // 被限流，抛出国际化异常
            log.warn("防抖限制：key={}, 方法={}", key, joinPoint.getSignature());
            throw new RuntimeException(I18N.msg(BaseI18nKey.ANTI_SHAKE_LIMIT));
        }
        
        // 4. 执行目标方法
        try {
            return joinPoint.proceed();
        } catch (RuntimeException e) {
            // 业务异常不捕获，直接抛出
            throw e;
        } catch (Throwable e) {
            // 其他异常包装为 RuntimeException
            log.error("防抖方法执行异常：key={}", key, e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 生成防抖 key
     */
    private String generateKey(ProceedingJoinPoint joinPoint, AntiShake antiShake) {
        String keySuffix;
        
        // 优先使用 SpEL 表达式
        if (antiShake.keyExpression() != null && !antiShake.keyExpression().isEmpty()) {
            keySuffix = parseSpELExpression(joinPoint, antiShake.keyExpression());
        } 
        // 其次使用自动生成
        else if (antiShake.useAutoKey()) {
            keySuffix = generateAutoKey(joinPoint);
        } 
        // 否则抛出异常
        else {
            throw new IllegalArgumentException(
                "@AntiShake 必须指定 keyExpression 或设置 useAutoKey=true"
            );
        }
        
        return antiShake.keyPrefix() + ":" + keySuffix;
    }
    
    /**
     * 解析 SpEL 表达式
     */
    private String parseSpELExpression(ProceedingJoinPoint joinPoint, String expression) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取参数名和值
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            // 构建 SpEL 上下文
            StandardEvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            // 解析表达式（使用缓存）
            Expression spELExpression = EXPRESSION_CACHE.computeIfAbsent(
                expression, 
                SPEL_PARSER::parseExpression
            );
            
            String result = spELExpression.getValue(context, String.class);
            if (result == null || result.isEmpty()) {
                throw new IllegalArgumentException("SpEL 表达式结果为空：" + expression);
            }
            
            return result;
        } catch (Exception e) {
            log.error("SpEL 表达式解析失败：expression={}", expression, e);
            throw new IllegalArgumentException("SpEL 表达式解析失败：" + expression, e);
        }
    }
    
    /**
     * 自动生成 key（基于方法签名 + 参数 hash）
     */
    private String generateAutoKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        
        // 参数序列化后计算 MD5
        String paramsJson = JsonUtil.toJson(joinPoint.getArgs());
        String paramsHash = DigestUtils.md5Hex(paramsJson);
        
        return methodName + ":" + paramsHash;
    }
    
    /**
     * 获取防抖缓存实现
     */
    private AntiShakeCache getAntiShakeCache(CacheType cacheType) {
        return switch (cacheType) {
            case LOCAL -> caffeineAntiShakeCache;
            case REDIS -> redisAntiShakeCache;
            case DEFAULT -> defaultAntiShakeCache;
        };
    }
}
