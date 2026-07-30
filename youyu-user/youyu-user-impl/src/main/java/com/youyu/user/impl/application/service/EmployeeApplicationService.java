package com.youyu.user.impl.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.auth.api.client.UserIdentityCreateApi;
import com.youyu.auth.api.dto.CreateUserIdentityRequest;
import com.youyu.auth.api.dto.CreateUserIdentityResponse;
import com.youyu.common.model.Result;
import com.youyu.framework.context.UserType;
import com.youyu.user.impl.domain.model.Employee;
import com.youyu.user.impl.domain.repository.EmployeeRepository;
import com.youyu.user.impl.interfaces.converter.EmployeeConverter;
import com.youyu.user.impl.interfaces.vo.EmployeeVO;
import com.youyu.user.impl.infrastructure.persistence.entity.EmployeeDO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工管理应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeApplicationService {

    private final EmployeeRepository employeeRepository;
    private final UserIdentityCreateApi userIdentityCreateApi;

    /**
     * 创建员工（同时创建登录账号）
     * @return Map containing employeeId and userId
     */
    @Transactional
    public Map<String, Long> createEmployee(CreateEmployeeRequest request) {
        log.info("创建员工，username: {}, deptId: {}", request.getUsername(), request.getDeptId());

        // 1. 检查用户名唯一性
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

        // 2. 调用 auth 服务创建用户身份
        CreateUserIdentityRequest authRequest = new CreateUserIdentityRequest();
        authRequest.setUsername(request.getUsername());
        authRequest.setPassword(request.getPassword());
        authRequest.setPhone(request.getPhone());
        authRequest.setUserType(UserType.ENTERPRISE.getValue());
        authRequest.setEnabled(true);

        Result<CreateUserIdentityResponse> authResult = userIdentityCreateApi.createUserIdentity(authRequest);
        if (!authResult.isSuccess()) {
            throw new IllegalArgumentException("创建登录账号失败: " + authResult.getMessage());
        }

        Long identityId = authResult.getData().getUserId();
        log.info("用户身份创建成功，identityId: {}", identityId);

        // 3. 创建 employee 记录
        Employee employee = Employee.create(identityId, request.getUsername(), request.getPhone(), request.getEmail());
        employee.setDeptId(request.getDeptId());
        employee.setPosition(request.getPosition());
        employee.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        employee.setHireDate(LocalDateTime.now());

        Long employeeId = employeeRepository.save(employee);
        log.info("员工资料创建成功，employeeId: {}", employeeId);

        // 注意：角色分配由前端调用 Auth 服务的 API 完成
        // if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
        //     调用 Auth 服务分配角色
        // }

        // 返回 employeeId 和 identityId（用于前端调用 Auth 服务分配角色）
        Map<String, Long> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("identityId", identityId);
        return result;
    }

    /**
     * 更新员工信息
     */
    @Transactional
    public void updateEmployee(UpdateEmployeeRequest request) {
        log.info("更新员工信息，id: {}", request.getId());

        Employee employee = employeeRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + request.getId()));

        // 更新基本信息
        employee.setDeptId(request.getDeptId());
        employee.setPosition(request.getPosition());
        employee.setStatus(request.getStatus());

        employeeRepository.update(employee);
        log.info("员工信息更新成功，id: {}", request.getId());

        // 注意：角色更新由前端调用 Auth 服务的 API 完成
        // if (request.getRoleIds() != null) {
        //     调用 Auth 服务更新角色
        // }
    }

    /**
     * 删除员工（逻辑删除）
     */
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("删除员工，id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + id));

        // 注意：角色移除由前端调用 Auth 服务的 API 完成
        // userRoleApplicationService.removeAllRolesFromUser(employee.getUserId());

        // 逻辑删除
        employeeRepository.removeById(id);
        log.info("员工删除成功，id: {}", id);
    }

    /**
     * 分页查询员工列表
     */
    public Page<EmployeeVO> listEmployees(Page<EmployeeDO> page, String keyword, Long deptId, Integer status) {
        Page<Employee> employeePage = employeeRepository.listPage(page, keyword, deptId, status);
        Page<EmployeeVO> result = new Page<>(employeePage.getCurrent(), employeePage.getSize(), employeePage.getTotal());
        result.setRecords(employeePage.getRecords().stream().map(EmployeeConverter.INSTANCE::toVO).collect(Collectors.toList()));
        return result;
    }

    /**
     * 获取员工详情
     */
    public EmployeeVO getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + id));
        return EmployeeConverter.INSTANCE.toVO(employee);
    }

    // ==================== DTO ====================

    @Data
    public static class CreateEmployeeRequest {
        private String username;
        private String password;
        private String phone;
        private String email;
        private Long deptId;
        private String position;
        private Integer status;
        private List<Long> roleIds;
    }

    @Data
    public static class UpdateEmployeeRequest {
        private Long id;
        private Long deptId;
        private String position;
        private Integer status;
        private List<Long> roleIds;
    }
}
