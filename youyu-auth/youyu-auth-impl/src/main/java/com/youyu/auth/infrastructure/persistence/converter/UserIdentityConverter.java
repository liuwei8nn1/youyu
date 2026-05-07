package com.youyu.auth.infrastructure.persistence.converter;

import com.youyu.auth.domain.model.UserIdentity;
import com.youyu.auth.infrastructure.persistence.entity.UserIdentityDO;

/**
 * 用户身份转换器 - 手动实现以避免MapStruct访问权限问题
 */
public class UserIdentityConverter {
    
    private static final UserIdentityConverter INSTANCE = new UserIdentityConverter();
    
    public static UserIdentityConverter getInstance() {
        return INSTANCE;
    }
    
    /**
     * DO转领域模型
     */
    public UserIdentity toDomain(UserIdentityDO userIdentityDO) {
        if (userIdentityDO == null) {
            return null;
        }
        
        return UserIdentity.restore(
                userIdentityDO.getId(),
                userIdentityDO.getUsername(),
                userIdentityDO.getPassword(),
                userIdentityDO.getUserType(),
                userIdentityDO.getEnabled(),
                userIdentityDO.getCreatedAt(),
                userIdentityDO.getUpdatedAt()
        );
    }
    
    /**
     * 领域模型转DO
     */
    public UserIdentityDO toDO(UserIdentity userIdentity) {
        if (userIdentity == null) {
            return null;
        }
        
        UserIdentityDO userIdentityDO = new UserIdentityDO();
        userIdentityDO.setId(userIdentity.getId());
        userIdentityDO.setUsername(userIdentity.getUsername());
        userIdentityDO.setPassword(userIdentity.getPassword());
        userIdentityDO.setUserType(userIdentity.getUserType());
        userIdentityDO.setEnabled(userIdentity.getEnabled());
        userIdentityDO.setCreatedAt(userIdentity.getCreatedAt());
        userIdentityDO.setUpdatedAt(userIdentity.getUpdatedAt());
        
        return userIdentityDO;
    }
}
