-- ========================================
-- YouYu AI Service 数据库初始化脚本
-- ========================================

CREATE DATABASE IF NOT EXISTS `youyu_ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `youyu_ai`;

-- AI 调用记录表
CREATE TABLE IF NOT EXISTS `ai_call_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `call_type` VARCHAR(50) NOT NULL COMMENT '调用类型: polish-润色, generate-生成',
    `request_content` TEXT COMMENT '请求内容',
    `response_content` TEXT COMMENT '响应内容',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型名称',
    `tokens_used` INT DEFAULT 0 COMMENT '消耗的 Token 数量',
    `processing_time_ms` BIGINT DEFAULT 0 COMMENT '处理耗时（毫秒）',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_call_type` (`call_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 调用记录表';

-- AI 使用统计表
CREATE TABLE IF NOT EXISTS `ai_usage_stats` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `call_count` INT DEFAULT 0 COMMENT '调用次数',
    `total_tokens` INT DEFAULT 0 COMMENT '总 Token 消耗',
    `total_processing_time_ms` BIGINT DEFAULT 0 COMMENT '总处理耗时（毫秒）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 使用统计表';
