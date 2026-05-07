package com.youyu.user.impl.application.service;

import com.youyu.framework.context.UserType;
import com.youyu.user.api.dto.UserProfileCreateRequest;
import com.youyu.user.impl.domain.model.*;
import com.youyu.user.impl.domain.repository.AddressRepository;
import com.youyu.user.impl.domain.repository.CustomerRepository;
import com.youyu.user.impl.domain.repository.EmployeeRepository;
import com.youyu.user.impl.domain.repository.PlatformUserRepository;
import com.youyu.user.impl.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileApplicationService {

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PlatformUserRepository platformUserRepository;

    /**
     * 注册外部顾客（同时创建customer和user_profile）
     *
     * @param request 注册请求
     * @return 用户ID
     */
    @Transactional
    public Long registerCustomer(UserProfileCreateRequest request) {
        // 1. 创建customer记录
        Customer customer = Customer.create(
                request.getUserId(),
                request.getUsername(),
                request.getPhone(),
                request.getEmail()
        );
        customerRepository.save(customer);

        // 2. 创建user_profile记录
        UserProfile profile = UserProfile.create(
                request.getUserId(),
                request.getUsername(),
                request.getNickname(),
                request.getPhone()
        );
        profile.updateProfile(
                request.getNickname(),
                null, // avatar
                request.getEmail(),
                request.getPhone(),
                0, // gender
                null, // birthday
                null  // signature
        );
        userProfileRepository.save(profile);

        return request.getUserId();
    }

    /**
     * 检查用户名是否存在（根据用户类型查询对应的表）
     */
    public boolean existsByUsername(String username, Integer userType) {
        UserType type = UserType.of(userType);
        return switch (type) {
            case CUSTOMER -> customerRepository.existsByUsername(username);
            case ENTERPRISE -> employeeRepository.existsByUsername(username);
            case PLATFORM -> platformUserRepository.existsByUsername(username);
            default -> false;
        };
    }

    /**
     * 检查手机号是否存在（根据用户类型查询对应的表）
     */
    public boolean existsByPhone(String phone, Integer userType) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        UserType type = UserType.of(userType);
        return switch (type) {
            case UNKNOWN -> throw new IllegalArgumentException("未知用户类型");
            case CUSTOMER -> customerRepository.existsByPhone(phone);
            case ENTERPRISE -> employeeRepository.existsByPhone(phone);
            case PLATFORM -> platformUserRepository.existsByPhone(phone);
        };
    }

    /**
     * 检查邮箱是否存在（根据用户类型查询对应的表）
     */
    public boolean existsByEmail(String email, Integer userType) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        UserType type = UserType.of(userType);
        return switch (type) {
	        case UNKNOWN -> throw new IllegalArgumentException("未知用户类型");
	        case CUSTOMER -> customerRepository.existsByEmail(email);
            case ENTERPRISE -> employeeRepository.existsByEmail(email);
            case PLATFORM -> platformUserRepository.existsByEmail(email);
        };
    }

    /**
     * 获取用户资料
     */
    public Optional<UserProfile> getUserProfile(Long identityId) {
        return userProfileRepository.findByIdentityId(identityId);
    }

    /**
     * 创建或更新用户资料
     */
    @Transactional
    public void saveOrUpdateUserProfile(Long identityId, String nickname, String avatar, 
                                       String email, String phone, Integer gender,
                                       LocalDateTime birthday, String signature) {
        Optional<UserProfile> existing = userProfileRepository.findByIdentityId(identityId);
        
        if (existing.isPresent()) {
            UserProfile profile = existing.get();
            profile.updateProfile(nickname, avatar, email, phone, gender, birthday, signature);
            userProfileRepository.update(profile);
        } else {
            // TODO: username 应该从 auth-service 获取，这里暂时使用 identityId
            String username = "user" + identityId;
            UserProfile profile = UserProfile.create(identityId, username, nickname, phone);
            profile.updateProfile(nickname, avatar, email, phone, gender, birthday, signature);
            userProfileRepository.save(profile);
        }
    }

    /**
     * 获取用户地址列表
     */
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    /**
     * 添加收货地址
     */
    @Transactional
    public Long addAddress(Long userId, String receiverName, String receiverPhone,
                          String province, String city, String district,
                          String detailAddress, String zipCode, Integer isDefault, String label) {
        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault != null && isDefault == 1) {
            addressRepository.cancelAllDefaultByUserId(userId);
        }

        Address address = Address.create(userId, receiverName, receiverPhone,
                province, city, district, detailAddress, isDefault, label);
        addressRepository.save(address);
        return address.getId();
    }

    /**
     * 更新收货地址
     */
    @Transactional
    public void updateAddress(Long addressId, Long userId, String receiverName,
                             String receiverPhone, String province, String city,
                             String district, String detailAddress, String label) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));
        
        if (!address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此地址");
        }

        address.updateInfo(receiverName, receiverPhone, province, city, district, detailAddress, label);
        addressRepository.update(address);
    }

    /**
     * 删除收货地址
     */
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));
        
        if (!address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此地址");
        }

        addressRepository.delete(addressId);
    }

    /**
     * 设置默认地址
     */
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));
        
        if (!address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此地址");
        }

        // 取消所有默认地址
        addressRepository.cancelAllDefaultByUserId(userId);
        
        // 设置新的默认地址
        address.setAsDefault();
        addressRepository.update(address);
    }
}
