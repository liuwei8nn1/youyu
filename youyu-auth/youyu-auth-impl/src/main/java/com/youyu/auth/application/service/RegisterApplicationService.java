package com.youyu.auth.application.service;

import com.youyu.auth.api.dto.RegisterRequest;
import com.youyu.auth.api.dto.RegisterResponse;
import com.youyu.framework.context.UserType;
import com.youyu.auth.domain.aggregate.UserIdentity;
import com.youyu.auth.domain.repository.UserIdentityRepository;
import com.youyu.auth.infrastructure.external.adapter.UserServiceAdapter;
import com.youyu.user.api.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户注册应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterApplicationService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserServiceAdapter userServiceAdapter;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册响应
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("用户注册，username: {}, userType: {}", request.getUsername(), request.getUserType());

        // 1. 参数校验
        validateRegisterRequest(request);

        // 2. 检查用户名唯一性（在auth服务的UserIdentity中检查）
        Integer userType = request.getUserType() != null ? request.getUserType() : UserType.CUSTOMER.getValue();
        if (userIdentityRepository.existsByUsername(request.getUsername(), userType)) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }

        // 3. 使用BCrypt加密密码
        String encryptedPassword = UserIdentity.encryptPassword(request.getPassword());

        // 4. 创建UserIdentity记录（save方法会回填ID）
        UserIdentity userIdentity = UserIdentity.create(
                request.getUsername(),
                encryptedPassword,
                UserType.of(userType)
        );
        userIdentityRepository.save(userIdentity);

        Long userId = userIdentity.getId();
        log.info("UserIdentity创建成功，userId: {}", userId);

        // 5. 通过Feign调用user-service创建用户资料（根据userType创建customer/employee/platform_user）和分配默认角色（一次性完成）
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        createUserRequest.setUsername(request.getUsername());
        createUserRequest.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        createUserRequest.setPhone(request.getPhone());
        createUserRequest.setEmail(request.getEmail());
        createUserRequest.setUserType(userType);

        Long createdUserId = userServiceAdapter.createUser(createUserRequest);
        log.info("用户资料和默认角色分配成功，userId: {}", createdUserId);

        log.info("用户注册成功，userId: {}, username: {}", userId, request.getUsername());

        return RegisterResponse.builder()
                .userId(userId)
                .username(request.getUsername())
                .build();
    }

    /**
     * 校验注册请求参数
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            throw new IllegalArgumentException("用户名长度必须在3-50个字符之间");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        // 验证手机号格式（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
        }
        // 验证邮箱格式（如果提供）
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
        }
    }
}
