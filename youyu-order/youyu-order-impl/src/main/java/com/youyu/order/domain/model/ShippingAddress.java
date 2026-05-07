package com.youyu.order.domain.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收货地址值对象
 * 作为订单的快照，保存下单时的地址信息
 * 这是一个值对象，不具有独立的生命周期
 */
@Getter
public class ShippingAddress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String zipCode;

    public ShippingAddress() {
    }

    /**
     * 创建收货地址值对象
     */
    public static ShippingAddress create(String receiverName, String receiverPhone,
                                        String province, String city, String district,
                                        String detailAddress, String zipCode) {
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

        ShippingAddress address = new ShippingAddress();
        address.receiverName = receiverName;
        address.receiverPhone = receiverPhone;
        address.province = province;
        address.city = city;
        address.district = district;
        address.detailAddress = detailAddress;
        address.zipCode = zipCode;
        return address;
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return province + city + district + detailAddress;
    }
}
