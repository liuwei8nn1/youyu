package com.youyu.framework.validator;

import java.lang.annotation.*;

/**
 * 字段检查注解
 * <p>
 * 用于标记需要验证的字段，提供基本的验证配置。
 * 通常与其他验证规则注解（如 @NotEmpty, @Email 等）配合使用。
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @Check(value = "用户名", required = true)
 *     @NotEmpty
 *     private String username;
 * }
 * }</pre>
 *
 * @see NotEmpty 非空验证
 * @see Email 邮箱验证
 * @since 1.0
 */
@Documented
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Check {

	/**
	 * 字段显示名称（用于错误提示）
	 * <p>
	 * 例如：value="用户名"，验证失败时提示“用户名不能为空”
	 *
	 * @return 字段名称
	 */
	String value() default "";

	/**
	 * 是否必填
	 * <p>
	 * true: 字段不能为 null 或空
	 * false: 字段可以为 null 或空
	 *
	 * @return 是否必填
	 */
	boolean required() default true;

	/**
	 * 是否启用国际化
	 * <p>
	 * true: 错误消息会通过 i18n 解析
	 * false: 直接使用原始消息
	 *
	 * @return 是否启用国际化
	 */
	boolean i18n() default true;

}
