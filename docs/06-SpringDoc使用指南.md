# SpringDoc OpenAPI 注解使用指南

本文档介绍如何在 Java 代码中使用 SpringDoc OpenAPI 注解来生成 API 文档，特别关注枚举类型的处理。

## 常用注解概览

### @Tag - API 分组标签
```java
@Tag(name = "用户管理", description = "用户相关的操作接口")
@RestController
@RequestMapping("/api/users")
public class UserController { }
```

### @Operation - 接口方法描述
```java
@Operation(summary = "创建新用户", description = "根据提供的用户信息创建新的用户账户")
@PostMapping
public Result<UserVO> createUser(@RequestBody CreateUserRequest request) { }
```

### @Parameter - 参数描述
```java
@GetMapping("/{id}")
public Result<UserVO> getUserById(
    @Parameter(description = "用户ID", required = true, example = "1001")
    @PathVariable Long id
) { }
```

### @ApiResponse - 响应描述
```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "删除成功"),
    @ApiResponse(responseCode = "404", description = "用户不存在")
})
@DeleteMapping("/{id}")
public Result<Void> deleteUser(@PathVariable Long id) { }
```

### @Schema - 数据模型描述
```java
@Schema(description = "用户信息")
public class UserVO {
    @Schema(description = "用户ID", example = "1001")
    private Long id;
}
```

## 枚举类型处理最佳实践

在实际项目中，为了避免网络传输序列化问题和解耦依赖，我们通常**不在 DTO 中直接使用枚举类型**。

### 推荐做法：基础类型 + @Schema 说明
```java
@Schema(description = "创建角色请求")
public class CreateRoleRequest {
    
    /**
     * 用户类型（使用 Integer 存储，对应 UserType 枚举的 value 值）
     * @see com.youyu.framework.context.UserType
     */
    @NotNull(message = "用户类型不能为空")
    @Schema(
        description = "用户类型",
        allowableValues = {"0", "1", "2", "3"},
        example = "1",
        implementation = UserType.class  // 关键：指向枚举类，生成文档时显示枚举选项
    )
    private Integer userType;
}
```

### 对应的枚举定义（framework 模块）
```java
@Getter
public enum UserType {
    UNKNOWN("unknown", "未知用户类型"),
    CUSTOMER("customer", "外部顾客"),
    ENTERPRISE("enterprise", "企业员工"),
    PLATFORM("platform", "平台管理员");

    @EnumValue  // MyBatis-Plus 使用此字段存储到数据库
    private final Integer value;
    private final String code;
    private final String description;

    UserType(String code, String description) {
        this.value = ordinal(); 
        this.code = code;
        this.description = description;
    }
}
```

### 优势说明
1. ✅ **API 文档清晰**: Swagger/Knife4j 会自动显示枚举的可选项
2. ✅ **解耦依赖**: API 模块不需要依赖 MyBatis-Plus
3. ✅ **传输效率高**: 使用 Integer 传输，体积小，序列化快
4. ✅ **数据库友好**: DO 层可以使用 `@EnumValue` 自动转换
5. ✅ **类型安全**: 通过 `@see` 注释和 `implementation` 属性保持关联

## 完整实战流程

1. **前端请求**: `{status: 0}`
2. **API 层 (DTO)**: `Integer status` (@Schema 指向枚举)
3. **应用服务层**: `OrderStatus.of(0)` 转换为枚举进行业务逻辑处理
4. **领域层**: 使用枚举保证业务规则
5. **DO 层**: `Integer status` (MyBatis-Plus 自动与枚举转换)
6. **数据库**: 存储 `0`

---

**文档版本**: 1.0  
**最后更新**: 2026-05-07  
**维护者**: Alan
