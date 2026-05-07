package com.youyu.user.impl.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.user.impl.infrastructure.persistence.entity.CustomerDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 外部顾客Mapper
 */
@Mapper
public interface CustomerMapper extends BaseDao<CustomerDO> {
}
