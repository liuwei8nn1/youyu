package com.youyu.user.impl.infrastructure.persistence.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.youyu.user.impl.domain.model.Address;
import com.youyu.user.impl.domain.repository.AddressRepository;
import com.youyu.user.impl.infrastructure.persistence.entity.AddressDO;
import com.youyu.user.impl.infrastructure.persistence.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressMapper addressMapper;

    @Override
    public void save(Address address) {
        AddressDO po = convertToPO(address);
        addressMapper.insert(po);
        address.setId(po.getId());
    }

    @Override
    public Optional<Address> findById(Long id) {
        AddressDO po = addressMapper.selectById(id);
        return Optional.ofNullable(po).map(this::convertToDomain);
    }

    @Override
    public List<Address> findByUserId(Long userId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
               .orderByDesc(AddressDO::getIsDefault)
               .orderByDesc(AddressDO::getCreatedAt);
        List<AddressDO> poList = addressMapper.selectList(wrapper);
        return poList.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void update(Address address) {
        AddressDO po = convertToPO(address);
        addressMapper.updateById(po);
    }

    @Override
    public void delete(Long id) {
        addressMapper.deleteById(id);
    }

    @Override
    public void cancelAllDefaultByUserId(Long userId) {
        LambdaUpdateWrapper<AddressDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
               .set(AddressDO::getIsDefault, 0);
        addressMapper.update(null, wrapper);
    }

    @Override
    public Optional<Address> findDefaultByUserId(Long userId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
               .eq(AddressDO::getIsDefault, 1)
               .last("LIMIT 1");
        AddressDO po = addressMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::convertToDomain);
    }

    private AddressDO convertToPO(Address domain) {
        AddressDO po = new AddressDO();
        po.setId(domain.getId());
        po.setUserId(domain.getUserId());
        po.setReceiverName(domain.getReceiverName());
        po.setReceiverPhone(domain.getReceiverPhone());
        po.setProvince(domain.getProvince());
        po.setCity(domain.getCity());
        po.setDistrict(domain.getDistrict());
        po.setDetailAddress(domain.getDetailAddress());
        po.setZipCode(domain.getZipCode());
        po.setIsDefault(domain.getIsDefault());
        po.setLabel(domain.getLabel());
        return po;
    }

    private Address convertToDomain(AddressDO po) {
        return Address.restore(
                po.getId(),
                po.getUserId(),
                po.getReceiverName(),
                po.getReceiverPhone(),
                po.getProvince(),
                po.getCity(),
                po.getDistrict(),
                po.getDetailAddress(),
                po.getZipCode(),
                po.getIsDefault(),
                po.getLabel()
        );
    }
}
