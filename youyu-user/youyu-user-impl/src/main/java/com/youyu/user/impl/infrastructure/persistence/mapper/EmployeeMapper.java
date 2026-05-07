package com.youyu.user.impl.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.user.impl.infrastructure.persistence.entity.EmployeeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业员工资料Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseDao<EmployeeDO> {
}
