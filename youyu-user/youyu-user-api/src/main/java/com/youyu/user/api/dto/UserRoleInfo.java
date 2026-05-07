package com.youyu.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户角色信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;
}
