package com.youyu.user.impl.interfaces.converter;

import com.youyu.user.impl.domain.model.UserProfile;
import com.youyu.user.impl.interfaces.vo.UserProfileVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserProfileConverter {

    UserProfileConverter INSTANCE = Mappers.getMapper(UserProfileConverter.class);

    UserProfileVO toVO(UserProfile profile);
}
