package com.youyu.auth.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer userType;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
