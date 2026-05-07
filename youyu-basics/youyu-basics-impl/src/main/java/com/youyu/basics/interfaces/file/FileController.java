package com.youyu.basics.interfaces.file;

import com.youyu.basics.api.file.FileServiceApi;
import com.youyu.basics.api.file.dto.FileUploadRequest;
import com.youyu.basics.api.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务 REST 接口
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController implements FileServiceApi {
    
    // TODO: 注入应用层 Service
    // private final FileApplicationService fileApplicationService;
    
    @Override
    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@RequestBody FileUploadRequest request) {
        // TODO: 实现文件上传逻辑
        return new FileUploadResponse();
    }
    
    @Override
    @DeleteMapping("/{fileId}")
    public void deleteFile(@PathVariable String fileId) {
        // TODO: 实现文件删除逻辑
    }
    
    @Override
    @GetMapping("/{fileId}")
    public FileUploadResponse getFile(@PathVariable String fileId) {
        // TODO: 实现获取文件信息逻辑
        return new FileUploadResponse();
    }
}
