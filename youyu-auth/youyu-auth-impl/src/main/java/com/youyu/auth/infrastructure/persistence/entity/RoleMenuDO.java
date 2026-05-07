package com.youyu.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.BaseDO;
import com.youyu.framework.datasource.mybatis.Bean;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色-菜单关联数据对象
 */
@Data
@TableName("sys_role_menu")
public class RoleMenuDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;
    private Long menuId;
}
