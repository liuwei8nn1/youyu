package com.youyu.user.impl.application.service;

import com.youyu.framework.context.UserType;
import com.youyu.user.api.dto.CreateUserRequest;
import com.youyu.user.impl.domain.model.Customer;
import com.youyu.user.impl.domain.model.Employee;
import com.youyu.user.impl.domain.model.PlatformUser;
import com.youyu.user.impl.domain.model.UserProfile;
import com.youyu.user.impl.domain.repository.CustomerRepository;
import com.youyu.user.impl.domain.repository.EmployeeRepository;
import com.youyu.user.impl.domain.repository.PlatformUserRepository;
import com.youyu.user.impl.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户创建应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreateApplicationService {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PlatformUserRepository platformUserRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * 创建用户（根据userType创建对应的资料表和user_profile）
     *
     * @param request 创建用户请求
     * @return 用户ID
     */
    @Transactional
    public Long createUser(CreateUserRequest request) {
        log.info("创建用户，username: {}, userType: {}", request.getUsername(), request.getUserType());

        UserType userType = UserType.of(request.getUserType());

        // 1. 根据用户类型检查唯一性并创建对应的资料记录
        switch (userType) {
            case CUSTOMER:
                createCustomer(request);
                break;
            case ENTERPRISE:
                createEmployee(request);
                break;
            case PLATFORM:
                createPlatformUser(request);
                break;
            default:
                throw new IllegalArgumentException("不支持的用户类型: " + userType);
        }

        // 2. 创建user_profile记录（通用用户资料）
        UserProfile profile = UserProfile.create(
                request.getUserId(),
                request.getUsername(),
                request.getNickname(),
                request.getPhone()
        );
        profile.updateProfile(
                request.getNickname(),
                null, // avatar
                request.getEmail(),
                request.getPhone(),
                0, // gender
                null, // birthday
                null  // signature
        );
        userProfileRepository.save(profile);
        log.info("UserProfile创建成功，userId: {}", request.getUserId());

        return request.getUserId();
    }

    /**
     * 创建外部顾客资料
     */
    private void createCustomer(CreateUserRequest request) {
        // 检查唯一性
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (customerRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("手机号已被注册: " + request.getPhone());
            }
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("邮箱已被注册: " + request.getEmail());
            }
        }

        // 创建customer记录
        Customer customer = Customer.create(
                request.getUserId(),
                request.getUsername(),
                request.getPhone(),
                request.getEmail()
        );
        customerRepository.save(customer);
        log.info("Customer创建成功，userId: {}", request.getUserId());
    }

    /**
     * 创建企业员工资料
     */
    private void createEmployee(CreateUserRequest request) {
        // 检查唯一性
        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (employeeRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("手机号已被注册: " + request.getPhone());
            }
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("邮箱已被注册: " + request.getEmail());
            }
        }

        // 创建employee记录
        Employee employee = Employee.create(
                request.getUserId(),
                request.getUsername(),
                request.getPhone(),
                request.getEmail()
        );
        employeeRepository.save(employee);
        log.info("Employee创建成功，userId: {}", request.getUserId());
    }

    /**
     * 创建平台管理员资料
     */
    private void createPlatformUser(CreateUserRequest request) {
        // 检查唯一性
        if (platformUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (platformUserRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("手机号已被注册: " + request.getPhone());
            }
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (platformUserRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("邮箱已被注册: " + request.getEmail());
            }
        }

        // 创建platform_user记录
        PlatformUser platformUser = PlatformUser.create(
                request.getUserId(),
                request.getUsername(),
                request.getPhone(),
                request.getEmail()
        );
        platformUserRepository.save(platformUser);
        log.info("PlatformUser创建成功，userId: {}", request.getUserId());
    }
}
