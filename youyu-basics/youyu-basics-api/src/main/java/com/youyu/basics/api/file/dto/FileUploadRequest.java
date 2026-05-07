package com.youyu.basics.api.file.dto;

import lombok.Data;

/**
 * 文件上传请求
 */
@Data
public class FileUploadRequest {
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型（MIME Type）
     */
    private String contentType;
    
    /**
     * 业务类型（如：avatar、product、order等）
     */
    private String businessType;
}
