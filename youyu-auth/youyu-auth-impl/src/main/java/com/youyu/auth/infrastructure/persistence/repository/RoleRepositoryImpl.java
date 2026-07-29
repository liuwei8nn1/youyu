package com.youyu.auth.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.auth.domain.aggregate.Role;
import com.youyu.auth.domain.repository.RoleRepository;
import com.youyu.auth.infrastructure.external.adapter.UserServiceAdapter;
import com.youyu.auth.infrastructure.persistence.converter.RoleConverter;
import com.youyu.auth.infrastructure.persistence.entity.RoleDO;
import com.youyu.auth.infrastructure.persistence.mapper.RoleMapper;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl extends BaseRepositoryImpl<RoleDO, RoleMapper, Long> implements RoleRepository {

    private final UserServiceAdapter userServiceAdapter;

    @Override
    public int update(Role role) {
        RoleDO roleDO = RoleConverter.INSTANCE.toDO(role);
        return this.update(roleDO);
    }

    @Override
    public Long save(Role role) {
        RoleDO roleDO = RoleConverter.INSTANCE.toDO(role);
        baseDao.insert(roleDO);
        log.info("角色保存成功，roleId: {}", roleDO.getId());
        return roleDO.getId();
    }

    @Override
    public Optional<Role> findById(Long id) {
        RoleDO roleDO = baseDao.selectById(id);
        return Optional.ofNullable(RoleConverter.INSTANCE.toDomain(roleDO));
    }

    @Override
    public List<Role> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return CollectionUtil.emptyList();
        }
        SmartQueryWrapper<RoleDO> wrapper = new SmartQueryWrapper<RoleDO>()
                .in(RoleDO.ID, ids);
        List<RoleDO> roleDOList = baseDao.selectList(wrapper);
        return CollectionUtil.toList(roleDOList, RoleConverter.INSTANCE::toDomain);
    }

    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        SmartQueryWrapper<RoleDO> wrapper = new SmartQueryWrapper<RoleDO>()
                .eq(RoleDO.ROLE_CODE, roleCode);
        RoleDO roleDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(RoleConverter.INSTANCE.toDomain(roleDO));
    }

    @Override
    public List<Role> listAll() {
        List<RoleDO> roleDOList = baseDao.selectList(new SmartQueryWrapper<RoleDO>()
                .orderByAsc(RoleDO.SORT_ORDER));
        return CollectionUtil.toList(roleDOList, RoleConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Role> findByStatus(Integer status) {
        List<RoleDO> roleDOList = baseDao.selectList(new SmartQueryWrapper<RoleDO>()
                .eq(RoleDO.STATUS, status)
                .orderByAsc(RoleDO.SORT_ORDER));
        return CollectionUtil.toList(roleDOList, RoleConverter.INSTANCE::toDomain);
    }

    @Override
    public boolean removeById(Long id) {
        int result = baseDao.deleteById(id);
        if (result > 0) {
            log.info("角色删除成功，roleId: {}", id);
        }
        return result > 0;
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        SmartQueryWrapper<RoleDO> wrapper = new SmartQueryWrapper<RoleDO>()
                .eq(RoleDO.ROLE_CODE, roleCode);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public List<Role> findByUserType(Integer userType) {
        List<RoleDO> roleDOList = baseDao.selectList(new SmartQueryWrapper<RoleDO>()
                .eq(RoleDO.USER_TYPE, userType)
                .orderByAsc(RoleDO.SORT_ORDER));
        return CollectionUtil.toList(roleDOList, RoleConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Role> findByRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return CollectionUtil.emptyList();
        }
        SmartQueryWrapper<RoleDO> wrapper = new SmartQueryWrapper<RoleDO>()
                .in(RoleDO.ROLE_CODE, roleCodes);
        List<RoleDO> roleDOList = baseDao.selectList(wrapper);
        return CollectionUtil.toList(roleDOList, RoleConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Role> findByRoleCodesAndUserType(List<String> roleCodes, Integer userType) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return CollectionUtil.emptyList();
        }
        SmartQueryWrapper<RoleDO> wrapper = new SmartQueryWrapper<RoleDO>()
                .in(RoleDO.ROLE_CODE, roleCodes)
                .eq(RoleDO.USER_TYPE, userType);
        List<RoleDO> roleDOList = baseDao.selectList(wrapper);
        return CollectionUtil.toList(roleDOList, RoleConverter.INSTANCE::toDomain);
    }

    @Override
    public Page<Role> findPage(Page<RoleDO> page, Integer userType) {
        SmartQueryWrapper<RoleDO> wrapper = new SmartQueryWrapper<RoleDO>()
                .orderByAsc(RoleDO.SORT_ORDER);
        
        if (userType != null) {
            wrapper.eq(RoleDO.USER_TYPE, userType);
        }
        
        // 使用 MyBatis-Plus 的分页查询
        Page<RoleDO> roleDOPage = baseDao.selectPage(page, wrapper);
        
        // 转换为领域对象分页
        Page<Role> rolePage = new Page<>(roleDOPage.getCurrent(), roleDOPage.getSize(), roleDOPage.getTotal());
        List<Role> roles = CollectionUtil.toList(roleDOPage.getRecords(), RoleConverter.INSTANCE::toDomain);
        rolePage.setRecords(roles);
        
        return rolePage;
    }

    @Override
    public boolean isUsedByUsers(Long roleId) {
        // 通过Feign调用User服务检查是否有用户关联此角色
        try {
            int count = userServiceAdapter.countUsersByRoleId(roleId);
            return count > 0;
        } catch (Exception e) {
            log.error("检查角色是否被用户使用失败，roleId: {}", roleId, e);
            // 异常情况下保守处理，认为被使用，防止误删
            return true;
        }
    }
}
