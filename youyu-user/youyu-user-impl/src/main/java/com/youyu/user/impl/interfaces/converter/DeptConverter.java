package com.youyu.user.impl.interfaces.converter;

import com.youyu.user.impl.domain.model.Dept;
import com.youyu.user.impl.interfaces.vo.DeptVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeptConverter {

    DeptConverter INSTANCE = Mappers.getMapper(DeptConverter.class);

    @Mapping(target = "children", ignore = true)
    DeptVO toVO(Dept dept);

    List<DeptVO> toVOList(List<Dept> depts);
}
