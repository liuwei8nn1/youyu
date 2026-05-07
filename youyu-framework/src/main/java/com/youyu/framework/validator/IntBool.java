package com.youyu.framework.validator;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 整数布尔值验证注解
 * <p>
 * 验证整数值是否为有效的布尔表示（0 或 1）。
 * 适用于数据库中用 tinyint 存储的布尔字段。
 * <p>
 * 支持类型：Integer, Long, Short, Byte
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @IntBool
 *     private Integer status;  // 0=禁用, 1=启用
 *     
 *     @IntBool
 *     private Byte isDeleted;  // 0=未删除, 1=已删除
 * }
 * }</pre>
 *
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(IntBool.IntBoolValidator.class)
public @interface IntBool {

	/**
	 * 整数布尔值验证器
	 */
	class IntBoolValidator extends AbstractRuleValidator<IntBool, Number> {

		@Override
		protected String validateInternal(Number val, ValidateContext context) {
			if (val instanceof Integer || val instanceof Long || val instanceof Short || val instanceof Byte) {
				int v = val.intValue();
				if (v == 1 || v == 0) {
					return null;
				}
			}
			return ValidateError.invalid;
		}

	}

}
