package com.youyu.user.impl.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeVO {
    private Long id;
    private Long identityId;
    private String username;
    private String phone;
    private String email;
    private Long deptId;
    private String position;
    private Integer status;
    private LocalDateTime hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
