package com.youyu.user.impl.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.user.impl.infrastructure.persistence.entity.DeptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper
 */
@Mapper
public interface DeptMapper extends BaseDao<DeptDO> {
}
