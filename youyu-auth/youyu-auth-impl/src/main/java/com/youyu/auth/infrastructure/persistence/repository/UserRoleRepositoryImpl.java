package com.youyu.auth.infrastructure.persistence.repository;

import com.youyu.auth.domain.model.UserRole;
import com.youyu.auth.domain.repository.UserRoleRepository;
import com.youyu.auth.infrastructure.persistence.converter.UserRoleConverter;
import com.youyu.auth.infrastructure.persistence.entity.UserRoleDO;
import com.youyu.auth.infrastructure.persistence.mapper.UserRoleMapper;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户-角色关联仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleMapper userRoleMapper;

    @Override
    public void save(UserRole userRole) {
        userRole.validate();
        UserRoleDO userRoleDO = UserRoleConverter.INSTANCE.toDO(userRole);
        userRoleMapper.insert(userRoleDO);
    }

    @Override
    public void batchSave(List<UserRole> userRoles) {
        if (CollectionUtils.isEmpty(userRoles)) {
            return;
        }
        
        List<UserRoleDO> userRoleDOs = CollectionUtil.toList(userRoles, UserRoleConverter.INSTANCE::toDO);
        userRoleDOs.forEach(userRoleMapper::insert);
    }

    @Override
    public Optional<UserRole> findById(Long id) {
        UserRoleDO userRoleDO = userRoleMapper.selectById(id);
        return Optional.ofNullable(UserRoleConverter.INSTANCE.toDomain(userRoleDO));
    }

    @Override
    public List<UserRole> findByUserIdentityId(Long userIdentityId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.USER_IDENTITY_ID, userIdentityId);
        List<UserRoleDO> userRoleDOs = userRoleMapper.selectList(wrapper);
        return CollectionUtil.toList(userRoleDOs, UserRoleConverter.INSTANCE::toDomain);
    }

    @Override
    public List<UserRole> findByUserIdAndType(Long userId, Integer userType) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.USER_ID, userId)
                .eq(UserRoleDO.USER_TYPE, userType);
        List<UserRoleDO> userRoleDOs = userRoleMapper.selectList(wrapper);
        return CollectionUtil.toList(userRoleDOs, UserRoleConverter.INSTANCE::toDomain);
    }

    @Override
    public List<UserRole> findByRoleId(Long roleId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.ROLE_ID, roleId);
        List<UserRoleDO> userRoleDOs = userRoleMapper.selectList(wrapper);
        return CollectionUtil.toList(userRoleDOs, UserRoleConverter.INSTANCE::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        userRoleMapper.deleteById(id);
    }

    @Override
    public void deleteByUserIdentityIdAndRoleId(Long userIdentityId, Long roleId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.USER_IDENTITY_ID, userIdentityId)
                .eq(UserRoleDO.ROLE_ID, roleId);
        userRoleMapper.delete(wrapper);
    }

    @Override
    public void deleteByUserIdentityId(Long userIdentityId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.USER_IDENTITY_ID, userIdentityId);
        userRoleMapper.delete(wrapper);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.ROLE_ID, roleId);
        userRoleMapper.delete(wrapper);
    }

    @Override
    public boolean existsByUserIdentityIdAndRoleId(Long userIdentityId, Long roleId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.USER_IDENTITY_ID, userIdentityId)
                .eq(UserRoleDO.ROLE_ID, roleId);
        return userRoleMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int countByRoleId(Long roleId) {
        SmartQueryWrapper<UserRoleDO> wrapper = new SmartQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO.ROLE_ID, roleId);
        return Math.toIntExact(userRoleMapper.selectCount(wrapper));
    }
}
