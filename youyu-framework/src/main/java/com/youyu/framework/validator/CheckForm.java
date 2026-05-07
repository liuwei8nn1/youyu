package com.youyu.framework.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 表单验证注解
 * <p>
 * 标记一个类为可验证的表单，触发整个对象的验证流程。
 * 这是验证框架的入口注解，所有需要验证的 DTO/VO 都应该添加此注解。
 * <p>
 * 验证流程：
 * 1. Spring Validation 检测到 @CheckForm 注解
 * 2. 调用 FormValidator 进行验证
 * 3. FormValidator 遍历所有字段，应用对应的验证规则
 * 4. 收集所有错误并抛出 ValidateException
 * <p>
 * 使用示例：
 * <pre>{@code
 * @CheckForm(label = "用户注册")
 * public class UserRegisterForm implements Form {
 *     @NotEmpty
 *     private String username;
 *     
 *     @Email
 *     private String email;
 * }
 * 
 * // Controller 中使用
 * public Result register(@Valid @RequestBody UserRegisterForm form) {
 *     // 如果验证失败，会自动抛出 ValidateException
 * }
 * }</pre>
 *
 * @see FormValidator 表单验证器
 * @see Form 表单接口
 * @since 1.0
 */
@Documented
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FormValidator.class)
public @interface CheckForm {

	/**
	 * 验证失败时的默认错误消息
	 * <p>
	 * 通常不需要修改，具体字段的错误消息由各自的验证规则决定
	 *
	 * @return 错误消息模板
	 */
	String message() default ValidateError.invalid;

	/**
	 * 验证分组（用于分组验证）
	 * <p>
	 * 可以定义不同的验证场景，如：创建、更新等
	 *
	 * @return 验证分组数组
	 */
	Class<?>[] groups() default {};

	/**
	 * 验证负载（用于传递额外信息）
	 * <p>
	 * 高级功能，一般不需要使用
	 *
	 * @return 负载数组
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * 表单对象本身是否不能为 null
	 * <p>
	 * true: 表单对象不能为 null
	 * false: 允许表单对象为 null
	 *
	 * @return 是否非空
	 */
	boolean notNull() default true;

	/**
	 * 表单显示标签（用于错误提示）
	 * <p>
	 * 例如：label="用户注册"，验证失败时可能提示“用户注册数据验证失败”
	 *
	 * @return 表单标签
	 */
	String label();

}
