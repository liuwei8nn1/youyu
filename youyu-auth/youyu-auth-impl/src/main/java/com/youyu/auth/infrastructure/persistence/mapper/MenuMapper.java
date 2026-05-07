package com.youyu.auth.infrastructure.persistence.mapper;

import com.youyu.auth.infrastructure.persistence.entity.MenuDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单Mapper
 */
@Mapper
public interface MenuMapper extends BaseDao<MenuDO> {
}
