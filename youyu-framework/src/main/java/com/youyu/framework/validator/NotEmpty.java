package com.youyu.framework.validator;

import java.lang.annotation.*;

import com.youyu.common.util.StringUtil;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 非空验证注解
 * <p>
 * 验证字符串不能为 null 或空字符串（""）。
 * 注意：只检查是否为空，不会去除空格。
 * <p>
 * 执行顺序：ORDER_PRE_VALIDATE（预校验阶段）
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @NotEmpty
 *     private String username;  // 不能为 null 或 ""
 * }
 * }</pre>
 *
 * @see Trim 去除空格注解
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(NotEmpty.NotEmptyValidator.class)
public @interface NotEmpty {

	/**
	 * 非空验证器
	 * <p>
	 * 在预校验阶段执行，确保字段不为空
	 */
	class NotEmptyValidator extends AbstractRuleValidator<NotEmpty, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return StringUtil.notEmpty(val) ? null : ValidateError.required;
		}

		@Override
		public int getOrder() {
			return ORDER_PRE_VALIDATE;
		}

	}

}
