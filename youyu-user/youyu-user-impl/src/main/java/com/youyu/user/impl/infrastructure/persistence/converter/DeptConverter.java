package com.youyu.user.impl.infrastructure.persistence.converter;

import com.youyu.user.impl.domain.aggregate.Dept;
import com.youyu.user.impl.infrastructure.persistence.entity.DeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 部门转换器
 */
@Mapper
public interface DeptConverter {
    DeptConverter INSTANCE = Mappers.getMapper(DeptConverter.class);

    /**
     * DO转领域模型
     */
    Dept toDomain(DeptDO deptDO);

    /**
     * 领域模型转DO
     */
    DeptDO toDO(Dept dept);
}
