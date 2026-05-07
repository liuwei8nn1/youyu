package com.youyu.auth.infrastructure.persistence.mapper;

import com.youyu.auth.infrastructure.persistence.entity.UserRoleDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseDao<UserRoleDO> {
}
