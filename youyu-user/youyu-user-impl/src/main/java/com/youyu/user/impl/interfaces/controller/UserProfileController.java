package com.youyu.user.impl.interfaces.controller;

import com.youyu.common.model.Result;
import com.youyu.user.impl.application.service.UserProfileApplicationService;
import com.youyu.user.impl.interfaces.vo.AddressVO;
import com.youyu.user.impl.interfaces.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileApplicationService userProfileApplicationService;

    /**
     * 获取用户资料
     */
    @GetMapping("/profile/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        Optional<UserProfileVO> profile = userProfileApplicationService.getUserProfile(userId);
        return profile.map(Result::success)
                .orElseGet(() -> Result.error("用户资料不存在"));
    }

    /**
     * 保存或更新用户资料
     */
    @PostMapping("/profile")
    public Result<Void> saveOrUpdateUserProfile(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "avatar", required = false) String avatar,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "gender", required = false) Integer gender,
            @RequestParam(value = "birthday", required = false) LocalDateTime birthday,
            @RequestParam(value = "signature", required = false) String signature) {
        
        userProfileApplicationService.saveOrUpdateUserProfile(
                userId, nickname, avatar, email, phone, gender, birthday, signature);
        return Result.success();
    }

    /**
     * 获取用户地址列表
     */
    @GetMapping("/address/{userId}")
    public Result<List<AddressVO>> getUserAddresses(@PathVariable Long userId) {
        List<AddressVO> addresses = userProfileApplicationService.getUserAddresses(userId);
        return Result.success(addresses);
    }

    /**
     * 添加收货地址
     */
    @PostMapping("/address")
    public Result<Long> addAddress(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "receiverName") String receiverName,
            @RequestParam(value = "receiverPhone") String receiverPhone,
            @RequestParam(value = "province") String province,
            @RequestParam(value = "city") String city,
            @RequestParam(value = "district") String district,
            @RequestParam(value = "detailAddress") String detailAddress,
            @RequestParam(value = "zipCode", required = false) String zipCode,
            @RequestParam(value = "isDefault", required = false, defaultValue = "0") Integer isDefault,
            @RequestParam(value = "label", required = false) String label) {
        
        Long addressId = userProfileApplicationService.addAddress(
                userId, receiverName, receiverPhone, province, city, 
                district, detailAddress, zipCode, isDefault, label);
        return Result.success(addressId);
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/address/{addressId}")
    public Result<Void> updateAddress(
            @PathVariable Long addressId,
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "receiverName", required = false) String receiverName,
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "detailAddress", required = false) String detailAddress,
            @RequestParam(value = "label", required = false) String label) {
        
        userProfileApplicationService.updateAddress(
                addressId, userId, receiverName, receiverPhone, 
                province, city, district, detailAddress, label);
        return Result.success();
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/address/{addressId}")
    public Result<Void> deleteAddress(
            @PathVariable Long addressId,
            @RequestParam(value = "userId") Long userId) {
        
        userProfileApplicationService.deleteAddress(addressId, userId);
        return Result.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/address/{addressId}/default")
    public Result<Void> setDefaultAddress(
            @PathVariable Long addressId,
            @RequestParam(value = "userId") Long userId) {
        
        userProfileApplicationService.setDefaultAddress(addressId, userId);
        return Result.success();
    }
}
