package com.youyu.order.infrastructure.external.acl;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 外部用户服务客户端(防腐层 - ACL)
 * <p>
 * 职责:
 * 1. 封装对外部微服务的 HTTP 调用
 * 2. 处理协议转换(JSON → Java Object)
 * 3. 隔离外部系统的变化
 * 4. 统一异常处理
 * <p>
 * 使用场景:
 * - 当对方服务是 Go/Python 等其他语言,没有提供 Java SDK 时
 * - 当对方服务没有提供 Feign Client 时
 * - 需要适配不同的 API 风格(REST/gRPC/GraphQL)
 * <p>
 * DDD 设计说明:
 * - 这是 Anti-Corruption Layer (ACL,防腐层)
 * - 防止外部系统的"腐败"影响到领域层
 * - 将外部 API 转换为内部熟悉的接口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;
    
    /**
     * 外部用户服务的基础 URL
     * 实际项目中应该从配置文件读取
     */
    private static final String BASE_URL = "http://go-user-service:8080";

    /**
     * 外部服务返回的地址响应结构
     * <p>
     * 注意:
     * - 这个类只用于接收外部服务的响应
     * - 不应该暴露给领域层
     * - 应该在 Adapter 层转换为领域对象
     */
    @Data
    public static class AddressResponse {
        private Long addressId;
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private String zipCode;
        private Boolean isDefault;
    }

    /**
     * 查询用户默认地址
     * <p>
     * 调用外部服务的 API: GET /api/users/{userId}/default-address
     * <p>
     * 示例说明:
     * - 当对方服务提供 HTTP REST API 但没有 Java SDK 时使用
     * - 使用 RestTemplate 进行 HTTP 调用
     * - 处理 JSON 序列化和反序列化
     * - 统一异常处理和日志记录
     *
     * @param userId 用户ID
     * @return 地址响应,如果失败返回 null
     */
    public AddressResponse getDefaultAddress(Long userId) {
        String url = BASE_URL + "/api/users/" + userId + "/default-address";
        
        log.info("调用用户服务查询默认地址，userId: {}, url: {}", userId, url);
        
        try {
            // 1. 发起 HTTP GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            // 2. 检查 HTTP 响应状态码
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("调用用户服务失败，status: {}, userId: {}", 
                    response.getStatusCode(), userId);
                return null;
            }
            
            // 3. 获取响应体
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                log.warn("用户服务返回空响应，userId: {}", userId);
                return null;
            }
            
            // 4. 解析 JSON 响应为 Java 对象
            AddressResponse addressResponse = JSON.parseObject(body, AddressResponse.class);
            
            log.info("调用用户服务成功，userId: {}, receiverName: {}", 
                userId, addressResponse != null ? addressResponse.getReceiverName() : "null");
            
            return addressResponse;
            
        } catch (Exception e) {
            log.error("调用用户服务异常，userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 根据地址ID查询地址详情
     * <p>
     * 调用外部服务的 API: GET /api/addresses/{addressId}
     *
     * @param addressId 地址ID
     * @return 地址响应,如果失败返回 null
     */
    public AddressResponse getAddressById(Long addressId) {
        String url = BASE_URL + "/api/addresses/" + addressId;
        
        log.info("调用用户服务查询地址详情，addressId: {}, url: {}", addressId, url);
        
        try {
            // 1. 发起 HTTP GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            // 2. 检查 HTTP 响应状态码
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("调用用户服务失败，status: {}, addressId: {}", 
                    response.getStatusCode(), addressId);
                return null;
            }
            
            // 3. 获取响应体
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                log.warn("用户服务返回空响应，addressId: {}", addressId);
                return null;
            }
            
            // 4. 解析 JSON 响应为 Java 对象
            AddressResponse addressResponse = JSON.parseObject(body, AddressResponse.class);
            
            log.info("调用用户服务成功，addressId: {}, receiverName: {}", 
                addressId, addressResponse != null ? addressResponse.getReceiverName() : "null");
            
            return addressResponse;
            
        } catch (Exception e) {
            log.error("调用用户服务异常，addressId: {}", addressId, e);
            return null;
        }
    }

    /**
     * 查询用户的所有收货地址列表
     * <p>
     * 调用外部服务的 API: GET /api/users/{userId}/addresses
     *
     * @param userId 用户ID
     * @return 地址响应列表,如果失败返回 null
     */
    public java.util.List<AddressResponse> getUserAddresses(Long userId) {
        String url = BASE_URL + "/api/users/" + userId + "/addresses";
        
        log.info("调用用户服务查询用户地址列表，userId: {}, url: {}", userId, url);
        
        try {
            // 1. 发起 HTTP GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            // 2. 检查 HTTP 响应状态码
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("调用用户服务失败，status: {}, userId: {}", 
                    response.getStatusCode(), userId);
                return null;
            }
            
            // 3. 获取响应体
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                log.warn("用户服务返回空响应，userId: {}", userId);
                return null;
            }
            
            // 4. 解析 JSON 数组响应为 Java 对象列表
            java.util.List<AddressResponse> addressList = 
                JSON.parseArray(body, AddressResponse.class);
            
            log.info("调用用户服务成功，userId: {}, 地址数量: {}", 
                userId, addressList != null ? addressList.size() : 0);
            
            return addressList;
            
        } catch (Exception e) {
            log.error("调用用户服务异常，userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 查询用户信息
     * <p>
     * 调用外部服务的 API: GET /api/users/{userId}
     *
     * @param userId 用户ID
     * @return 用户信息 JSON 字符串
     */
    public String getUserInfo(Long userId) {
        String url = BASE_URL + "/api/users/" + userId;
        
        log.info("调用外部用户服务查询用户信息，userId: {}", userId);
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            
            log.warn("调用外部用户服务失败，status: {}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("调用外部用户服务异常，userId: {}", userId, e);
            return null;
        }
    }
}
