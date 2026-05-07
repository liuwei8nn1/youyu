package com.youyu.auth.infrastructure.persistence.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.youyu.auth.api.JwtTokenProvider;
import com.youyu.auth.api.model.RedisKey;
import com.youyu.framework.cache.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 认证相关的 Redis 操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRedisService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 禁用用户
     *
     * @param identityId 用户身份ID
     * @param reason 禁用原因
     */
    public void disableUser(Long identityId, Integer userType, String reason) {
        String redisKey = RedisKey.buildUserDisableKey(identityId, userType);
        redisTemplate.opsForValue().set(redisKey, reason != null ? reason : "disabled");
    }

    /**
     * 解除用户禁用
     *
     * @param identityId 用户身份ID
     */
    public void enableUser(Long identityId, Integer userType) {
        String redisKey = RedisKey.buildUserDisableKey(identityId, userType);
        redisTemplate.delete(redisKey);
    }

    /**
     * 检查用户是否被禁用
     *
     * @param identityId 用户身份ID
     * @return true-已禁用，false-未禁用
     */
    public boolean isUserDisabled(Long identityId, Integer userType) {
        String redisKey = RedisKey.buildUserDisableKey(identityId, userType);
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    /**
     * 获取禁用原因
     *
     * @param identityId 用户身份ID
     * @return 禁用原因
     */
    public String getDisableReason(Long identityId,Integer userType) {
        String redisKey = RedisKey.buildUserDisableKey(identityId, userType);
        return redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 标记设备在线
     * <p>
     * 设计思路（节省 Redis 空间）：
     * - 正常在线：不在 Redis 中存储任何内容（零空间占用）
     * - 离线/退出：存储 key="exit"，TTL = refreshTokenTtl
     * - 判断在线：key 不存在或已过期 = 在线；key 存在且值为 "exit" = 已退出
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @param deviceId 设备ID（UserDevice表的id，Long类型）
     * @param ttl      过期时间（秒）
     */
    public void markDeviceOnline(Long identityId, Integer userType, Long deviceId, long ttl) {
        // 正常在线不需要在 Redis 中存储，直接删除可能存在的"exit"标记
        String key = RedisKey.buildDeviceKey(identityId, userType, String.valueOf(deviceId));
        redisTemplate.delete(key);
        log.debug("标记设备在线: identityId={}, userType={}, deviceId={}", identityId, userType, deviceId);
    }

    /**
     * 标记设备离线/退出
     * <p>
     * 设计思路（节省 Redis 空间）：
     * - 正常在线：不在 Redis 中存储任何内容（零空间占用）
     * - 离线/退出：存储 key="exit"，TTL = refreshTokenTtl
     * - 判断在线：key 不存在或已过期 = 在线；key 存在且值为 "exit" = 已退出
     * <p>
     * 为什么 TTL = refreshTokenTtl？
     * - refreshToken 过期后，用户必须重新登录，旧的"exit"标记不再需要
     * - 在 refreshToken 有效期内，能确保检测到"已退出"状态
     * - 无需 *2，因为 JWT 过期是硬性的，不存在宽限期
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @param deviceId 设备ID（UserDevice表的id，Long类型）
     */
    public void markDeviceOffline(Long identityId, Integer userType, Long deviceId) {
        String key = RedisKey.buildDeviceKey(identityId, userType, String.valueOf(deviceId));
        long ttl = jwtTokenProvider.getRefreshTokenTtl();
        redisTemplate.opsForValue().set(key, "exit", ttl, TimeUnit.SECONDS);
        log.debug("标记设备离线: identityId={}, userType={}, deviceId={}, ttl={}s", identityId, userType, deviceId, ttl);
    }

    /**
     * 检查设备是否在线
     * <p>
     * 判断逻辑：
     * - key 不存在或已过期 → 在线（正常状态，不占 Redis 空间）
     * - key 存在且值为 "exit" → 已退出（被踢出或主动登出）
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @param deviceId 设备ID（UserDevice表的id，Long类型）
     * @return true=在线，false=已退出
     */
    public boolean isDeviceOnline(Long identityId, Integer userType, Long deviceId) {
        String key = RedisKey.buildDeviceKey(identityId, userType, String.valueOf(deviceId));
        String value = redisTemplate.opsForValue().get(key);
        
        // key 不存在或已过期 = 在线
        // key 存在且值为 "exit" = 已退出
        boolean online = (value == null);
        log.trace("检查设备在线状态: identityId={}, deviceId={}, online={}", identityId, deviceId, online);
        return online;
    }

    /**
     * 批量标记设备离线
     * <p>
     * 使用 Pipeline 批量操作，提升性能
     *
     * @param identityId 用户身份ID
     * @param userType  用户类型
     * @param deviceIds 设备ID列表（UserDevice表的id，Long类型）
     */
    public void batchMarkDeviceOffline(Long identityId, Integer userType, List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        
        long ttl = jwtTokenProvider.getRefreshTokenTtl();
        log.info("批量标记设备离线: identityId={}, count={}, ttl={}s", identityId, deviceIds.size(), ttl);
        
        RedisUtil.execInPipeline((opt) -> {
            deviceIds.forEach(deviceId -> {
                String key = RedisKey.buildDeviceKey(identityId, userType, String.valueOf(deviceId));
                redisTemplate.opsForValue().set(key, "exit", ttl, TimeUnit.SECONDS);
            });
        });
    }

    // ==================== 在线状态管理（Presence） ====================

    /**
     * 更新用户在线状态
     * <p>
     * 设计思路：
     * - 按用户维度存储，不区分设备（业务只关心用户是否在线）
     * - Value 格式：deviceId:timestamp（记录最后活跃设备和时间）
     * - 在刷新 Token 时调用，而非每次请求都更新
     * - 利用现有的 Token 刷新机制（每15分钟），避免高频写入
     * - TTL = AccessToken TTL + 缓冲时间 = 15分钟 + 1分钟 = 16分钟
     * <p>
     * 优势：
     * - 写入压力极低：千万级 DAU 也只需 ~1000次/分钟
     * - 内存占用小：100万在线用户 ≈ 100MB
     * - 准确性可接受：电商系统不需要秒级精度
     * - 实现简单：无需前端改造，无需心跳
     * - 信息丰富：知道哪个设备最后活跃
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @param deviceId 设备ID（UserDevice表的id，Long类型）
     */
    public void updatePresence(Long identityId, Integer userType, Long deviceId) {
        String key = RedisKey.buildPresenceKey(identityId, userType);
        long now = System.currentTimeMillis();
        // Value 格式：deviceId:timestamp
        String value = deviceId + ":" + now;
        // TTL = AccessToken TTL (900s) + 缓冲时间 (60s) = 960s = 16分钟
        long ttl = jwtTokenProvider.getAccessTokenTtl() + 60;
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        log.debug("更新在线状态: identityId={}, deviceId={}, timestamp={}, ttl={}s", identityId, deviceId, now, ttl);
    }

    /**
     * 检查用户是否在线
     * <p>
     * 判断逻辑：Redis Key 存在且未过期 = 在线
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @return true=在线，false=离线
     */
    public boolean isUserOnline(Long identityId, Integer userType) {
        String key = RedisKey.buildPresenceKey(identityId, userType);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取用户最后活跃信息
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @return 最后活跃信息，null表示离线
     */
    public PresenceInfo getLastActiveInfo(Long identityId, Integer userType) {
        String key = RedisKey.buildPresenceKey(identityId, userType);
        String value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        try {
            // 解析 value: deviceId:timestamp
            int lastColonIndex = value.lastIndexOf(':');
            if (lastColonIndex == -1) {
                log.warn("在线状态数据格式错误: identityId={}, value={}", identityId, value);
                return null;
            }
            
            String deviceId = value.substring(0, lastColonIndex);
            Long timestamp = Long.parseLong(value.substring(lastColonIndex + 1));
            
            return new PresenceInfo(deviceId, timestamp);
        } catch (Exception e) {
            log.error("解析在线状态数据失败: identityId={}, value={}", identityId, value, e);
            return null;
        }
    }

    /**
     * 获取用户最后活跃时间戳
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @return 最后活跃时间戳（毫秒），null表示离线
     */
    public Long getLastActiveTime(Long identityId, Integer userType) {
        PresenceInfo info = getLastActiveInfo(identityId, userType);
        return info != null ? info.getTimestamp() : null;
    }

    /**
     * 获取用户最后活跃的设备ID
     *
     * @param identityId 用户身份ID
     * @param userType 用户类型
     * @return 设备ID，null表示离线
     */
    public String getLastActiveDeviceId(Long identityId, Integer userType) {
        PresenceInfo info = getLastActiveInfo(identityId, userType);
        return info != null ? info.getDeviceId() : null;
    }

    /**
     * 在线状态信息
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PresenceInfo {
        private final String deviceId;
        private final long timestamp;
    }

}
