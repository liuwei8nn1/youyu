package com.youyu.basics.application.file;

import com.youyu.basics.api.file.dto.FileUploadRequest;
import com.youyu.basics.api.file.dto.FileUploadResponse;
import org.springframework.stereotype.Service;

/**
 * 文件应用服务
 */
@Service
public class FileApplicationService {
    
    /**
     * 上传文件
     */
    public FileUploadResponse uploadFile(FileUploadRequest request) {
        // TODO: 调用领域服务处理业务逻辑
        return new FileUploadResponse();
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String fileId) {
        // TODO: 调用领域服务处理业务逻辑
    }
}
