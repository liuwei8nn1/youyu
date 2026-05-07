-- ============================================
-- Seckill Service - 秒杀模块 DDL
-- ============================================

-- DROP TABLE IF EXISTS `seckill_activity`;

CREATE TABLE IF NOT EXISTS `seckill_activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
    `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
    `stock` INT NOT NULL COMMENT '秒杀库存',
    `limit_per_user` INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- ============================================
-- MQ 消息补偿表
-- 注意：此表已由框架层统一管理，使用 youyu-framework/doc/mq-compensation-table.sql
-- 如果需要使用自定义表名，请在 application.yml 中配置：
--   mq:
--     compensation:
--       table-name: your_custom_table_name
-- ============================================

-- ============================================
-- 说明
-- ============================================
-- 1. deleted_at = NULL 表示未删除（有效数据）
-- 2. deleted_at = 时间戳 表示已删除
