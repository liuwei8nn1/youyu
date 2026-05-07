package com.youyu.basics.api.file;

import com.youyu.basics.api.file.dto.FileUploadRequest;
import com.youyu.basics.api.file.dto.FileUploadResponse;

/**
 * 文件服务接口
 */
public interface FileServiceApi {
    
    /**
     * 上传文件
     *
     * @param request 上传请求
     * @return 上传响应
     */
    FileUploadResponse uploadFile(FileUploadRequest request);
    
    /**
     * 删除文件
     *
     * @param fileId 文件ID
     */
    void deleteFile(String fileId);
    
    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileUploadResponse getFile(String fileId);
}
