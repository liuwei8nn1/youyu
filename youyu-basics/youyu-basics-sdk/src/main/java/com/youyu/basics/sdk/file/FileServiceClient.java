package com.youyu.basics.sdk.file;

import com.youyu.basics.api.file.FileServiceApi;
import com.youyu.basics.api.file.dto.FileUploadRequest;
import com.youyu.basics.api.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务 Feign 客户端
 */
@FeignClient(name = "youyu-basics", path = "/api/v1/files")
@RequiredArgsConstructor
public class FileServiceClient implements FileServiceApi {
    
    @Override
    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@RequestBody FileUploadRequest request) {
        // TODO: Feign 自动实现
        return new FileUploadResponse();
    }
    
    @Override
    @DeleteMapping("/{fileId}")
    public void deleteFile(@PathVariable String fileId) {
        // TODO: Feign 自动实现
    }
    
    @Override
    @GetMapping("/{fileId}")
    public FileUploadResponse getFile(@PathVariable String fileId) {
        // TODO: Feign 自动实现
        return new FileUploadResponse();
    }
}
