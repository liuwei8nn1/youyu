package com.youyu.basics.infrastructure.file.mapper;

import com.youyu.basics.infrastructure.file.entity.FileDO;
import com.youyu.framework.datasource.mybatis.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件 Mapper
 */
@Mapper
public interface FileMapper extends BaseDao<FileDO> {
}
