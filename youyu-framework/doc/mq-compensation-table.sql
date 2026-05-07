-- ============================================
-- MQ 消息补偿表
-- ============================================
-- 说明：
-- 1. 此表用于存储发送失败的 MQ 消息
-- 2. 由定时任务扫描并重新发送
-- 3. 支持自定义表名（默认：mq_message_compensation）
-- ============================================

-- DROP TABLE IF EXISTS `mq_message_compensation`;

CREATE TABLE IF NOT EXISTS `mq_message_compensation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_id` VARCHAR(100) NOT NULL COMMENT '消息ID（业务唯一标识）',
    `topic` VARCHAR(100) NOT NULL COMMENT 'MQ Topic',
    `tag` VARCHAR(50) DEFAULT NULL COMMENT 'MQ Tag',
    `message_body` TEXT NOT NULL COMMENT '消息体（JSON字符串）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-成功, 3-失败',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_next_retry_time` (`next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消息补偿表';
-- UNIQUE KEY `uk_message_id_topic_tag` (`message_id`, `topic`, `tag`), 可以根据业务需求添加
