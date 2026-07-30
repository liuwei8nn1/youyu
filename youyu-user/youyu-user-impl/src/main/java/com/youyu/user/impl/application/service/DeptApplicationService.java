package com.youyu.user.impl.application.service;

import com.youyu.user.impl.domain.model.Dept;
import com.youyu.user.impl.domain.repository.DeptRepository;
import com.youyu.user.impl.interfaces.converter.DeptConverter;
import com.youyu.user.impl.interfaces.vo.DeptVO;
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
    public List<DeptVO> getDeptTree() {
        List<Dept> allDepts = deptRepository.listAll();
        return buildDeptVOTree(allDepts, 0L);
    }

    /**
     * 获取所有部门（平铺列表）
     */
    public List<DeptVO> getAllDepts() {
        return DeptConverter.INSTANCE.toVOList(deptRepository.listAll());
    }

    private List<DeptVO> buildDeptVOTree(List<Dept> allDepts, Long parentId) {
        if (CollectionUtils.isEmpty(allDepts)) {
            return Collections.emptyList();
        }
        return allDepts.stream()
                .filter(dept -> dept.getParentId().equals(parentId))
                .map(dept -> {
                    DeptVO vo = DeptConverter.INSTANCE.toVO(dept);
                    vo.setChildren(buildDeptVOTree(allDepts, dept.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
