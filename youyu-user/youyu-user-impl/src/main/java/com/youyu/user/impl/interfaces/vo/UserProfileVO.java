package com.youyu.user.impl.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    private Long id;
    private Long identityId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private LocalDateTime birthday;
    private String signature;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
