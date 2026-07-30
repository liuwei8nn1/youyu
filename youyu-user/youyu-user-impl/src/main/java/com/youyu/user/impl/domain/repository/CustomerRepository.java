package com.youyu.user.impl.domain.repository;


import java.util.Optional;

import com.youyu.user.impl.domain.model.Customer;

/**
 * 外部顾客仓储接口
 */
public interface CustomerRepository {
    /**
     * 保存外部顾客
     */
    Long save(Customer customer);

    /**
     * 根据identityId查询
     */
    Optional<Customer> findByIdentityId(Long identityId);

    /**
     * 根据用户名查询
     */
    Optional<Customer> findByUsername(String username);

    /**
     * 根据手机号查询
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * 根据邮箱查询
     */
    Optional<Customer> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
