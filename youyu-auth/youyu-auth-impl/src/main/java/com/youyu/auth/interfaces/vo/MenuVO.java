package com.youyu.auth.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private String permissionCode;
    private Integer type;
    private Integer targetUserType;
    private Integer visible;
    private Integer status;
    private Integer sortOrder;
    private String redirect;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MenuVO> children;
}
