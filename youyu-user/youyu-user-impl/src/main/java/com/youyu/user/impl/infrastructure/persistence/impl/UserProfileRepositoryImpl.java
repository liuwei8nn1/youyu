package com.youyu.user.impl.infrastructure.persistence.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youyu.user.impl.domain.model.UserProfile;
import com.youyu.user.impl.domain.repository.UserProfileRepository;
import com.youyu.user.impl.infrastructure.persistence.entity.UserProfileDO;
import com.youyu.user.impl.infrastructure.persistence.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfileMapper userProfileMapper;

    @Override
    public void save(UserProfile userProfile) {
        UserProfileDO po = convertToPO(userProfile);
        userProfileMapper.insert(po);
        userProfile.setId(po.getId());
    }

    @Override
    public Optional<UserProfile> findByIdentityId(Long identityId) {
        LambdaQueryWrapper<UserProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileDO::getIdentityId, identityId);
        UserProfileDO po = userProfileMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::convertToDomain);
    }

    @Override
    public Optional<UserProfile> findByUsername(String username) {
        LambdaQueryWrapper<UserProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileDO::getUsername, username);
        UserProfileDO po = userProfileMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::convertToDomain);
    }

    @Override
    public Optional<UserProfile> findByPhone(String phone) {
        LambdaQueryWrapper<UserProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileDO::getPhone, phone);
        UserProfileDO po = userProfileMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::convertToDomain);
    }

    @Override
    public Optional<UserProfile> findByEmail(String email) {
        LambdaQueryWrapper<UserProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileDO::getEmail, email);
        UserProfileDO po = userProfileMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::convertToDomain);
    }

    @Override
    public void update(UserProfile userProfile) {
        UserProfileDO po = convertToPO(userProfile);
        userProfileMapper.updateById(po);
    }

    private UserProfileDO convertToPO(UserProfile domain) {
        UserProfileDO po = new UserProfileDO();
        po.setId(domain.getId());
        po.setIdentityId(domain.getIdentityId());
        po.setUsername(domain.getUsername());
        po.setNickname(domain.getNickname());
        po.setAvatar(domain.getAvatar());
        po.setEmail(domain.getEmail());
        po.setPhone(domain.getPhone());
        po.setGender(domain.getGender());
        po.setBirthday(domain.getBirthday());
        po.setSignature(domain.getSignature());
        return po;
    }

    private UserProfile convertToDomain(UserProfileDO po) {
        return UserProfile.restore(
                po.getId(),
                po.getIdentityId(),
                po.getUsername(),
                po.getNickname(),
                po.getAvatar(),
                po.getEmail(),
                po.getPhone(),
                po.getGender(),
                po.getBirthday(),
                po.getSignature(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
