package com.youyu.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收货地址DTO
 * <p>
 * 用于微服务间通过 HTTP/Feign 传递用户地址信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地址ID
     */
    private Long addressId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 是否为默认地址
     */
    private Boolean isDefault;
}
