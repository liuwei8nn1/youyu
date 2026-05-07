package com.youyu.framework.validator;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 真实姓名验证注解
 * <p>
 * 验证字符串是否符合中文真实姓名格式。
 * 规则：2-6位中文字符
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @RealName
 *     private String realName;  // 张三、欧阳娜娜
 * }
 * }</pre>
 *
 * @see ValidateHelper#REAL_NAME_MATCHER 真实姓名匹配器
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(RealName.RealNameValidator.class)
public @interface RealName {

	/**
	 * 真实姓名验证器
	 */
	class RealNameValidator extends AbstractRuleValidator<RealName, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			boolean result = ValidateHelper.REAL_NAME_MATCHER.test(val);
			return result ? null : ValidateError.real_name_invalid;
		}

	}

}
