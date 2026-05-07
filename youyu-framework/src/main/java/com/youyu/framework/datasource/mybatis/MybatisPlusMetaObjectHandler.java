package com.youyu.framework.datasource.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 用于自动填充 create_time 和 update_time 字段
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 如果 createdAt 为空，则自动填充
        if (metaObject.getValue("createdAt") == null) {
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        }
        // 如果 updatedAt 为空，则自动填充
        if (metaObject.getValue("updatedAt") == null) {
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充 updatedAt
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
