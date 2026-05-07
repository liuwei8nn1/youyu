package com.youyu.auth.infrastructure.persistence.mapper;

import com.youyu.auth.infrastructure.persistence.entity.UserIdentityDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户身份Mapper
 */
@Mapper
public interface UserIdentityMapper extends BaseDao<UserIdentityDO> {
}
