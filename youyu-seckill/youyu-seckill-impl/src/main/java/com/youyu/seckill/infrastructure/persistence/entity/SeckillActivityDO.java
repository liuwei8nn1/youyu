package com.youyu.seckill.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.Bean;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀活动DO（基础设施层 - 数据库实体）
 */
@Data
@TableName("seckill_activity")
public class SeckillActivityDO implements Bean<Long>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer stock;

    private Integer limitPerUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ==================== 字段常量定义 ====================
    public static final String ID = "id";
    public static final String PRODUCT_ID = "product_id";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String STOCK = "stock";
    public static final String LIMIT_PER_USER = "limit_per_user";
}
