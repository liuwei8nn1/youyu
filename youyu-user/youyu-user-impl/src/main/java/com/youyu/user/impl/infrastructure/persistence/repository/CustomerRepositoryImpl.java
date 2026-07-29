package com.youyu.user.impl.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.user.impl.domain.entity.Customer;
import com.youyu.user.impl.domain.repository.CustomerRepository;
import com.youyu.user.impl.infrastructure.persistence.converter.CustomerConverter;
import com.youyu.user.impl.infrastructure.persistence.entity.CustomerDO;
import com.youyu.user.impl.infrastructure.persistence.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 外部顾客仓储实现
 */
@Slf4j
@Repository
public class CustomerRepositoryImpl extends BaseRepositoryImpl<CustomerDO, CustomerMapper, Long> implements CustomerRepository {

    @Override
    public Long save(Customer customer) {
        CustomerDO customerDO = CustomerConverter.INSTANCE.toDO(customer);
        baseDao.insert(customerDO);
        log.info("外部顾客保存成功，identityId: {}", customerDO.getIdentityId());
        return customerDO.getId();
    }

    @Override
    public Optional<Customer> findByIdentityId(Long identityId) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.IDENTITY_ID, identityId);
        CustomerDO customerDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(CustomerConverter.INSTANCE.toDomain(customerDO));
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.USERNAME, username);
        CustomerDO customerDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(CustomerConverter.INSTANCE.toDomain(customerDO));
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.PHONE, phone);
        CustomerDO customerDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(CustomerConverter.INSTANCE.toDomain(customerDO));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.EMAIL, email);
        CustomerDO customerDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(CustomerConverter.INSTANCE.toDomain(customerDO));
    }

    @Override
    public boolean existsByUsername(String username) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.USERNAME, username);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.PHONE, phone);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        SmartQueryWrapper<CustomerDO> wrapper = new SmartQueryWrapper<CustomerDO>()
                .eq(CustomerDO.EMAIL, email);
        return baseDao.selectCount(wrapper) > 0;
    }
}
