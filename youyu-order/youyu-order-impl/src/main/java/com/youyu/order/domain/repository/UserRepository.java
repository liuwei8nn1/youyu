package com.youyu.order.domain.repository;

import com.youyu.order.domain.valueobject.ShippingAddress;
import java.util.Optional;

/**
 * 用户仓储接口(领域层)
 * <p>
 * 职责:
 * 1. 抽象用户相关的外部查询
 * 2. 领域层不关心具体实现(HTTP/RPC/MQ)
 * 3. 返回领域对象(ShippingAddress 值对象)
 * <p>
 * DDD 设计说明:
 * - 这是领域层的端口(Port),定义契约
 * - 具体实现在基础设施层(Adapter)
 * - 符合依赖倒置原则(DIP)
 */
public interface UserRepository {
    
    /**
     * 查询用户默认收货地址
     *
     * @param userId 用户ID
     * @return 收货地址值对象
     */
    Optional<ShippingAddress> findDefaultAddress(Long userId);
}
