package com.youyu.user.impl.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 收货地址持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("address")
public class AddressDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

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
