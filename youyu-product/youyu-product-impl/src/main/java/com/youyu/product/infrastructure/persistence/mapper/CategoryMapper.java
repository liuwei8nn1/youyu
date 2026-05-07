package com.youyu.product.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.product.infrastructure.persistence.entity.CategoryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseDao<CategoryDO> {
}