package com.youyu.auth.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youyu.auth.domain.model.UserIdentity;
import com.youyu.auth.domain.repository.UserIdentityRepository;
import com.youyu.auth.infrastructure.persistence.converter.UserIdentityConverter;
import com.youyu.auth.infrastructure.persistence.entity.UserIdentityDO;
import com.youyu.auth.infrastructure.persistence.mapper.UserIdentityMapper;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户身份仓储实现
 */
@Repository
public class UserIdentityRepositoryImpl extends BaseRepositoryImpl<UserIdentityDO, UserIdentityMapper, Long> implements UserIdentityRepository {

    private final UserIdentityConverter converter = UserIdentityConverter.getInstance();

    @Override
    public void save(UserIdentity userIdentity) {
        UserIdentityDO userIdentityDO = converter.toDO(userIdentity);
        baseDao.insert(userIdentityDO);
        // 回填ID
        userIdentity.setId(userIdentityDO.getId());
    }

    @Override
    public void update(UserIdentity userIdentity) {
        UserIdentityDO userIdentityDO = converter.toDO(userIdentity);
        baseDao.updateById(userIdentityDO);
    }

    @Override
    public Optional<UserIdentity> findById(Long id) {
        UserIdentityDO userIdentityDO = baseDao.selectById(id);
        return Optional.ofNullable(userIdentityDO)
                .map(converter::toDomain);
    }

    @Override
    public Optional<UserIdentity> findByUserIdAndType(Long id, Integer userType) {
        SmartQueryWrapper<UserIdentityDO> queryWrapper = new SmartQueryWrapper<UserIdentityDO>()
                .eq(UserIdentity.USER_ID, id)
                .eq(UserIdentity.USER_TYPE, userType);
        UserIdentityDO userIdentityDO = baseDao.selectOne(queryWrapper);
        return Optional.ofNullable(userIdentityDO)
                .map(converter::toDomain);
    }

    @Override
    public void delete(Long id) {
        baseDao.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username, Integer userType) {
        SmartQueryWrapper<UserIdentityDO> queryWrapper = new SmartQueryWrapper<UserIdentityDO>()
                .eq("username", username)
                .eq("user_type", userType);
        return baseDao.selectCount(queryWrapper) > 0;
    }

    @Override
    public Optional<UserIdentity> findByUsernameAndType(String username, Integer userType) {
        SmartQueryWrapper<UserIdentityDO> queryWrapper = new SmartQueryWrapper<UserIdentityDO>()
                .eq("username", username)
                .eq("user_type", userType);
        UserIdentityDO userIdentityDO = baseDao.selectOne(queryWrapper);
        return Optional.ofNullable(userIdentityDO)
                .map(converter::toDomain);
    }
}
