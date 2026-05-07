package com.youyu.framework.validator;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 正则表达式验证注解
 * <p>
 * 使用自定义正则表达式验证字符串格式。
 * 支持两种模式：
 * 1. 纯验证：检查字符串是否匹配正则表达式
 * 2. 替换模式：将匹配的部分替换为指定字符串
 * <p>
 * 执行顺序：
 * - set=true: ORDER_PRE + 10（预处理阶段）
 * - set=false: ORDER_DEFAULT（正常校验阶段）
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @RegExp("^[a-zA-Z0-9_]{3,20}$")
 *     private String username;  // 只允许字母、数字、下划线，3-20位
 *     
 *     @RegExp(value = "\\s+", replace = "")  // 去除所有空白字符
 *     private String code;
 * }
 * }</pre>
 *
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(RegExp.RegExpValidator.class)
public @interface RegExp {

	/**
	 * 正则表达式
	 *
	 * @return 正则表达式字符串
	 */
	String value();

	/**
	 * 替换字符串
	 * <p>
	 * 默认值 "\0" 表示不进行替换，只做验证
	 * 如果设置了其他值，则将匹配的部分替换为该字符串
	 *
	 * @return 替换字符串
	 */
	String replace() default "\0";

	/**
	 * 是否将替换后的值设置回原字段
	 * <p>
	 * true: 会修改原对象的字段值（默认）
	 * false: 只验证，不修改
	 *
	 * @return 是否设置回字段
	 */
	boolean set() default true;

	/**
	 * 正则表达式标志位
	 * <p>
	 * 如 Pattern.CASE_INSENSITIVE（忽略大小写）
	 *
	 * @return 标志位
	 * @see java.util.regex.Pattern 标志位常量
	 */
	int flags() default 0;

	/**
	 * 正则表达式验证器
	 * <p>
	 * 在初始化时编译正则表达式，提高验证性能
	 */
	class RegExpValidator extends AbstractRuleValidator<RegExp, String> {

		protected RegExp rule;
		protected Pattern pattern;

		@Override
		public void init(RegExp rule) {
			this.rule = rule;
			pattern = Pattern.compile(rule.value(), rule.flags());
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			String replace = rule.replace();
			if ("\0".equals(replace)) {
				return pattern.matcher(val).matches() ? null : ValidateError.format_invalid;
			} else {
				context.setCurrentValue(pattern.matcher(val).replaceAll(replace), rule.set());
				return null;
			}
		}

		@Override
		public int getOrder() {
			return rule.set() ? ORDER_PRE + 10 : ORDER_DEFAULT;
		}

	}

}
