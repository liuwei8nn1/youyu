package com.youyu.user.impl.infrastructure.persistence.converter;

import com.youyu.user.impl.domain.model.Employee;
import com.youyu.user.impl.infrastructure.persistence.entity.EmployeeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Employee转换器
 */
@Mapper
public interface EmployeeConverter {
    EmployeeConverter INSTANCE = Mappers.getMapper(EmployeeConverter.class);

    /**
     * DO转领域模型
     */
    Employee toDomain(EmployeeDO employeeDO);

    /**
     * 领域模型转DO
     */
    EmployeeDO toDO(Employee employee);
}
