package com.youyu.basics.domain.file.repository;

import com.youyu.basics.infrastructure.file.entity.FileDO;

/**
 * 文件仓储接口
 */
public interface FileRepository {
    
    /**
     * 保存文件
     */
    boolean save(FileDO file);
    
    /**
     * 根据ID查询文件
     */
    FileDO findById(String fileId);
    
    /**
     * 删除文件
     */
    void delete(String fileId);
}
