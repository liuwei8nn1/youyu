package com.youyu.user.impl.domain.repository;

import com.youyu.user.impl.domain.model.Address;

import java.util.List;
import java.util.Optional;

/**
 * 收货地址仓储接口
 */
public interface AddressRepository {

    /**
     * 保存地址
     */
    void save(Address address);

    /**
     * 根据ID查询
     */
    Optional<Address> findById(Long id);

    /**
     * 根据用户ID查询所有地址
     */
    List<Address> findByUserId(Long userId);

    /**
     * 更新地址
     */
    void update(Address address);

    /**
     * 删除地址
     */
    void delete(Long id);

    /**
     * 取消用户的所有默认地址
     */
    void cancelAllDefaultByUserId(Long userId);

    /**
     * 查询用户的默认收货地址
     *
     * @param userId 用户ID
     * @return 默认地址，如果不存在则返回 Optional.empty()
     */
    Optional<Address> findDefaultByUserId(Long userId);
}
