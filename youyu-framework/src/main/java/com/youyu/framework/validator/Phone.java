package com.youyu.framework.validator;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 手机号验证注解
 * <p>
 * 验证字符串是否符合中国大陆手机号格式。
 * 规则：11位数字，以1开头
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @Phone
 *     private String phone;  // 13800138000
 * }
 * }</pre>
 *
 * @see ValidateHelper#PHONE_MATCHER 手机号匹配器
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Phone.PhoneValidator.class)
public @interface Phone {

	/**
	 * 手机号验证器
	 */
	class PhoneValidator extends AbstractRuleValidator<Phone, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return ValidateHelper.PHONE_MATCHER.test(val) ? null : ValidateError.format_invalid;
		}

	}

}
