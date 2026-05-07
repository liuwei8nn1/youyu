package com.youyu.framework.validator;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 邮箱验证注解
 * <p>
 * 验证字符串是否符合邮箱格式。
 * 使用正则表达式：^\w+(?:\.?[\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\.[a-zA-Z]+$
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @Email
 *     private String email;  // user@example.com
 * }
 * }</pre>
 *
 * @see ValidateHelper#EMAIL_MATCHER 邮箱匹配器
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Email.EmailValidator.class)
public @interface Email {

	/**
	 * 邮箱验证器
	 */
	class EmailValidator extends AbstractRuleValidator<Email, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return ValidateHelper.EMAIL_MATCHER.test(val) ? null : ValidateError.email_invalid;
		}

	}

}
