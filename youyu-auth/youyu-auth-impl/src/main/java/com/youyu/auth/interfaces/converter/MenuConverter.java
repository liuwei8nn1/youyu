package com.youyu.auth.interfaces.converter;

import com.youyu.auth.domain.model.Menu;
import com.youyu.auth.interfaces.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MenuConverter {

    MenuConverter INSTANCE = Mappers.getMapper(MenuConverter.class);

    @Named("toVO")
    MenuVO toVO(Menu menu);

    @Named("toVOFlat")
    @Mapping(target = "children", ignore = true)
    MenuVO toVOFlat(Menu menu);
}
