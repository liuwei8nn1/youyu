package com.youyu.auth.infrastructure.persistence.mapper;

import com.youyu.auth.infrastructure.persistence.entity.RoleMenuDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联Mapper
 */
@Mapper
public interface RoleMenuMapper extends BaseDao<RoleMenuDO> {
}
