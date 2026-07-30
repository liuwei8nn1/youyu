package com.youyu.user.impl.interfaces.vo;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
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
}
