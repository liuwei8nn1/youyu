package com.youyu.auth.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.youyu.auth.domain.aggregate.UserDevice;
import com.youyu.auth.domain.repository.UserDeviceRepository;
import com.youyu.auth.infrastructure.persistence.converter.UserDeviceConverter;
import com.youyu.auth.infrastructure.persistence.entity.UserDeviceDO;
import com.youyu.auth.infrastructure.persistence.mapper.UserDeviceMapper;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户设备仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserDeviceRepositoryImpl implements UserDeviceRepository {

    private final UserDeviceMapper userDeviceMapper;

    @Override
    public void save(UserDevice device) {
        UserDeviceDO entity = UserDeviceConverter.toEntity(device);
        if (entity.getId() == null) {
            userDeviceMapper.insert(entity);
            device.setId(entity.getId());
        } else {
            userDeviceMapper.updateById(entity);
        }
    }

    @Override
    public Optional<UserDevice> findByIdentityIdAndDeviceUniqueId(Long identityId, String deviceUniqueId) {
        SmartQueryWrapper<UserDeviceDO> wrapper = new SmartQueryWrapper<UserDeviceDO>()
                .eq(UserDeviceDO.IDENTITY_ID, identityId)
                .eq(UserDeviceDO.DEVICE_UNIQUE_ID, deviceUniqueId);
        UserDeviceDO entity = userDeviceMapper.selectOne(wrapper);
        return Optional.ofNullable(UserDeviceConverter.toDomain(entity));
    }

    @Override
    public Optional<UserDevice> findById(Long id) {
        UserDeviceDO entity = userDeviceMapper.selectById(id);
        return Optional.ofNullable(UserDeviceConverter.toDomain(entity));
    }

    @Override
    public List<UserDevice> findByIdentityIdOrderByLoginTimeDesc(Long identityId) {
        LambdaQueryWrapper<UserDeviceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDeviceDO::getIdentityId, identityId)
                .orderByDesc(UserDeviceDO::getLoginTime);
        List<UserDeviceDO> entities = userDeviceMapper.selectList(wrapper);
        return entities.stream()
                .map(UserDeviceConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDevice> findOnlineByIdentityId(Long identityId,Long excludeDeviceId) {
        SmartQueryWrapper<UserDeviceDO> wrapper = new SmartQueryWrapper<UserDeviceDO>()
                .eq(UserDeviceDO.IDENTITY_ID, identityId)
                .ne(UserDeviceDO.ID, excludeDeviceId)
                .eq(UserDeviceDO.STATUS, 1)
                .orderByDesc(UserDeviceDO.LOGIN_TIME);
        List<UserDeviceDO> entities = userDeviceMapper.selectList(wrapper);
        return CollectionUtil.toList(entities, UserDeviceConverter::toDomain);
    }

    @Override
    public void update(UserDevice device) {
        UserDeviceDO entity = UserDeviceConverter.toEntity(device);
        userDeviceMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        userDeviceMapper.deleteById(id);
    }

    @Override
    public void markOffline(Long identityId, Integer userType, Long deviceId) {
        var updateWrapper = new UpdateWrapper<UserDeviceDO>()
                .set(UserDeviceDO.STATUS,0)
                .set(UserDeviceDO.UPDATED_AT, LocalDateTime.now())
                .eq(UserDeviceDO.IDENTITY_ID,identityId)
                .eq(UserDeviceDO.USER_TYPE,userType)
                .eq(UserDeviceDO.ID,deviceId)
                ;
        userDeviceMapper.update(updateWrapper);
    }
}
