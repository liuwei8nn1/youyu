package com.youyu.basics.infrastructure.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文件数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file")
public class FileDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 存储路径
     */
    private String storagePath;

    /**
     * 访问URL
     */
    private String accessUrl;
}
