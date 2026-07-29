package com.youyu.auth.infrastructure.persistence.converter;

import com.youyu.auth.domain.aggregate.Role;
import com.youyu.auth.infrastructure.persistence.entity.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色转换器
 */
@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    /**
     * DO转领域模型
     */
    default Role toDomain(RoleDO roleDO) {
        if (roleDO == null) {
            return null;
        }
        Role role = new Role();
        role.setId(roleDO.getId());
        role.setRoleCode(roleDO.getRoleCode());
        role.setRoleName(roleDO.getRoleName());
        role.setDescription(roleDO.getDescription());
        role.setUserType(roleDO.getUserType());
        role.setStatus(roleDO.getStatus());
        role.setSortOrder(roleDO.getSortOrder());
        role.setCreatedAt(roleDO.getCreatedAt());
        role.setUpdatedAt(roleDO.getUpdatedAt());
        role.setCreatedBy(roleDO.getCreatedBy());
        role.setUpdatedBy(roleDO.getUpdatedBy());
        return role;
    }

    /**
     * 领域模型转DO
     */
    default RoleDO toDO(Role role) {
        if (role == null) {
            return null;
        }
        RoleDO roleDO = new RoleDO();
        roleDO.setId(role.getId());
        roleDO.setRoleCode(role.getRoleCode());
        roleDO.setRoleName(role.getRoleName());
        roleDO.setDescription(role.getDescription());
        roleDO.setUserType(role.getUserType());
        roleDO.setStatus(role.getStatus());
        roleDO.setSortOrder(role.getSortOrder());
        roleDO.setCreatedAt(role.getCreatedAt());
        roleDO.setUpdatedAt(role.getUpdatedAt());
        roleDO.setCreatedBy(role.getCreatedBy());
        roleDO.setUpdatedBy(role.getUpdatedBy());
        return roleDO;
    }
}
