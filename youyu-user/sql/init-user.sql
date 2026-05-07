-- ============================================
-- User Service - 用户模块 DDL
-- ============================================

DROP TABLE IF EXISTS `user_profile`;
DROP TABLE IF EXISTS `address`;
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `employee`;
DROP TABLE IF EXISTS `platform_user`;
DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `identity_id` BIGINT NOT NULL COMMENT '关联 user_identity.id(Auth领域主键)',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `signature` VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity_id` (`identity_id`),
    UNIQUE KEY `uk_username_deleted` (`username`, `deleted_at`),
    UNIQUE KEY `uk_phone_deleted` (`phone`, `deleted_at`),
    UNIQUE KEY `uk_email_deleted` (`email`, `deleted_at`),
    KEY `idx_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 8. 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联 user_identity.user_id(业务用户ID)',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `zip_code` VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认地址',
    `label` VARCHAR(20) DEFAULT NULL COMMENT '地址标签(家、公司等)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ============================================
-- 外部顾客表（外部顾客的扩展信息）
-- ============================================
CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `identity_id` BIGINT NOT NULL COMMENT '关联user_identity.id（Auth领域主键）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity_id` (`identity_id`),
    UNIQUE KEY `uk_username_deleted` (`username`, `deleted_at`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部顾客表';

-- ============================================
-- 企业员工表（企业员工的扩展信息）
-- ============================================
CREATE TABLE IF NOT EXISTS `employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `identity_id` BIGINT NOT NULL COMMENT '关联user_identity.id（Auth领域主键）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `position` VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `hire_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入职时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity_id` (`identity_id`),
    UNIQUE KEY `uk_username_deleted` (`username`, `deleted_at`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业员工表';

-- ============================================
-- 平台管理员表（平台管理员的扩展信息）
-- ============================================
CREATE TABLE IF NOT EXISTS `platform_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `identity_id` BIGINT NOT NULL COMMENT '关联user_identity.id（Auth领域主键）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_identity_id` (`identity_id`),
    UNIQUE KEY `uk_username_deleted` (`username`, `deleted_at`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台管理员表';

-- ============================================
-- 部门表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
    `dept_name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `dept_code` VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
    `leader` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 初始化根部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `sort_order`, `status`) 
VALUES (0, -1, '某某科技', 'ROOT', 0, 1);

-- ============================================
-- 初始化测试数据（与 init-auth.sql 中的 user_identity 对应）
-- ============================================

-- 平台管理员扩展信息 (identity_id=1, user_id=1)
INSERT INTO `platform_user` (`id`, `identity_id`, `username`, `phone`, `email`, `status`) VALUES
(1, 1, 'platform_admin', '13800000001', 'admin@platform.com', 1);

-- 企业管理员扩展信息 (identity_id=2, user_id=2)
INSERT INTO `employee` (`id`, `identity_id`, `username`, `phone`, `email`, `dept_id`, `position`, `status`) VALUES
(1, 2, 'enterprise_admin', '13800000002', 'admin@enterprise.com', 0, '总经理', 1);

-- 外部顾客扩展信息 (identity_id=3, user_id=3)
INSERT INTO `customer` (`id`, `identity_id`, `username`, `phone`, `email`, `status`) VALUES
(1, 3, 'customer01', '13800000003', 'customer01@example.com', 1);

-- 用户资料（通用资料表）
INSERT INTO `user_profile` (`id`, `identity_id`, `username`, `nickname`, `avatar`, `email`, `phone`, `gender`) VALUES
(1, 1, 'platform_admin', '平台管理员', NULL, 'admin@platform.com', '13800000001', 0),
(2, 2, 'enterprise_admin', '企业管理员', NULL, 'admin@enterprise.com', '13800000002', 0),
(3, 3, 'customer01', '顾客01', NULL, 'customer01@example.com', '13800000003', 0);

-- ============================================
-- Product Service 表
