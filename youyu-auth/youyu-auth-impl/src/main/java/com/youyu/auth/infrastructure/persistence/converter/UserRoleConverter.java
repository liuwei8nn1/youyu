package com.youyu.auth.infrastructure.persistence.converter;

import com.youyu.auth.domain.model.UserRole;
import com.youyu.auth.infrastructure.persistence.entity.UserRoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户-角色关联转换器
 */
@Mapper
public interface UserRoleConverter {

    UserRoleConverter INSTANCE = Mappers.getMapper(UserRoleConverter.class);

    /**
     * DO 转领域模型
     */
    UserRole toDomain(UserRoleDO userRoleDO);

    /**
     * 领域模型转 DO
     */
    UserRoleDO toDO(UserRole userRole);
}
