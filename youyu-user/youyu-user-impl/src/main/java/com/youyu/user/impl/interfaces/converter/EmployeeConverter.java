package com.youyu.user.impl.interfaces.converter;

import com.youyu.user.impl.domain.model.Employee;
import com.youyu.user.impl.interfaces.vo.EmployeeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmployeeConverter {

    EmployeeConverter INSTANCE = Mappers.getMapper(EmployeeConverter.class);

    EmployeeVO toVO(Employee employee);
}
