package com.youyu.user.impl.infrastructure.persistence.converter;

import com.youyu.user.impl.domain.entity.PlatformUser;
import com.youyu.user.impl.infrastructure.persistence.entity.PlatformUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * PlatformUser转换器
 */
@Mapper
public interface PlatformUserConverter {
    PlatformUserConverter INSTANCE = Mappers.getMapper(PlatformUserConverter.class);

    /**
     * DO转领域模型
     */
    PlatformUser toDomain(PlatformUserDO platformUserDO);

    /**
     * 领域模型转DO
     */
    PlatformUserDO toDO(PlatformUser platformUser);
}
