package com.youyu.user.impl.domain.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收货地址实体
 * 属于用户领域，管理用户的收货地址信息
 */
@Getter
public class Address implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String zipCode;
    private Integer isDefault;
    private String label;

    public Address() {
    }

    /**
     * 创建收货地址
     */
    public static Address create(Long userId, String receiverName, String receiverPhone,
                                String province, String city, String district,
                                String detailAddress, Integer isDefault, String label) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalArgumentException("收货人姓名不能为空");
        }
        if (receiverPhone == null || !receiverPhone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("收货人手机号格式不正确");
        }
        if (province == null || province.trim().isEmpty()) {
            throw new IllegalArgumentException("省份不能为空");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("城市不能为空");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("区县不能为空");
        }
        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("详细地址不能为空");
        }

        Address address = new Address();
        address.userId = userId;
        address.receiverName = receiverName;
        address.receiverPhone = receiverPhone;
        address.province = province;
        address.city = city;
        address.district = district;
        address.detailAddress = detailAddress;
        address.isDefault = isDefault != null ? isDefault : 0;
        address.label = label;
        return address;
    }

    /**
     * 从持久化恢复
     */
    public static Address restore(Long id, Long userId, String receiverName,
                                 String receiverPhone, String province, String city,
                                 String district, String detailAddress, String zipCode,
                                 Integer isDefault, String label) {
        Address address = new Address();
        address.id = id;
        address.userId = userId;
        address.receiverName = receiverName;
        address.receiverPhone = receiverPhone;
        address.province = province;
        address.city = city;
        address.district = district;
        address.detailAddress = detailAddress;
        address.zipCode = zipCode;
        address.isDefault = isDefault != null ? isDefault : 0;
        address.label = label;
        return address;
    }

    /**
     * 验证数据有效性
     */
    public void validate() {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalArgumentException("收货人姓名不能为空");
        }
        if (receiverPhone != null && !receiverPhone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("收货人手机号格式不正确");
        }
        if (province == null || province.trim().isEmpty()) {
            throw new IllegalArgumentException("省份不能为空");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("城市不能为空");
        }
        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("详细地址不能为空");
        }
    }

    /**
     * 更新地址信息
     */
    public void updateInfo(String receiverName, String receiverPhone, String province,
                          String city, String district, String detailAddress, String label) {
        if (receiverName != null && !receiverName.trim().isEmpty()) {
            this.receiverName = receiverName;
        }
        if (receiverPhone != null) {
            this.receiverPhone = receiverPhone;
        }
        if (province != null && !province.trim().isEmpty()) {
            this.province = province;
        }
        if (city != null && !city.trim().isEmpty()) {
            this.city = city;
        }
        if (district != null && !district.trim().isEmpty()) {
            this.district = district;
        }
        if (detailAddress != null && !detailAddress.trim().isEmpty()) {
            this.detailAddress = detailAddress;
        }
        if (label != null) {
            this.label = label;
        }
        validate();
    }

    /**
     * 设置为默认地址
     */
    public void setAsDefault() {
        this.isDefault = 1;
    }

    /**
     * 取消默认地址
     */
    public void cancelDefault() {
        this.isDefault = 0;
    }

    /**
     * 是否为默认地址
     */
    public boolean isDefault() {
        return this.isDefault != null && this.isDefault == 1;
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return province + city + district + detailAddress;
    }
}
