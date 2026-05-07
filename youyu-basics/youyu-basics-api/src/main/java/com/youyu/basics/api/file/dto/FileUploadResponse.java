package com.youyu.basics.api.file.dto;

import lombok.Data;

/**
 * 文件上传响应
 */
@Data
public class FileUploadResponse {
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
}
