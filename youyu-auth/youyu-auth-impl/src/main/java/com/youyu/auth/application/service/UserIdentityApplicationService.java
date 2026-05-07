package com.youyu.auth.application.service;

import com.youyu.auth.api.dto.CreateUserIdentityRequest;
import com.youyu.auth.api.dto.CreateUserIdentityResponse;
import com.youyu.auth.domain.model.UserIdentity;
import com.youyu.auth.domain.repository.UserIdentityRepository;
import com.youyu.framework.context.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户身份应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdentityApplicationService {

    private final UserIdentityRepository userIdentityRepository;

    /**
     * 创建用户身份
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @Transactional
    public CreateUserIdentityResponse createUserIdentity(CreateUserIdentityRequest request) {
        log.info("开始创建用户身份，username: {}, userType: {}", request.getUsername(), request.getUserType());

        // 1. 检查用户名唯一性
        if (userIdentityRepository.existsByUsername(request.getUsername(), request.getUserType())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }

        // 2. 加密密码
        String encryptedPassword = UserIdentity.encryptPassword(request.getPassword());

        // 3. 创建 UserIdentity（领域层）
        UserIdentity userIdentity = UserIdentity.create(
                request.getUsername(),
                encryptedPassword,
                UserType.of(request.getUserType())
        );
        
        // 4. 设置启用状态
        if (request.getEnabled() != null) {
            userIdentity.setEnabled(request.getEnabled());
        }

        // 5. 保存
        userIdentityRepository.save(userIdentity);

        log.info("用户身份创建成功，userId: {}", userIdentity.getId());

        return CreateUserIdentityResponse.builder()
                .userId(userIdentity.getId())
                .username(request.getUsername())
                .build();
    }

    /**
     * 修改密码
     *
     * @param identityId 用户身份ID
     * @param oldPassword 原密码（明文）
     * @param newPassword 新密码（明文）
     */
    @Transactional
    public void changePassword(Long identityId, String oldPassword, String newPassword) {
        log.info("开始修改密码，identityId: {}", identityId);

        // 1. 查询用户身份
        UserIdentity userIdentity = userIdentityRepository.findById(identityId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 2. 验证用户状态
        if (!userIdentity.isActive()) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        // 3. 修改密码（领域层处理业务逻辑）
        userIdentity.changePasswordWithVerification(oldPassword, newPassword);

        // 4. 保存
        userIdentityRepository.update(userIdentity);

        log.info("密码修改成功，identityId: {}", identityId);
    }
}
