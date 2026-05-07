package com.youyu.framework.validator;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 可选字段注解
 * <p>
 * 标记字段为可选，允许为 null 或空字符串。
 * 如果字段为空，则跳过后续的所有验证规则。
 * <p>
 * 执行顺序：ORDER_PRE_VALIDATE - 10（在预校验之前执行）
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserQueryForm {
 *     @Optional  // 可选参数，可以为 null
 *     @Size(min = 2, max = 20)
 *     private String keyword;
 * }
 * </pre>
 *
 * @see NotEmpty 非空验证（相反）
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Optional.OptionalValidator.class)
public @interface Optional {

	/**
	 * 可选字段验证器
	 * <p>
	 * 如果值为 null 或空字符串，返回 YES_BREAK 中断后续验证
	 */
	class OptionalValidator implements RuleValidator<Optional, Object> {

		@Override
		public Result validate(Object val, ValidateContext context) {
			if (val == null || (val instanceof String && val.toString().isEmpty())) {
				return Result.YES_BREAK;  // 为空则跳过后续验证
			}
			return Result.YES;  // 不为空则继续验证
		}

		@Override
		public int getOrder() {
			return ORDER_PRE_VALIDATE - 10;  // 最先执行
		}

		@Override
		public boolean notNull() {
			return false;  // 允许为 null
		}
	}

}
