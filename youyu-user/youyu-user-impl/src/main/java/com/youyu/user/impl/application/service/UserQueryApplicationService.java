package com.youyu.user.impl.application.service;

import com.youyu.user.api.dto.AddressDTO;
import com.youyu.user.api.dto.UserLoginInfo;
import com.youyu.user.impl.domain.model.Address;
import com.youyu.user.impl.domain.model.UserProfile;
import com.youyu.user.impl.domain.repository.AddressRepository;
import com.youyu.user.impl.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户查询应用服务
 * 供 auth-service 调用，用于登录时查询用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryApplicationService {

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;

    /**
     * 根据用户名查询用户登录信息
     */
    public Optional<UserLoginInfo> findByUsername(String username) {
        log.debug("根据用户名查询用户: {}", username);
        return userProfileRepository.findByUsername(username)
                .map(this::convertToLoginInfo);
    }

    /**
     * 根据手机号查询用户登录信息
     */
    public Optional<UserLoginInfo> findByPhone(String phone) {
        log.debug("根据手机号查询用户: {}", phone);
        return userProfileRepository.findByPhone(phone)
                .map(this::convertToLoginInfo);
    }

    /**
     * 根据邮箱查询用户登录信息
     */
    public Optional<UserLoginInfo> findByEmail(String email) {
        log.debug("根据邮箱查询用户: {}", email);
        return userProfileRepository.findByEmail(email)
                .map(this::convertToLoginInfo);
    }

    /**
     * 查询用户的默认收货地址
     *
     * @param userId 用户ID
     * @return 默认地址 DTO，如果不存在则返回 Optional.empty()
     */
    public Optional<AddressDTO> findDefaultAddress(Long userId) {
        log.debug("查询用户默认收货地址: {}", userId);
        return addressRepository.findDefaultByUserId(userId)
                .map(this::convertToAddressDTO);
    }

    /**
     * 转换为登录信息 DTO
     */
    private UserLoginInfo convertToLoginInfo(UserProfile profile) {
        return UserLoginInfo.builder()
                .userId(profile.getIdentityId())  // 返回 identityId（user_identity.id）
                .username(profile.getUsername())
                .phone(profile.getPhone())
                .email(profile.getEmail())
                .status(1) // 默认正常状态，实际应该从 auth-service 获取
                .build();
    }

    /**
     * 转换为地址 DTO
     */
    private AddressDTO convertToAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(address.getId());
        dto.setUserId(address.getUserId());
        dto.setReceiverName(address.getReceiverName());
        dto.setReceiverPhone(address.getReceiverPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetailAddress(address.getDetailAddress());
        dto.setZipCode(address.getZipCode());
        dto.setIsDefault(address.getIsDefault() == 1);
        return dto;
    }
}
