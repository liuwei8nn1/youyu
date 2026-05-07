package com.youyu.auth.infrastructure.persistence.mapper;

import com.youyu.auth.infrastructure.persistence.entity.UserDeviceDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户设备 Mapper
 */
@Mapper
public interface UserDeviceMapper extends BaseDao<UserDeviceDO> {
}
