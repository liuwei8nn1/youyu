package com.youyu.user.impl.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.user.impl.infrastructure.persistence.entity.UserProfileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseDao<UserProfileDO> {
}
