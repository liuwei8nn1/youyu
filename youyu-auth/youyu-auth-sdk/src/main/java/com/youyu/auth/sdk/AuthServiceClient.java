package com.youyu.auth.sdk;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.youyu.auth.api.client.AuthFeignClient;
import com.youyu.auth.api.model.Permission;
import com.youyu.auth.api.model.PermissionLevel;
import com.youyu.framework.context.UserType;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.cache.sync.core.CacheCleanHandler;
import com.youyu.framework.context.UserInfo;
import com.youyu.framework.context.web.util.RequestContextUtil;
import com.youyu.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 认证服务 SDK 客户端
 * <p>
 * 提供权限检查能力，使用 Caffeine 本地缓存（3分钟TTL）
 * 
 * <p>使用示例:</p>
 * <pre>{@code
 * @Autowired
 * private AuthServiceClient authServiceClient;
 * 
 * public void checkPermission() {
 *     // 检查当前用户是否有某个权限
 *     boolean hasPermission = authServiceClient.hasPermission("product:view");
 *     
 *     // 检查指定用户是否有某个权限
 *     boolean hasPermission = authServiceClient.hasPermission(userId, userType, "product:view");
 * }
 * }</pre>
 */
@Slf4j
public class AuthServiceClient implements CacheCleanHandler {

    private final AuthFeignClient authFeignClient;

    /**
     * 使用 Caffeine 本地缓存：key = "userId:userType", value = 权限码集合
     */
    private final Cache<String, Set<String>> permissionCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(50000)  // 最大缓存50000个用户
            .build();


    private final Cache<String, Byte> remoteGetUserPermissionsErr = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)  // 写入后3分钟过期
            .maximumSize(1000)
            .build();


    /**
     * 字符串规范器（方案 B - 可选）
     * <p>
     * 用于替代 String.intern()，适用于权限码种类非常多（> 10000）的场景
     * 优势：可监控、可清理、不受 JVM StringTable 限制
     * 劣势：需要额外维护，内存占用略高（约多 7-8 KB）
     * <p>
     * 使用方法：将 getUserPermissions() 中的 .map(String::intern) 改为 .map(this::canonicalize)
     */
    // private static final ConcurrentHashMap<String, String> STRING_CANONICALIZER = new ConcurrentHashMap<>(256);

    public AuthServiceClient(AuthFeignClient authFeignClient) {
        this.authFeignClient = authFeignClient;
    }

    /**
     * 检查当前用户是否有指定权限
     *
     * @param permissionCode 权限码
     * @return true-有权限，false-无权限
     */
    public boolean hasPermission(String permissionCode) {
        UserInfo userInfo = RequestContextUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            log.warn("用户未登录，无法检查权限");
            return false;
        }
        return hasPermission(userInfo.getUserId(), userInfo.getUserType(), permissionCode, UserType.UNKNOWN);
    }

    /**
     * 检查指定用户是否有指定权限
     *
     * @param userId         用户ID
     * @param userType       用户类型
     * @param permissionCode 权限码
     * @return true-有权限，false-无权限
     */
    public boolean hasPermission(Long userId, Integer userType, String permissionCode, UserType checkUserType) {
        if (userId == null || userType == null || permissionCode == null) {
            log.warn("参数不能为空: userId={}, userType={}, permissionCode={}", userId, userType, permissionCode);
            return false;
        }
        
        UserType userTypeEnum = UserType.of(userType);
        boolean hasPermission;
        
        // 1. 内置权限码：通过 PermissionLevel 快速判断（不需要查数据库）
        switch (permissionCode.toUpperCase()) {
            case Permission.NONE:
                hasPermission = true;
                break;
                
            case Permission.LOGIN:
                hasPermission = PermissionLevel.LOGIN.isAllowed(userTypeEnum);
                break;
                
            case Permission.CUSTOMER:
                hasPermission = PermissionLevel.CUSTOMER.isAllowed(userTypeEnum);
                break;
                
            case Permission.EMP:
                hasPermission = PermissionLevel.EMP.isAllowed(userTypeEnum);
                break;
                
            case Permission.ENTERPRISE:
                hasPermission = PermissionLevel.ENTERPRISE.isAllowed(userTypeEnum);
                break;
                
            case Permission.PLATFORM:
                hasPermission = PermissionLevel.PLATFORM.isAllowed(userTypeEnum);
                break;
                
            default:
                // 2. 自定义权限码：查询数据库的角色-菜单权限
                Set<String> permissions = getUserPermissions(userId, userType);
                hasPermission = permissions.contains(permissionCode);
                break;
        }
        
        // 3. 如果有额外的用户类型限制，需要二次校验
        if (hasPermission && checkUserType != UserType.UNKNOWN) {
            hasPermission = checkUserType == userTypeEnum;
        }
        
        log.debug("权限检查: userId={}, userType={}, permissionCode={}, result={}", 
                userId, userType, permissionCode, hasPermission);
        return hasPermission;
    }

    /**
     * 检查当前用户是否有任一权限
     *
     * @param permissionCodes 权限码列表
     * @return true-有任一权限，false-无任何权限
     */
    public boolean hasAnyPermission(String... permissionCodes) {
        UserInfo userInfo = RequestContextUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return false;
        }
        return hasAnyPermission(userInfo.getUserId(), userInfo.getUserType(), permissionCodes);
    }

    /**
     * 检查指定用户是否有任一权限
     *
     * @param userId          用户ID
     * @param userType        用户类型
     * @param permissionCodes 权限码列表
     * @return true-有任一权限，false-无任何权限
     */
    public boolean hasAnyPermission(Long userId, Integer userType, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }

        Set<String> permissions = getUserPermissions(userId, userType);
        for (String code : permissionCodes) {
            if (permissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前用户是否有所有权限
     *
     * @param permissionCodes 权限码列表
     * @return true-有所有权限，false-缺少任一权限
     */
    public boolean hasAllPermissions(String... permissionCodes) {
        UserInfo userInfo = RequestContextUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return false;
        }
        return hasAllPermissions(userInfo.getUserId(), userInfo.getUserType(), permissionCodes);
    }

    /**
     * 检查指定用户是否有所有权限
     *
     * @param userId          用户ID
     * @param userType        用户类型
     * @param permissionCodes 权限码列表
     * @return true-有所有权限，false-缺少任一权限
     */
    public boolean hasAllPermissions(Long userId, Integer userType, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }

        Set<String> permissions = getUserPermissions(userId, userType);
        for (String code : permissionCodes) {
            if (!permissions.contains(code)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取用户的权限码集合（带二级缓存：Caffeine + Redis）
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 权限码集合
     */
    @NonNull
    public Set<String> getUserPermissions(Long userId, Integer userType) {
        String cacheKey = buildCacheKey(userId, userType);
        Set<String> cachedPermissions =  permissionCache.get(cacheKey,(k) -> {
                // 缓存未命中，远程调用获取权限
                log.debug("Caffeine userId：{} userType：{} L1缓存命中: {}", userId, userType, k);
                return remoteGetUserPermissions(userId, userType, cacheKey);
            }
        );
        return cachedPermissions == null ? Collections.emptySet() : cachedPermissions;
    }

    private @Nullable Set<String> remoteGetUserPermissions(Long userId, Integer userType,String cacheKey) {
        try {
            if(remoteGetUserPermissionsErr.getIfPresent(cacheKey) == null){
                Result<List<String>> result = authFeignClient.getUserPermissions(userId, userType);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    // 对权限码调用 intern()，利用 JVM 字符串常量池减少重复字符串的内存占用
                    // 原理：相同内容的字符串共享同一对象引用，避免堆上创建多个副本
                    // 适用场景：权限码种类有限（< 1000 种）、重复率高
                    // 内存节省：对于 50000 用户 × 20 权限/用户，可节省约 60-70 MB
                    return CollectionUtil.toSet(result.getData(), String::intern);
                }
            }
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}, userType={}", userId, userType, e);
            remoteGetUserPermissionsErr.put(cacheKey, (byte) 1);
        }
        return null;
    }

    /**
     * 清除指定用户的权限缓存（同时清除L1和L2缓存）
     *
     * @param userId   用户ID
     * @param userType 用户类型
     */
    public void evictPermissionCache(Long userId, Integer userType) {
        String cacheKey = buildCacheKey(userId, userType);
        
        // 清除 L1 Caffeine 缓存
        permissionCache.invalidate(cacheKey);
        log.debug("清除Caffeine L1缓存: {}", cacheKey);
    }

    /**
     * 清除所有权限缓存
     */
    public void evictAllPermissionCache() {
        long size = permissionCache.estimatedSize();
        permissionCache.invalidateAll();
        log.info("清除所有权限缓存，共 {} 条", size);
    }

    /**
     * 构建缓存 Key
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 缓存 Key
     */
    private String buildCacheKey(Long userId, Integer userType) {
        return userId + ":" + userType;
    }

    @Override
    public String supportType() {
        return "auth";
    }

    @Override
    public String supportSubType() {
        return "permission";
    }

    @Override
    public void cacheSync(String type, String subType, String cacheKey, Map<String, String> metadata) {
        permissionCache.invalidate(cacheKey);
    }

    // ==================== 方案 B：手动字符串规范器（可选） ====================
    
    /**
     * 字符串规范化方法（方案 B）
     * <p>
     * 当权限码种类非常多（> 10000）或需要精确控制时使用此方案替代 intern()
     * <p>
     * 优势：
     * 1. 完全可控：可以监控命中率、动态清理、设置最大容量
     * 2. 不影响 JVM 全局 StringTable
     * 3. 高并发性能更好（ConcurrentHashMap 分段锁）
     * <p>
     * 劣势：
     * 1. 需要额外维护生命周期
     * 2. 内存占用略高于 intern()（约多 7-8 KB，可忽略）
     * <p>
     * 启用方法：
     * 1. 取消注释 STRING_CANONICALIZER 字段
     * 2. 将 getUserPermissions() 中的 .map(String::intern) 改为 .map(this::canonicalize)
     *
     * @param s 待规范的字符串
     * @return 规范化的字符串（相同内容返回同一对象引用）
     */
    // private String canonicalize(String s) {
    //     if (s == null) {
    //         return null;
    //     }
    //     // computeIfAbsent 保证相同内容返回同一对象引用
    //     return STRING_CANONICALIZER.computeIfAbsent(s, k -> k);
    // }
}
