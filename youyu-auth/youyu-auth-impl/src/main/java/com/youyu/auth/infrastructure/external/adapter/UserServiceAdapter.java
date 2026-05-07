package com.youyu.auth.infrastructure.external.adapter;

import com.youyu.common.model.Result;
import com.youyu.user.api.client.UserFeignClient;
import com.youyu.user.api.dto.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 用户服务适配器
 * 封装对 user-service 的调用，提供统一的异常处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceAdapter {

    private final UserFeignClient userFeignClient;

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @param userType 用户类型：1-user, 2-merchant
     * @return 用户登录信息
     */
    public Optional<UserLoginInfo> findByUsername(String username, Integer userType) {
        try {
            log.debug("调用 user-service 查询用户，username: {}, userType: {}", username, userType);
            
            Result<UserLoginInfo> result = userFeignClient.getByUsername(username, userType);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                log.debug("查询用户成功，username: {}", username);
                return Optional.of(result.getData());
            } else {
                log.warn("查询用户失败，username: {}, userType: {}, message: {}", username, userType,
                        result != null ? result.getMessage() : "null");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("调用 user-service 查询用户异常，username: {}, userType: {}", username, userType, e);
            throw new RuntimeException("调用用户服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @param userType 用户类型：1-user, 2-merchant
     * @return 用户登录信息
     */
    public Optional<UserLoginInfo> findByPhone(String phone, Integer userType) {
        try {
            log.debug("调用 user-service 查询用户，phone: {}, userType: {}", phone, userType);
            
            Result<UserLoginInfo> result = userFeignClient.getByPhone(phone, userType);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                log.debug("查询用户成功，phone: {}", phone);
                return Optional.of(result.getData());
            } else {
                log.warn("查询用户失败，phone: {}, userType: {}, message: {}", phone, userType,
                        result != null ? result.getMessage() : "null");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("调用 user-service 查询用户异常，phone: {}, userType: {}", phone, userType, e);
            throw new RuntimeException("调用用户服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @param userType 用户类型：1-user, 2-merchant
     * @return 用户登录信息
     */
    public Optional<UserLoginInfo> findByEmail(String email, Integer userType) {
        try {
            log.debug("调用 user-service 查询用户，email: {}, userType: {}", email, userType);
            
            Result<UserLoginInfo> result = userFeignClient.getByEmail(email, userType);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                log.debug("查询用户成功，email: {}", email);
                return Optional.of(result.getData());
            } else {
                log.warn("查询用户失败，email: {}, userType: {}, message: {}", email, userType,
                        result != null ? result.getMessage() : "null");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("调用 user-service 查询用户异常，email: {}, userType: {}", email, userType, e);
            throw new RuntimeException("调用用户服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统计使用指定角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    public int countUsersByRoleId(Long roleId) {
        // TODO: 需要在User服务中实现此接口
        // 当前简化实现：返回0，表示未被使用
        // 实际应该调用 user-service 的 /api/user/roles/{roleId}/users/count 接口
        log.warn("countUsersByRoleId 尚未在User服务中实现，roleId: {}", roleId);
        return 0;
    }

    /**
     * 创建用户（供注册使用，一次性完成用户资料创建和默认角色分配）
     */
    public Long createUser(com.youyu.user.api.dto.CreateUserRequest request) {
        try {
            log.debug("调用 user-service 创建用户，username: {}", request.getUsername());
            Result<Long> result = userFeignClient.createUser(request);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                log.debug("创建用户成功，userId: {}", result.getData());
                return result.getData();
            } else {
                log.warn("创建用户失败，username: {}, message: {}", request.getUsername(),
                        result != null ? result.getMessage() : "null");
                throw new RuntimeException("创建用户失败: " + (result != null ? result.getMessage() : "unknown"));
            }
        } catch (Exception e) {
            log.error("调用 user-service 创建用户异常，username: {}", request.getUsername(), e);
            throw new RuntimeException("调用用户服务失败: " + e.getMessage(), e);
        }
    }
}
