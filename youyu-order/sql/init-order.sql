-- ============================================
-- Order Service - 订单模块 DDL
-- ============================================

-- DROP TABLE IF EXISTS `t_order`;

CREATE TABLE IF NOT EXISTS `t_order` (
    `id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消',
    `receiver_info` JSON DEFAULT NULL COMMENT '收货信息快照(JSON)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no_deleted` (`order_no`, `deleted_at`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- Seckill Service 表
-- ============================================

-- 14. 秒杀活动表（不继承基类，手动管理）
