package com.youyu.order.infrastructure.external.adapter;

import com.youyu.order.domain.model.ShippingAddress;
import com.youyu.order.domain.repository.UserRepository;
import com.youyu.order.infrastructure.external.acl.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现 - HTTP服务版本(基础设施层 - Adapter)
 * <p>
 * 职责:
 * 1. 实现领域层定义的 UserRepository 接口
 * 2. 封装对 UserServiceClient 的调用
 * 3. External Response → Domain Object 转换
 * 4. 异常处理和日志记录
 * <p>
 * 使用场景:
 * - 当其他服务提供 HTTP REST API 但没有 Java SDK 时
 * - 当不使用 Feign Client,直接使用 RestTemplate/OkHttp 等 HTTP 客户端时
 * <p>
 * DDD 设计说明:
 * - 这是基础设施层的适配器(Adapter)
 * - 将外部服务的响应适配为领域层需要的 ShippingAddress
 * - 符合六边形架构(Hexagonal Architecture)
 * - 隔离外部系统的变化,保护领域层不受影响
 */
@Slf4j
// @Repository // 不注入，该只是一个实例
@RequiredArgsConstructor
public class HttpUserRepositoryImpl implements UserRepository {

    private final UserServiceClient userServiceClient;

    @Override
    public Optional<ShippingAddress> findDefaultAddress(Long userId) {
        log.info("通过 HTTP 服务查询用户默认收货地址，userId: {}", userId);
        
        try {
            // 1. 调用 HTTP 客户端(ACL层)
            UserServiceClient.AddressResponse response =
                userServiceClient.getDefaultAddress(userId);
            
            // 2. 检查结果
            if (response == null) {
                log.warn("HTTP 服务未返回用户默认收货地址，userId: {}", userId);
                return Optional.empty();
            }
            
            // 3. 转换为领域层的值对象
            ShippingAddress shippingAddress = ShippingAddress.create(
                response.getReceiverName(),
                response.getReceiverPhone(),
                response.getProvince(),
                response.getCity(),
                response.getDistrict(),
                response.getDetailAddress(),
                response.getZipCode()
            );
            
            log.info("通过 HTTP 服务查询用户默认收货地址成功，userId: {}, receiverName: {}", 
                userId, response.getReceiverName());
            
            return Optional.of(shippingAddress);
            
        } catch (Exception e) {
            log.error("通过 HTTP 服务查询用户默认收货地址失败，userId: {}", userId, e);
            return Optional.empty();
        }
    }
}
