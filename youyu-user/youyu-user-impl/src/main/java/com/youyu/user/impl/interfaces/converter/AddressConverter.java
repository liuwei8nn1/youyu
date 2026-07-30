package com.youyu.user.impl.interfaces.converter;

import com.youyu.user.impl.domain.model.Address;
import com.youyu.user.impl.interfaces.vo.AddressVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AddressConverter {

    AddressConverter INSTANCE = Mappers.getMapper(AddressConverter.class);

    AddressVO toVO(Address address);
}
