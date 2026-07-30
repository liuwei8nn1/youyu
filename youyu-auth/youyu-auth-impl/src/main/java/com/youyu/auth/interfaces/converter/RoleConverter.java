package com.youyu.auth.interfaces.converter;

import com.youyu.auth.domain.model.Role;
import com.youyu.auth.interfaces.vo.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    RoleVO toVO(Role role);
}
