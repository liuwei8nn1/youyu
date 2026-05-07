-- ============================================
-- Auth Service - 认证授权模块 DDL
-- ============================================

DROP TABLE IF EXISTS `user_identity`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `user_device`;

-- 1. 用户身份表(统一认证表,存储所有类型用户的登录凭证)
CREATE TABLE IF NOT EXISTS `user_identity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户id',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_user_type_deleted_at` (`user_id`, `user_type`, `deleted_at`),
    UNIQUE KEY `uk_username_user_type_deleted_at` (`username`, `user_type`, `deleted_at`),
    UNIQUE KEY `uk_phone_user_type_deleted_at` (`phone`, `user_type`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户身份表(统一认证)';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code_deleted` (`role_code`, `deleted_at`),
    KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 菜单表（包含权限信息）
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `permission_code` VARCHAR(100) DEFAULT NULL COMMENT '权限编码(DIRECTORY类型可为空)',
    `type` TINYINT NOT NULL DEFAULT 2 COMMENT '菜单类型: 1-目录, 2-菜单, 3-按钮',
    `target_user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '目标用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见: 0-隐藏, 1-显示',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `redirect` VARCHAR(200) DEFAULT NULL COMMENT '重定向路径',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted_at` BIGINT NULL DEFAULT NULL COMMENT '逻辑删除时间戳(NULL-未删除)',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_permission_code` (`permission_code`),
    KEY `idx_target_user_type` (`target_user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 4. 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- 5. 用户-角色关联表（DDD: 属于 Auth 领域 - 授权关系）
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_identity_id` BIGINT NOT NULL COMMENT '用户身份ID（引用 user_identity.id）',
    `user_id` BIGINT NOT NULL COMMENT '用户业务ID（冗余字段，便于查询）',
    `user_type` TINYINT NOT NULL COMMENT '用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM',
    `role_id` BIGINT NOT NULL COMMENT '角色ID（引用 sys_role.id）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_identity_id`, `role_id`),
    KEY `idx_user_id_type` (`user_id`, `user_type`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 7. 用户设备表
CREATE TABLE IF NOT EXISTS `user_device` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `identity_id` BIGINT NOT NULL COMMENT '用户身份ID（关联 user_identity.id）',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM',
    `device_unique_id` VARCHAR(64) NOT NULL COMMENT '客户端唯一标识(前端生成)',
    `device_name` VARCHAR(100) COMMENT '设备名称(用户自定义或自动获取)',
    `os` VARCHAR(50) COMMENT '操作系统',
    `browser` VARCHAR(50) COMMENT '浏览器',
    `ip` VARCHAR(45) COMMENT '登录IP',
    `user_agent` TEXT COMMENT '原始User-Agent',
    `login_time` DATETIME NOT NULL COMMENT '本次登录时间',
    `status` TINYINT DEFAULT 1 COMMENT '1-在线 0-已登出',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    UNIQUE KEY `uk_identity_device` (`identity_id`, `user_type`, `device_unique_id`),
    INDEX idx_identity_id (`identity_id`),
    INDEX idx_user_type (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设备表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化角色（包含所有用户类型）
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `user_type`, `status`, `sort_order`) VALUES
-- 外部顾客角色 (user_type=1)
(1, 'CUSTOMER_USER', '外部顾客', '基础顾客权限', 1, 1, 1),
-- 企业用户角色 (user_type=2)
(2, 'ENTERPRISE_ADMIN', '企业管理员', '拥有企业管理权限', 2, 1, 1),
(3, 'ENTERPRISE_USER', '企业普通用户', '基础企业用户权限', 2, 1, 2),
-- 平台管理员角色 (user_type=3)
(4, 'PLATFORM_ADMIN', '平台管理员', '拥有平台管理权限', 3, 1, 1);

-- 初始化菜单（包含权限码）
-- 注意：target_user_type 字段表示该菜单对哪些用户类型可见：0-未知, 1-顾客, 2-企业, 3-平台
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `permission_code`, `type`, `target_user_type`, `visible`, `sort_order`) VALUES
-- ============================================
-- 企业用户 (user_type=2) 的菜单结构
-- ============================================
-- 系统管理目录 (企业)
(1, 0, '系统管理', '/system', 'Layout', 'Setting', NULL, 1, 2, 1, 1),
-- 用户管理菜单 (企业)
(2, 1, '用户管理', '/system/user', 'system/user/index', 'user', 'system:user', 2, 2, 1, 1),
(3, 2, '查看用户', '', '', '', 'user:view', 3, 2, 1, 1),
(4, 2, '创建用户', '', '', '', 'user:create', 3, 2, 1, 2),
(5, 2, '修改用户', '', '', '', 'user:update', 3, 2, 1, 3),
-- 角色管理菜单 (企业)
(6, 1, '角色管理', '/system/role', 'system/role/index', 'team', 'system:role', 2, 2, 1, 2),
(7, 6, '查看角色', '', '', '', 'role:view', 3, 2, 1, 1),
(8, 6, '创建角色', '', '', '', 'role:create', 3, 2, 1, 2),
(9, 6, '修改角色', '', '', '', 'role:update', 3, 2, 1, 3),
(10, 6, '分配权限', '', '', '', 'role:assign_menu', 3, 2, 1, 4),
-- 菜单管理菜单 (企业 - 只读)
(11, 1, '菜单管理', '/system/menu', 'system/menu/index', 'menu', 'system:menu', 2, 2, 1, 3),
(12, 11, '查看菜单', '', '', '', 'menu:view', 3, 2, 1, 1),
-- 企业管理目录 (企业)
(20, 0, '企业管理', '/enterprise', 'Layout', 'OfficeBuilding', NULL, 1, 2, 1, 10),
-- 部门管理
(21, 20, '部门管理', '/enterprise/dept', 'enterprise/dept/index', 'tree', 'enterprise:dept', 2, 2, 1, 1),
(22, 21, '查看部门', '', '', '', 'dept:view', 3, 2, 1, 1),
(23, 21, '创建部门', '', '', '', 'dept:create', 3, 2, 1, 2),
(24, 21, '修改部门', '', '', '', 'dept:update', 3, 2, 1, 3),
(25, 21, '删除部门', '', '', '', 'dept:delete', 3, 2, 1, 4),
-- 员工管理
(26, 20, '员工管理', '/enterprise/employee', 'enterprise/employee/index', 'user', 'enterprise:employee', 2, 2, 1, 2),
(27, 26, '查看员工', '', '', '', 'employee:view', 3, 2, 1, 1),
(28, 26, '创建员工', '', '', '', 'employee:create', 3, 2, 1, 2),
(29, 26, '修改员工', '', '', '', 'employee:update', 3, 2, 1, 3),
(30, 26, '删除员工', '', '', '', 'employee:delete', 3, 2, 1, 4),
(31, 26, '分配角色', '', '', '', 'employee:assignrole', 3, 2, 1, 5),

-- ============================================
-- 平台管理员 (user_type=3) 的菜单结构
-- ============================================
-- 系统管理目录 (平台)
(101, 0, '系统管理', '/system', 'Layout', 'Setting', NULL, 1, 3, 1, 1),
-- 用户管理菜单 (平台)
(102, 101, '用户管理', '/system/user', 'system/user/index', 'user', 'system:user', 2, 3, 1, 1),
(103, 102, '查看用户', '', '', '', 'user:view', 3, 3, 1, 1),
(104, 102, '创建用户', '', '', '', 'user:create', 3, 3, 1, 2),
(105, 102, '修改用户', '', '', '', 'user:update', 3, 3, 1, 3),
(106, 102, '删除用户', '', '', '', 'user:delete', 3, 3, 1, 4),
-- 角色管理菜单 (平台)
(107, 101, '角色管理', '/system/role', 'system/role/index', 'team', 'system:role', 2, 3, 1, 2),
(108, 107, '查看角色', '', '', '', 'role:view', 3, 3, 1, 1),
(109, 107, '创建角色', '', '', '', 'role:create', 3, 3, 1, 2),
(110, 107, '修改角色', '', '', '', 'role:update', 3, 3, 1, 3),
(111, 107, '删除角色', '', '', '', 'role:delete', 3, 3, 1, 4),
(112, 107, '分配权限', '', '', '', 'role:assign_menu', 3, 3, 1, 5),
-- 菜单管理菜单 (平台 - 完整权限)
(113, 101, '菜单管理', '/system/menu', 'system/menu/index', 'menu', 'system:menu', 2, 3, 1, 3),
(114, 113, '查看菜单', '', '', '', 'menu:view', 3, 3, 1, 1),
(115, 113, '创建菜单', '', '', '', 'menu:create', 3, 3, 1, 2),
(116, 113, '修改菜单', '', '', '', 'menu:update', 3, 3, 1, 3),
(117, 113, '删除菜单', '', '', '', 'menu:delete', 3, 3, 1, 4),

-- ============================================
-- 企业管理目录 (平台管理员 user_type=3)
-- ============================================
(201, 0, '企业管理', '/enterprise', 'Layout', 'office', NULL, 1, 3, 1, 2),
-- 部门管理
(202, 201, '部门管理', '/enterprise/dept', 'enterprise/dept/index', 'tree', 'enterprise:dept', 2, 3, 1, 1),
(203, 202, '查看部门', '', '', '', 'dept:view', 3, 3, 1, 1),
(204, 202, '创建部门', '', '', '', 'dept:create', 3, 3, 1, 2),
(205, 202, '修改部门', '', '', '', 'dept:update', 3, 3, 1, 3),
(206, 202, '删除部门', '', '', '', 'dept:delete', 3, 3, 1, 4),
-- 员工管理
(207, 201, '员工管理', '/enterprise/employee', 'enterprise/employee/index', 'user', 'enterprise:employee', 2, 3, 1, 2),
(208, 207, '查看员工', '', '', '', 'employee:view', 3, 3, 1, 1),
(209, 207, '创建员工', '', '', '', 'employee:create', 3, 3, 1, 2),
(210, 207, '修改员工', '', '', '', 'employee:update', 3, 3, 1, 3),
(211, 207, '删除员工', '', '', '', 'employee:delete', 3, 3, 1, 4),
(212, 207, '分配角色', '', '', '', 'employee:assign_role', 3, 3, 1, 5);

-- 初始化角色-菜单关联关系
-- 平台管理员 (role_id=4) 拥有所有菜单权限 (target_user_type=3)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 4, id FROM `sys_menu` WHERE target_user_type = 3;

-- 企业管理员 (role_id=2) 拥有除删除操作外的所有菜单权限 (target_user_type=2)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 2, id FROM `sys_menu` 
WHERE target_user_type = 2 
AND permission_code NOT IN ('user:delete', 'role:delete', 'menu:create', 'menu:update', 'menu:delete', 'dept:delete', 'employee:delete');

-- 企业普通用户 (role_id=3) 只能查看 (target_user_type=2)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 3, id FROM `sys_menu` 
WHERE target_user_type = 2 
AND permission_code IN ('user:view', 'role:view', 'menu:view', 'dept:view', 'employee:view');

-- 外部顾客 (role_id=1) 只能查看基本信息 (target_user_type=1)
-- 目前没有为顾客初始化菜单，如需添加可自行补充

-- 插入测试用户数据(密码均为BCrypt加密)
-- 原始密码: 所有账号统一使用 Admin123
-- 说明: 为了测试方便，所有账号统一使用密码 Admin123，登录后可以自行修改
-- 如需生成新密码，可使用以下Java代码:
-- import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
-- System.out.println(new BCryptPasswordEncoder().encode("your_password"));
INSERT INTO `user_identity` (`user_id`, `username`, `password`, `user_type`, `enabled`) VALUES
-- 平台管理员 (user_type=3) - 密码: Admin123
(1, 'platform_admin', '$2a$10$hV54g36ey1OnWfgLWXtzz.CwYBIdk.RqHN4FpVmyg.cgKGqEDuPAq', 3, 1),
-- 企业管理员 (user_type=2) - 密码: Admin123
(2, 'enterprise_admin', '$2a$10$hV54g36ey1OnWfgLWXtzz.CwYBIdk.RqHN4FpVmyg.cgKGqEDuPAq', 2, 1),
-- 外部顾客 (user_type=1) - 密码: Admin123
(3, 'customer01', '$2a$10$hV54g36ey1OnWfgLWXtzz.CwYBIdk.RqHN4FpVmyg.cgKGqEDuPAq', 1, 1);

-- 初始化用户-角色关联关系
-- 注意: user_identity_id 是 user_identity 表的 id 字段，不是 user_id
-- platform_admin (user_identity_id=1) -> PLATFORM_ADMIN (role_id=4)
INSERT INTO `user_role` (`user_identity_id`, `user_id`, `user_type`, `role_id`) VALUES
(1, 1, 3, 4),
-- enterprise_admin (user_identity_id=2) -> ENTERPRISE_ADMIN (role_id=2)
(2, 2, 2, 2),
-- customer01 (user_identity_id=3) -> CUSTOMER_USER (role_id=1)
(3, 3, 1, 1);
