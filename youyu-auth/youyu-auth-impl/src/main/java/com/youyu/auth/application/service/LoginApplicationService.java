package com.youyu.auth.application.service;

import com.youyu.auth.api.model.AuthRedisKey;
import com.youyu.auth.domain.model.UserDevice;
import com.youyu.auth.domain.model.UserIdentity;
import com.youyu.auth.domain.repository.UserDeviceRepository;
import com.youyu.auth.domain.repository.UserIdentityRepository;
import com.youyu.auth.infrastructure.external.adapter.UserServiceAdapter;
import com.youyu.auth.infrastructure.persistence.config.LoginModeConfig;
import com.youyu.auth.infrastructure.persistence.redis.AuthRedisService;
import com.youyu.auth.api.JwtTokenProvider;
import com.youyu.auth.interfaces.dto.*;
import com.youyu.common.util.CollectionUtil;
import com.youyu.common.util.StringUtil;
import com.youyu.framework.cache.redis.RedisUtil;
import com.youyu.framework.context.UserInfo;
import com.youyu.user.api.dto.UserLoginInfo;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 登录应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginApplicationService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRedisService authRedisService;
    private final LoginModeConfig loginModeConfig;
    private final UserServiceAdapter userServiceAdapter;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip      客户端IP
     * @param userAgent User-Agent
     * @return 登录响应（包含Token）
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        log.info("用户登录，loginType: {}, credential: {}, deviceUniqueId: {}",
                request.getLoginType(), maskCredential(request.getCredential()), request.getDeviceUniqueId());

        // 根据登录类型查询用户信息（调用 user ）
        UserLoginInfo userInfo = queryUserByLoginType(request);
        Long userId = userInfo.getUserId();
        String username = userInfo.getUsername();

        // 获取用户类型（从请求中获取，如果未提供则默认为普通用户）
        Integer userType = request.getUserType() != null ? request.getUserType() : 1; // 默认 user

        // 查询用户身份（从 youyu-auth 数据库，使用 userId + userType 联合查询）
        UserIdentity userIdentity = userIdentityRepository.findByUserIdAndType(userId, userType)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取用户身份ID (sid)
        Long userIdentityId = userIdentity.getId();

        // 验证用户状态
        if (!userIdentity.isActive()) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码（仅用户名登录需要）
        if ("USERNAME".equals(request.getLoginType())) {
            if (!userIdentity.verifyPassword(request.getPassword(), userIdentity.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
        }
        // TODO: 手机号/邮箱登录需要验证验证码

        // 检查用户是否被禁用（Redis）
        if (authRedisService.isUserDisabled(userId, userType)) {
            throw new RuntimeException("账号已被禁用：" + authRedisService.getDisableReason(userId, userType));
        }

        // 获取用户角色列表（暂时返回空列表，后续从数据库查询）
        List<String> roleList = Collections.emptyList();

        // 根据登录模式确定需要踢出的设备
        LoginModeConfig.LoginMode mode = loginModeConfig.parseGlobalMode();
        Pair<List<Long>, UserDevice> devicesToKickPair = determineDevicesToKick(userIdentityId, request.getDeviceUniqueId(), mode);
        List<Long> devicesToKick = devicesToKickPair.getKey();
        UserDevice currDevice = devicesToKickPair.getValue();
        
        // 处理当前设备（determineDevicesToKick 可能已经找到了当前设备）
        if (currDevice == null) {
            // 当前设备不在在线列表中，需要从数据库查询（可能是离线状态）
            currDevice = userDeviceRepository.findByIdentityIdAndDeviceUniqueId(userIdentityId, request.getDeviceUniqueId()).orElse(null);
        }
        
        if (currDevice == null) {
            // 新设备，创建记录
            currDevice = UserDevice.create(userIdentityId, request.getDeviceUniqueId(), ip, userAgent);
            userDeviceRepository.save(currDevice);
        } else {
            // 已有设备，更新信息
            currDevice.updateLoginInfo(ip, userAgent);
            userDeviceRepository.update(currDevice);
        }

        // 获取设备ID
        Long deviceId = currDevice.getId();

        // 批量执行 Redis 操作（使用管道提升性能）
        executeLoginRedisOperations(userId, userType, deviceId, devicesToKick);
        
        if (!devicesToKick.isEmpty()) {
            log.info("踢出设备，userId: {}, devices: {}", userId, devicesToKick);
        }

        String roles = StringUtil.join(roleList, t -> t);
        // 生成 Access Token + Refresh Token - Token中存储的是 deviceId
        String accessToken = jwtTokenProvider.generateAccessToken(
                userIdentityId,  // sid
                userId,
                username,
                userType,
                roles,
                deviceId
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                userIdentityId,  // sid
                userId,
                username,
                userType,
                roles,
                deviceId); // 不使用 version

        log.info("用户登录成功，userId: {}, userType: {}, deviceUniqueId: {}, deviceId: {}",
                userId, userType, request.getDeviceUniqueId(), deviceId);

        // 返回登录响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .username(username)
                .userType(userType)
                .roles(roleList)
                .build();
    }

    /**
     * 根据登录类型查询用户信息
     */
    private UserLoginInfo queryUserByLoginType(LoginRequest request) {
        String loginType = request.getLoginType();
        String credential = request.getCredential();
        Integer userType = request.getUserType() != null ? request.getUserType() : 1; // 默认 user

        return switch (loginType) {
            case "USERNAME" -> userServiceAdapter.findByUsername(credential, userType)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            case "PHONE" -> userServiceAdapter.findByPhone(credential, userType)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            case "EMAIL" -> userServiceAdapter.findByEmail(credential, userType)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            default -> throw new RuntimeException("不支持的登录类型: " + loginType);
        };
    }

    /**
     * 脱敏凭证信息（日志用）
     */
    private String maskCredential(String credential) {
        if (credential == null || credential.length() <= 2) {
            return "***";
        }
        return credential.charAt(0) + "***" + credential.charAt(credential.length() - 1);
    }

    /**
     * 刷新 Token
     * <p>
     * 注意：deviceId 从 Refresh Token 中解析，不需要前端传递
     * 这样可以避免 deviceId 泄漏问题，也符合 OAuth 2.0 标准
     * <p>
     * Refresh Token 滚动刷新策略：
     * - 每次刷新生成新的 Refresh Token
     * - 旧 Token 依靠 JWT 有效期自然过期（无需额外存储）
     * - 如果被踢出，通过 Redis "exit" 标记立即失效
     *
     * @param refreshToken Refresh Token
     * @return 新的 Token
     */
    public RefreshTokenResponse refreshToken(String refreshToken) {
        // 1. 验证 Refresh Token 签名、有效期
        Claims claims = jwtTokenProvider.validateToken(refreshToken);
        UserInfo userInfo = jwtTokenProvider.getUserInfo(claims);
        Long userIdentityId = userInfo.getSid();  // 从 Token 中解析 sid
        Long deviceId = userInfo.getDeviceId(); // 从 Token 中解析 deviceId
        Long userId = userInfo.getUserId();
        Integer userType = userInfo.getUserType();
        String username = userInfo.getUsername();

        log.info("刷新Token，userId: {}, deviceId: {}", userId, deviceId);

        // 2. 检查用户是否被禁用
        if (authRedisService.isUserDisabled(userId, userType)) {
            throw new RuntimeException("账号已被禁用");
        }

        // 3. 检查设备是否在线（使用 deviceId，Long类型）
        if (!authRedisService.isDeviceOnline(userId, userType, deviceId)) {
            throw new RuntimeException("设备未登录或已被踢下线");
        }

        // 【优化】直接从 Token 中获取用户信息，无需查询数据库
        // Token 中已包含：userId, username, userType, roles, deviceId
        List<String> roleList = Collections.emptyList(); // TODO: 如果需要角色，可以从 Token 中解析

        // 4. 生成新的 Access Token - Token中存储的是 deviceId（Long类型）
        String roles = StringUtil.join(roleList, t -> t);
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userIdentityId,  // sid
                userId,
                username,
                userType,
                roles,
                deviceId
        );

        // 5. 生成新的 Refresh Token（滚动刷新）
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                userIdentityId,  // sid
                userId,
                username,
                userType,
                roles,
                deviceId
        );

        // 6. 更新在线状态（利用 Token 刷新机制，每15分钟更新一次）
        authRedisService.updatePresence(userId, userType, deviceId);

        log.info("Token刷新成功，userId: {}, deviceId: {}", userId, deviceId);

        return com.youyu.auth.interfaces.dto.RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * 用户登出（当前设备）
     *
     * @param identityId 用户身份ID
     * @param deviceId 设备ID（UserDevice表的id，Long类型）
     */
    @Transactional
    public void logout(Long identityId, Integer userType, Long deviceId) {
        log.info("用户登出，identityId: {}, deviceId: {}", identityId, deviceId);

        // 使用管道批量执行 Redis 操作
        String deviceKey = AuthRedisKey.calcDeviceKey(identityId, userType, String.valueOf(deviceId));
        String presenceKey = AuthRedisKey.calcPresenceKey(identityId, userType);
        long ttl = jwtTokenProvider.getRefreshTokenTtl();
        
        RedisUtil.execInPipeline(redisOps -> {
            // 1. 标记设备离线
            redisOps.opsForValue().set(deviceKey, "exit", ttl, TimeUnit.SECONDS);
            
            // 2. 删除用户在线状态
            redisOps.delete(presenceKey);
        });

        // 3. 更新数据库设备状态 - 使用 deviceId（Long类型）
        userDeviceRepository.markOffline(identityId, userType, deviceId);

        log.info("用户登出成功，identityId: {}, deviceId: {}", identityId, deviceId);
    }

    /**
     * 退出所有设备
     *
     * @param identityId 用户身份ID
     */
    public void logoutAll(Long identityId, Integer userType, @Nullable Long excludeDeviceId) {
        log.info("退出所有设备，identityId: {}", identityId);

        // 1. 查询所有在线设备
        List<UserDevice> onlineDevices = userDeviceRepository.findOnlineByIdentityId(identityId, excludeDeviceId);

        if (!onlineDevices.isEmpty()) {
            // 2. 使用管道批量标记所有设备离线
            List<Long> deviceIds = CollectionUtil.toList(onlineDevices, UserDevice::getId);
            long ttl = jwtTokenProvider.getRefreshTokenTtl();
            String presenceKey = AuthRedisKey.calcPresenceKey(identityId, userType);

            RedisUtil.execInPipeline(redisOps -> {
                for (Long deviceId : deviceIds) {
                    String deviceKey = AuthRedisKey.calcDeviceKey(identityId, userType, String.valueOf(deviceId));
                    redisOps.opsForValue().set(deviceKey, "exit", ttl, TimeUnit.SECONDS);
                }

                // 删除用户在线状态
                redisOps.delete(presenceKey);
            });
        }

        // 3. 更新数据库所有设备状态为离线 - 使用 deviceId（Long类型）
        for (UserDevice device : onlineDevices) {
            userDeviceRepository.markOffline(identityId, userType, device.getId());
        }

        log.info("退出所有设备成功，identityId: {}, count: {}", identityId, onlineDevices.size());
    }

    /**
     * 执行登录相关的 Redis 操作（使用管道批量执行以提升性能）
     *
     * @param identityId 用户身份ID
     * @param userType     用户类型
     * @param deviceId     设备ID
     * @param devicesToKick 需要踢出的设备ID列表
     */
    private void executeLoginRedisOperations(Long identityId, Integer userType, Long deviceId, List<Long> devicesToKick) {
        RedisUtil.execInPipeline(redisOps -> {
            // 1. 标记被踢设备离线
            if (!devicesToKick.isEmpty()) {
                long ttl = jwtTokenProvider.getRefreshTokenTtl();
                for (Long kickDeviceId : devicesToKick) {
                    String key = AuthRedisKey.calcDeviceKey(identityId, userType, String.valueOf(kickDeviceId));
                    redisOps.opsForValue().set(key, "exit", ttl, TimeUnit.SECONDS);
                }
            }
            
            // 2. 标记当前设备在线（删除可能存在的"exit"标记）
            String currentDeviceKey = AuthRedisKey.calcDeviceKey(identityId, userType, String.valueOf(deviceId));
            redisOps.delete(currentDeviceKey);
            
            // 3. 更新用户在线状态
            String presenceKey = AuthRedisKey.calcPresenceKey(identityId, userType);
            long now = System.currentTimeMillis();
            String presenceValue = deviceId + ":" + now;
            long presenceTtl = jwtTokenProvider.getAccessTokenTtl() + 60; // TTL = AccessToken TTL + 缓冲时间
            redisOps.opsForValue().set(presenceKey, presenceValue, presenceTtl, TimeUnit.SECONDS);
        });
    }

    /**
     * 根据登录模式确定需要踢出的设备
     *
     * @param identityId 用户身份ID
     * @param deviceUniqueId  当前设备唯一ID（String类型）
     * @param mode      登录模式
     * @return Pair<需要踢出的设备ID列表, 当前设备（如果存在且在线）>
     */
    private Pair<List<Long>, UserDevice> determineDevicesToKick(Long identityId, String deviceUniqueId, LoginModeConfig.LoginMode mode) {
        List<UserDevice> onlineDevices = userDeviceRepository.findOnlineByIdentityId(identityId);
        
        // 找到当前设备（如果在线）
        UserDevice currDevice = CollectionUtil.findFirst(onlineDevices, d -> d.getDeviceUniqueId().equals(deviceUniqueId));
        
        // 排除当前设备后的其他在线设备
        List<UserDevice> otherDevices = CollectionUtil.filter(onlineDevices, d -> !d.getDeviceUniqueId().equals(deviceUniqueId));
        
        List<Long> toKickList;
        switch (mode) {
            case SINGLE:
                // 唯一登录，踢出所有其他设备
                toKickList = CollectionUtil.toList(otherDevices, UserDevice::getId);
                break;
                        
            case MAX:
                // 最多 n 端登录
                int maxDevices = mode.getMaxDevices();
                
                // 计算当前占用的设备数（当前设备如果存在且在线，算1个）
                int currentCount = currDevice != null ? 1 : 0;
                int availableSlots = maxDevices - currentCount;
                
                if (otherDevices.size() <= availableSlots) {
                    // 未达到上限，不需要踢出
                    toKickList = Collections.emptyList();
                } else {
                    // 达到上限，踢出最旧的设备,查询时已经排序了的
                    // 踢出超出限制的设备
                    toKickList = CollectionUtil.toList(
                            otherDevices.subList(availableSlots, otherDevices.size()),
                            UserDevice::getId
                    );
                }
                break;
                
            case MULTI:
            default:
                // 无限制多端登录，不需要踢出
                toKickList = Collections.emptyList();
                break;
        }
        
        return Pair.of(toKickList, currDevice);
    }
}
