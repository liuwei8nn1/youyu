# 角色菜单管理使用说明

## 架构设计说明

### 简化版权限设计

本系统采用简化的权限设计方案：
- **菜单即权限**：菜单表中包含权限码（`permission_code`）
- **角色关联菜单**：通过 `sys_role_menu` 表将角色与菜单关联
- **用户关联角色**：通过 `user_role` 表（在 user 服务中）将用户与角色关联
- **前后端统一权限控制**：使用同一个 `permission_code` 进行前端菜单显示和后端 API 访问控制

### 数据库表结构

该脚本会创建:
- `sys_role` - 角色表
- `sys_menu` - 菜单表（包含权限信息）
- `sys_role_menu` - 角色-菜单关联表

注意：`user_role` 表位于 user 服务的数据库中

## API接口说明

### 1. 角色管理

#### 创建角色
```http
POST /api/auth/roles
Content-Type: application/json

{
  "roleCode": "VIP_USER",
  "roleName": "VIP用户",
  "description": "VIP会员专属角色",
  "sortOrder": 4
}
```

#### 更新角色
```http
PUT /api/auth/roles/{roleId}
Content-Type: application/json

{
  "roleName": "高级VIP用户",
  "description": "更新后的描述",
  "sortOrder": 5
}
```

#### 删除角色
```http
DELETE /api/auth/roles/{roleId}
```

#### 查询所有角色
```http
GET /api/auth/roles
```

### 2. 菜单权限管理

#### 为角色分配菜单
```http
POST /api/auth/roles/{roleId}/menus
Content-Type: application/json

{
  "menuIds": [1, 2, 3, 4, 5]
}
```

#### 查询角色的菜单
```http
GET /api/auth/roles/{roleId}/menus
```

#### 查询所有菜单（用于分配时选择）
```http
GET /api/auth/menus/all
```

### 3. 菜单管理

#### 创建菜单
```http
POST /api/auth/menus
Content-Type: application/json

{
  "parentId": 0,
  "name": "订单管理",
  "path": "/order",
  "component": "Layout",
  "icon": "shopping",
  "permissionCode": "order:manage",
  "type": "MENU",
  "visible": 1,
  "sortOrder": 10,
  "redirect": "/order/list"
}
```

#### 更新菜单
```http
PUT /api/auth/menus/{menuId}
Content-Type: application/json

{
  "name": "订单管理(更新)",
  "path": "/order",
  "component": "Layout",
  "icon": "shopping-cart",
  "permissionCode": "order:manage",
  "visible": 1,
  "sortOrder": 10,
  "redirect": "/order/list"
}
```

#### 删除菜单
```http
DELETE /api/auth/menus/{menuId}
```

#### 获取所有菜单(树形结构)
```http
GET /api/auth/menus
```

#### 获取当前用户的菜单
```http
GET /api/auth/user/menus?userId=1001
```

## 架构设计说明

### DDD分层架构

```
interfaces (Controller层)
    ↓
application (应用服务层)
    ↓
domain (领域层)
    ↑
infrastructure (基础设施层)
```

### 各层职责

#### Domain层 (领域层)
- **Role.java** - 角色聚合根
- **Menu.java** - 菜单实体（包含权限信息）
- **RoleRepository.java** - 角色仓储接口
- **MenuRepository.java** - 菜单仓储接口

#### Infrastructure层 (基础设施层)
- **DO类**: RoleDO, MenuDO, RoleMenuDO
- **Mapper**: RoleMapper, MenuMapper, RoleMenuMapper
- **Converter**: RoleConverter, MenuConverter
- **Repository实现**: RoleRepositoryImpl, MenuRepositoryImpl

#### Application层 (应用服务层)
- **RolePermissionApplicationService** - 统一的角色菜单应用服务
  - 角色CRUD
  - 菜单分配
  - 菜单管理
  - 用户菜单查询

#### Interfaces层 (接口层)
- **RolePermissionController** - REST API控制器
  - 提供HTTP接口
  - 参数校验
  - 异常处理

## 业务规则

### 角色管理
1. 角色编码必须唯一
2. 删除角色前需检查是否有用户关联
3. 角色可以启用/禁用

### 菜单管理
1. 菜单支持树形结构(通过parentId)
2. 菜单包含权限码（permission_code）用于前后端权限控制
3. 菜单类型：DIRECTORY-目录, MENU-菜单, BUTTON-按钮
4. 删除菜单前需检查是否有子菜单
5. DIRECTORY 类型的菜单 permission_code 可为 NULL

## 待完善功能

1. **用户角色关联**: user_role 表已在 user 服务中实现
2. **Feign调用**: RolePermissionApplicationService中的getUserMenus需要完善角色→菜单的查询逻辑
3. **菜单缓存**: 建议添加Redis缓存优化菜单查询性能
4. **操作日志**: 记录角色、菜单的变更日志
5. **权限校验AOP**: 已有的 @Permission 注解基于 permission_code 进行 API 权限控制，无需额外配置

## 注意事项

1. 本实现遵循DDD架构原则,领域层不依赖任何框架
2. 使用MapStruct进行DO和领域模型的转换
3. 使用MyBatis-Plus简化数据库操作
4. 所有写操作都有事务保护(@Transactional)
5. 统一的Result返回格式
