package com.youyu.user.sdk;

import com.youyu.common.model.Result;
import com.youyu.user.api.client.UserFeignClient;
import com.youyu.user.api.dto.UserLoginInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务 SDK 客户端
 * 提供便捷的缓存 + 远程调用封装
 * 
 * <p>使用示例:</p>
 * <pre>{@code
 * @Autowired
 * private UserServiceClient userServiceClient;
 * 
 * public void doSomething(Long userId) {
 *     // 自动带缓存,无需关心 Feign 细节
 *     UserLoginInfo user = userServiceClient.getUserWithCache(userId);
 * }
 * }</pre>
 */
@Slf4j
@Component
public class UserServiceClient {

    @Autowired(required = false)
    private UserFeignClient userFeignClient;

    @Autowired(required = false)
    private RedisTemplate<String, UserLoginInfo> redisTemplate;

    /**
     * 根据用户名查询用户(带缓存)
     * 缓存策略: Redis 5分钟TTL
     * 
     * @param username 用户名
     * @return 用户登录信息,不存在返回null
     */
    public UserLoginInfo getByUsernameWithCache(String username,  Integer userType) {
        if (userFeignClient == null) {
            log.warn("UserFeignClient not available");
            return null;
        }

        String cacheKey = "user:username:" + username;

        try {
            // 1. 查缓存
            if (redisTemplate != null) {
                UserLoginInfo cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for username: {}", username);
                    return cached;
                }
            }

            // 2. 远程调用(通过Feign)
            Result<UserLoginInfo> result = userFeignClient.getByUsername(username, userType);
            if (result != null && result.isSuccess() && result.getData() != null) {
                UserLoginInfo userInfo = result.getData();

                // 3. 写入缓存(5分钟过期)
                if (redisTemplate != null) {
                    redisTemplate.opsForValue().set(cacheKey, userInfo, 5, TimeUnit.MINUTES);
                    log.debug("Cached user info for username: {}", username);
                }

                return userInfo;
            }
        } catch (Exception e) {
            log.error("Failed to get user by username: {}", username, e);
        }

        return null;
    }

    /**
     * 根据手机号查询用户(带缓存)
     */
    public UserLoginInfo getByPhoneWithCache(String phone, Integer userType) {
        if (userFeignClient == null) {
            return null;
        }

        String cacheKey = "user:phone:" + phone;

        try {
            // 查缓存
            if (redisTemplate != null) {
                UserLoginInfo cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return cached;
                }
            }

            // 远程调用
            Result<UserLoginInfo> result = userFeignClient.getByPhone(phone, userType);
            if (result != null && result.isSuccess() && result.getData() != null) {
                UserLoginInfo userInfo = result.getData();

                // 写入缓存
                if (redisTemplate != null) {
                    redisTemplate.opsForValue().set(cacheKey, userInfo, 5, TimeUnit.MINUTES);
                }

                return userInfo;
            }
        } catch (Exception e) {
            log.error("Failed to get user by phone: {}", phone, e);
        }

        return null;
    }

    /**
     * 清除用户缓存
     * 当用户信息更新时调用
     */
    public void evictUserCache(String username) {
        if (redisTemplate != null) {
            String cacheKey = "user:username:" + username;
            redisTemplate.delete(cacheKey);
            log.debug("Evicted cache for username: {}", username);
        }
    }
}
