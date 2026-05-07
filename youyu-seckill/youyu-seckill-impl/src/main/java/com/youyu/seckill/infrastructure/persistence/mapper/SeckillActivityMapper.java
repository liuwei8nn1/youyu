package com.youyu.seckill.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.seckill.infrastructure.persistence.entity.SeckillActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillActivityMapper extends BaseDao<SeckillActivityDO> {
}
