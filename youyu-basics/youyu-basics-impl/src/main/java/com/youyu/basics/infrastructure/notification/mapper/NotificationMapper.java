package com.youyu.basics.infrastructure.notification.mapper;

import com.youyu.basics.infrastructure.notification.entity.NotificationDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseDao<NotificationDO> {
}
