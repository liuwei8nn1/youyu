# 用户权限获取功能实现说明

## 功能概述

实现了 `getUserPermissions` 接口，用于获取指定用户的所有权限码（permission codes）。

## 实现内容

### 1. 应用服务层 (`RolePermissionApplicationService`)

新增方法：
```java
public List<String> getUserPermissions(Long userId, Integer userType)
```

**功能逻辑：**
1. 调用 `getUserMenus()` 获取用户的菜单列表
2. 从菜单列表中提取所有非空的 `permissionCode`
3. 去重后返回权限码列表

### 2. 控制器层 (`RolePermissionController`)

新增接口：
```java
@GetMapping("/getUserPermissions")
public Result<List<String>> getUserPermissions(
    @RequestParam("userId") Long userId,
    @RequestParam("userType") Integer userType
)
```

**接口地址：** `GET /api/auth/role-permission/getUserPermissions`

**请求参数：**
- `userId`: 用户ID
- `userType`: 用户类型（1-顾客, 2-企业, 3-平台）

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": ["user:view", "user:create", "role:view"]
}
```

## 技术特点

### 1. 复用现有逻辑
- 基于已有的 `getUserMenus()` 方法实现
- 充分利用菜单树缓存机制
- 保持与菜单查询一致的权限过滤逻辑

### 2. 性能优化
- 使用 Stream API 进行函数式编程
- 自动去重（distinct）
- 利用 Caffeine 缓存减少数据库查询

### 3. 健壮性处理
- 空值检查：过滤 null 和空的 permissionCode
- 空列表处理：用户无角色或无菜单时返回空列表
- 日志记录：记录权限获取过程和结果

## 数据流程

```
用户ID + 用户类型
    ↓
获取用户角色编码 (UserServiceAdapter)
    ↓
查询角色信息 (RoleRepository)
    ↓
获取角色关联的菜单ID (RoleMenuRepository)
    ↓
从缓存获取菜单树并过滤 (MenuRepository + Cache)
    ↓
提取权限码并去重 (Stream API)
    ↓
返回权限码列表
```

## 使用示例

### cURL
```bash
curl -X GET "http://localhost:8080/api/auth/role-permission/getUserPermissions?userId=1&userType=2"
```

### JavaScript (Axios)
```javascript
axios.get('/api/auth/role-permission/getUserPermissions', {
  params: {
    userId: 1,
    userType: 2
  }
}).then(response => {
  console.log('用户权限:', response.data);
});
```

### Java (Feign Client)
```java
@FeignClient(name = "youyu-auth")
public interface AuthFeignClient {
    @GetMapping("/api/auth/role-permission/getUserPermissions")
    Result<List<String>> getUserPermissions(
        @RequestParam("userId") Long userId,
        @RequestParam("userType") Integer userType
    );
}
```

## 测试

### 单元测试
位置：`youyu-auth-impl/src/test/java/com/youyu/auth/integration/UserPermissionIntegrationTest.java`

运行测试：
```bash
mvn test -pl youyu-auth/youyu-auth-impl -Dtest=UserPermissionIntegrationTest
```

### 手动测试
1. 确保数据库已初始化（执行 `youyu-auth/sql/init-auth.sql`）
2. 启动 auth 服务
3. 访问接口进行测试

## 注意事项

1. **依赖关系**：该接口依赖用户服务的角色关联功能，需要确保 `user_role` 表中有正确的数据
2. **权限码来源**：权限码来自菜单表的 `permission_code` 字段，DIRECTORY 类型菜单通常为空
3. **缓存策略**：菜单树缓存时间为 10 分钟，修改菜单后可能需要等待缓存过期
4. **用户类型**：不同用户类型的菜单和权限可能不同，务必传入正确的 userType

## 相关文件

- 应用服务：`youyu-auth-impl/src/main/java/com/youyu/auth/application/service/RolePermissionApplicationService.java`
- 控制器：`youyu-auth-impl/src/main/java/com/youyu/auth/interfaces/controller/RolePermissionController.java`
- API文档：`youyu-auth/doc/user-permissions-api.md`
- 集成测试：`youyu-auth-impl/src/test/java/com/youyu/auth/integration/UserPermissionIntegrationTest.java`

## 后续优化建议

1. **缓存优化**：可以考虑对用户权限码进行单独缓存
2. **批量查询**：支持批量获取多个用户的权限
3. **权限变更通知**：当角色或菜单变更时，主动清除相关用户的权限缓存
4. **权限继承**：支持角色继承关系中的权限合并