package com.youyu.user.impl.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.user.impl.infrastructure.persistence.entity.PlatformUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台管理员资料Mapper
 */
@Mapper
public interface PlatformUserMapper extends BaseDao<PlatformUserDO> {
}
