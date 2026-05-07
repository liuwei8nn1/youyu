package com.youyu.auth.infrastructure.persistence.mapper;

import com.youyu.auth.infrastructure.persistence.entity.RoleDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper extends BaseDao<RoleDO> {
}
