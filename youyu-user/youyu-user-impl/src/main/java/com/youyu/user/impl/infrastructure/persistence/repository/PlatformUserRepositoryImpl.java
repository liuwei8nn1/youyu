package com.youyu.user.impl.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.user.impl.domain.model.PlatformUser;
import com.youyu.user.impl.domain.repository.PlatformUserRepository;
import com.youyu.user.impl.infrastructure.persistence.converter.PlatformUserConverter;
import com.youyu.user.impl.infrastructure.persistence.entity.PlatformUserDO;
import com.youyu.user.impl.infrastructure.persistence.mapper.PlatformUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 平台管理员资料仓储实现
 */
@Slf4j
@Repository
public class PlatformUserRepositoryImpl extends BaseRepositoryImpl<PlatformUserDO, PlatformUserMapper, Long> implements PlatformUserRepository {

    @Override
    public Long save(PlatformUser platformUser) {
        PlatformUserDO platformUserDO = PlatformUserConverter.INSTANCE.toDO(platformUser);
        baseDao.insert(platformUserDO);
        log.info("平台管理员资料保存成功，identityId: {}", platformUserDO.getIdentityId());
        return platformUserDO.getId();
    }

    @Override
    public Optional<PlatformUser> findByIdentityId(Long identityId) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.IDENTITY_ID, identityId);
        PlatformUserDO platformUserDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(PlatformUserConverter.INSTANCE.toDomain(platformUserDO));
    }

    @Override
    public Optional<PlatformUser> findByUsername(String username) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.USERNAME, username);
        PlatformUserDO platformUserDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(PlatformUserConverter.INSTANCE.toDomain(platformUserDO));
    }

    @Override
    public Optional<PlatformUser> findByPhone(String phone) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.PHONE, phone);
        PlatformUserDO platformUserDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(PlatformUserConverter.INSTANCE.toDomain(platformUserDO));
    }

    @Override
    public Optional<PlatformUser> findByEmail(String email) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.EMAIL, email);
        PlatformUserDO platformUserDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(PlatformUserConverter.INSTANCE.toDomain(platformUserDO));
    }

    @Override
    public boolean existsByUsername(String username) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.USERNAME, username);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.PHONE, phone);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        SmartQueryWrapper<PlatformUserDO> wrapper = new SmartQueryWrapper<PlatformUserDO>()
                .eq(PlatformUserDO.EMAIL, email);
        return baseDao.selectCount(wrapper) > 0;
    }
}
