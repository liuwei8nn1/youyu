-- ============================================
-- Product Service - 商品模块 DDL
-- ============================================

-- DROP TABLE IF EXISTS `t_category`;
-- DROP TABLE IF EXISTS `t_product`;
-- DROP TABLE IF EXISTS `t_price_history`;
-- DROP TABLE IF EXISTS `t_stock_flow`;

CREATE TABLE IF NOT EXISTS `t_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 10. 商品表
CREATE TABLE IF NOT EXISTS `t_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
CREATE TABLE IF NOT EXISTS `t_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `stock` BIGINT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `is_seckill` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否参与秒杀',
    `seckill_start_time` DATETIME DEFAULT NULL COMMENT '秒杀开始时间',
    `seckill_end_time` DATETIME DEFAULT NULL COMMENT '秒杀结束时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    KEY `idx_product_name` (`product_name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 11. 价格历史表
CREATE TABLE IF NOT EXISTS `t_price_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `old_price` DECIMAL(10,2) NOT NULL COMMENT '原价格',
    `new_price` DECIMAL(10,2) NOT NULL COMMENT '新价格',
CREATE TABLE IF NOT EXISTS `t_price_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `old_price` DECIMAL(10,2) NOT NULL COMMENT '原价格',
    `new_price` DECIMAL(10,2) NOT NULL COMMENT '新价格',
    `change_reason` VARCHAR(200) DEFAULT NULL COMMENT '变更原因',
    `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格历史表';

-- 12. 库存流水表
CREATE TABLE IF NOT EXISTS `t_stock_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `before_stock` BIGINT NOT NULL COMMENT '变更前库存',
CREATE TABLE IF NOT EXISTS `t_stock_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `before_stock` BIGINT NOT NULL COMMENT '变更前库存',
    `change_quantity` INT NOT NULL COMMENT '变更数量',
    `flow_type` VARCHAR(20) NOT NULL COMMENT '流水类型: IN-入库, OUT-出库',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '关联订单号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ============================================
-- Order Service 表
