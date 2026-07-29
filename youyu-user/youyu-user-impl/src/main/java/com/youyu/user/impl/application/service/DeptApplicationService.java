package com.youyu.user.impl.application.service;

import com.youyu.user.impl.domain.aggregate.Dept;
import com.youyu.user.impl.domain.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptApplicationService {

    private final DeptRepository deptRepository;

    /**
     * 创建部门
     */
    @Transactional
    public Long createDept(Long parentId, String deptName, String deptCode, String leader,
                           String phone, String email, Integer sortOrder) {
        // 检查父部门是否存在（parentId=0表示根部门，不需要检查）
        if (parentId != null && parentId != 0) {
            deptRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("父部门不存在: " + parentId));
        }

        // 检查部门编码唯一性
        if (deptCode != null && !deptCode.isEmpty()) {
            if (deptRepository.existsByDeptCode(deptCode)) {
                throw new IllegalArgumentException("部门编码已存在: " + deptCode);
            }
        }

        Dept dept = Dept.create(parentId, deptName, deptCode, sortOrder);
        dept.setLeader(leader);
        dept.setPhone(phone);
        dept.setEmail(email);

        return deptRepository.save(dept);
    }

    /**
     * 更新部门
     */
    @Transactional
    public void updateDept(Long deptId, String deptName, String deptCode, String leader,
                           String phone, String email, Integer sortOrder, Integer status) {
        Dept dept = deptRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + deptId));

        dept.updateInfo(deptName, deptCode, leader, phone, email, sortOrder, status);
        deptRepository.update(dept);
    }

    /**
     * 删除部门
     */
    @Transactional
    public void deleteDept(Long deptId) {
        Dept dept = deptRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + deptId));

        // 检查是否有子部门
        if (deptRepository.hasChildren(deptId)) {
            throw new IllegalStateException("部门下存在子部门，无法删除");
        }

        deptRepository.removeById(deptId);
    }

    /**
     * 获取所有部门（树形结构）
     */
    public List<Dept> getDeptTree() {
        List<Dept> allDepts = deptRepository.listAll();
        return buildDeptTree(allDepts, 0L);
    }

    /**
     * 获取所有部门（平铺列表）
     */
    public List<Dept> getAllDepts() {
        return deptRepository.listAll();
    }

    /**
     * 构建部门树
     */
    private List<Dept> buildDeptTree(List<Dept> allDepts, Long parentId) {
        if (CollectionUtils.isEmpty(allDepts)) {
            return Collections.emptyList();
        }

        return allDepts.stream()
                .filter(dept -> dept.getParentId().equals(parentId))
                .peek(dept -> {
                    // 递归设置子部门
                    List<Dept> children = buildDeptTree(allDepts, dept.getId());
                    dept.setChildren(children);
                })
                .collect(Collectors.toList());
    }
}
