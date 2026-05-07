package com.youyu.user.impl.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.user.impl.infrastructure.persistence.entity.AddressDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseDao<AddressDO> {
}
