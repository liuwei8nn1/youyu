package com.youyu.user.impl.infrastructure.persistence.converter;

import com.youyu.user.impl.domain.entity.Customer;
import com.youyu.user.impl.infrastructure.persistence.entity.CustomerDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Customer转换器
 */
@Mapper
public interface CustomerConverter {
    CustomerConverter INSTANCE = Mappers.getMapper(CustomerConverter.class);

    /**
     * DO转领域模型
     */
    Customer toDomain(CustomerDO customerDO);

    /**
     * 领域模型转DO
     */
    CustomerDO toDO(Customer customer);
}
