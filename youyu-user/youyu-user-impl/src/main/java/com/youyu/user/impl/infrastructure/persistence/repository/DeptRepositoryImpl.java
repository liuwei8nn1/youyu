package com.youyu.user.impl.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.user.impl.domain.aggregate.Dept;
import com.youyu.user.impl.domain.repository.DeptRepository;
import com.youyu.user.impl.infrastructure.persistence.converter.DeptConverter;
import com.youyu.user.impl.infrastructure.persistence.entity.DeptDO;
import com.youyu.user.impl.infrastructure.persistence.mapper.DeptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 部门仓储实现
 */
@Slf4j
@Repository
public class DeptRepositoryImpl extends BaseRepositoryImpl<DeptDO, DeptMapper, Long> implements DeptRepository {

    @Override
    public Long save(Dept dept) {
        DeptDO deptDO = DeptConverter.INSTANCE.toDO(dept);
        baseDao.insert(deptDO);
        log.info("部门保存成功，deptId: {}", deptDO.getId());
        return deptDO.getId();
    }

    @Override
    public void update(Dept dept) {
        DeptDO deptDO = DeptConverter.INSTANCE.toDO(dept);
        baseDao.updateById(deptDO);
        log.info("部门更新成功，deptId: {}", deptDO.getId());
    }

    @Override
    public Optional<Dept> findById(Long id) {
        DeptDO deptDO = baseDao.selectById(id);
        return Optional.ofNullable(DeptConverter.INSTANCE.toDomain(deptDO));
    }

    @Override
    public List<Dept> listAll() {
        LambdaQueryWrapper<DeptDO> wrapper = new LambdaQueryWrapper<DeptDO>()
                .orderByAsc(DeptDO::getSortOrder);
        List<DeptDO> deptDOList = baseDao.selectList(wrapper);
        return deptDOList.stream()
                .map(DeptConverter.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Dept> findByParentId(Long parentId) {
        SmartQueryWrapper<DeptDO> wrapper = new SmartQueryWrapper<DeptDO>()
                .eq(DeptDO.PARENT_ID, parentId)
                .orderByAsc(DeptDO.SORT_ORDER);
        List<DeptDO> deptDOList = baseDao.selectList(wrapper);
        return deptDOList.stream()
                .map(DeptConverter.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Dept> findByDeptCode(String deptCode) {
        SmartQueryWrapper<DeptDO> wrapper = new SmartQueryWrapper<DeptDO>()
                .eq(DeptDO.DEPT_CODE, deptCode);
        DeptDO deptDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(DeptConverter.INSTANCE.toDomain(deptDO));
    }

    @Override
    public boolean removeById(Long id) {
        int result = baseDao.deleteById(id);
        if (result > 0) {
            log.info("部门删除成功，deptId: {}", id);
        }
        return result > 0;
    }

    @Override
    public boolean hasChildren(Long parentId) {
        SmartQueryWrapper<DeptDO> wrapper = new SmartQueryWrapper<DeptDO>()
                .eq(DeptDO.PARENT_ID, parentId);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByDeptCode(String deptCode) {
        return findByDeptCode(deptCode).isPresent();
    }
}
