package com.youyu.user.impl.interfaces.controller;

import com.youyu.common.model.Result;
import com.youyu.user.impl.application.service.DeptApplicationService;
import com.youyu.user.impl.domain.model.Dept;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptApplicationService deptApplicationService;

    /**
     * 创建部门
     */
    @PostMapping("/create")
    public Result<Long> createDept(@RequestBody CreateDeptRequest request) {
        Long deptId = deptApplicationService.createDept(
                request.getParentId(),
                request.getDeptName(),
                request.getDeptCode(),
                request.getLeader(),
                request.getPhone(),
                request.getEmail(),
                request.getSortOrder()
        );
        return Result.success(deptId);
    }

    /**
     * 更新部门
     */
    @PostMapping("/update")
    public Result<Void> updateDept(@RequestBody UpdateDeptRequest request) {
        deptApplicationService.updateDept(
                request.getDeptId(),
                request.getDeptName(),
                request.getDeptCode(),
                request.getLeader(),
                request.getPhone(),
                request.getEmail(),
                request.getSortOrder(),
                request.getStatus()
        );
        return Result.success();
    }

    /**
     * 删除部门
     */
    @PostMapping("/delete")
    public Result<Void> deleteDept(@RequestParam Long deptId) {
        deptApplicationService.deleteDept(deptId);
        return Result.success();
    }

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    public Result<List<Dept>> getDeptTree() {
        List<Dept> depts = deptApplicationService.getDeptTree();
        return Result.success(depts);
    }

    /**
     * 获取所有部门（平铺列表）
     */
    @GetMapping("/all")
    public Result<List<Dept>> getAllDepts() {
        List<Dept> depts = deptApplicationService.getAllDepts();
        return Result.success(depts);
    }

    // ==================== DTO类 ====================

    @Data
    public static class CreateDeptRequest {
        private Long parentId;
        private String deptName;
        private String deptCode;
        private String leader;
        private String phone;
        private String email;
        private Integer sortOrder;
    }

    @Data
    public static class UpdateDeptRequest {
        private Long deptId;
        private String deptName;
        private String deptCode;
        private String leader;
        private String phone;
        private String email;
        private Integer sortOrder;
        private Integer status;
    }
}
