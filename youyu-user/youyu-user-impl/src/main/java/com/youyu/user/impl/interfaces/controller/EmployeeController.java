package com.youyu.user.impl.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.common.model.Result;
import com.youyu.framework.context.web.resolver.ProxyRequest;
import com.youyu.user.impl.application.service.EmployeeApplicationService;
import com.youyu.user.impl.application.service.EmployeeApplicationService.CreateEmployeeRequest;
import com.youyu.user.impl.application.service.EmployeeApplicationService.UpdateEmployeeRequest;
import com.youyu.user.impl.interfaces.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 员工管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeApplicationService employeeApplicationService;

    /**
     * 创建员工
     */
    @PostMapping("/create")
    public Result<Map<String, Long>> createEmployee(@RequestBody CreateEmployeeRequest request) {
        Map<String, Long> result = employeeApplicationService.createEmployee(request);
        return Result.success(result);
    }

    /**
     * 更新员工
     */
    @PostMapping("/update")
    public Result<Void> updateEmployee(@RequestBody UpdateEmployeeRequest request) {
        employeeApplicationService.updateEmployee(request);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @PostMapping("/delete")
    public Result<Void> deleteEmployee(@RequestParam Long id) {
        employeeApplicationService.deleteEmployee(id);
        return Result.success();
    }

    /**
     * 分页查询员工列表
     */
    @GetMapping("/list")
    public Result<Page<EmployeeVO>> listEmployees(ProxyRequest q,
                                                   @RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "deptId", required = false) Long deptId,
                                                   @RequestParam(value = "status", required = false) Integer status) {
        Page<EmployeeVO> page = employeeApplicationService.listEmployees(q.getPage(), keyword, deptId, status);
        return Result.success(page);
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public Result<EmployeeVO> getEmployee(@PathVariable Long id) {
        EmployeeVO employee = employeeApplicationService.getEmployee(id);
        return Result.success(employee);
    }
}
