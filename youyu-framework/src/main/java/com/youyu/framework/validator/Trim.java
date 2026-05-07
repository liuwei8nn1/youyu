package com.youyu.framework.validator;

import java.lang.annotation.*;

import com.youyu.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 去除空格注解
 * <p>
 * 自动去除字符串首尾的空格，或去除所有空白字符。
 * 这是一个数据清洗注解，会在验证前修改字段值。
 * <p>
 * 执行顺序：ORDER_PRE - 10（预处理阶段，最先执行）
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @Trim  // 去除首尾空格
 *     private String username;  // "  john  " -> "john"
 *     
 *     @Trim(all = true)  // 去除所有空白字符
 *     private String code;  // "a b c" -> "abc"
 * }
 * }</pre>
 *
 * @see NotEmpty 非空验证（常配合使用）
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Trim.TrimValidator.class)
public @interface Trim {

	/**
	 * 是否去除所有空白字符
	 * <p>
	 * false: 只去除首尾空格（默认）
	 * true: 去除所有空白字符（包括中间的空格、制表符等）
	 *
	 * @return 是否去除所有空白
	 */
	boolean all() default false;

	/**
	 * 是否将处理后的值设置回原字段
	 * <p>
	 * true: 会修改原对象的字段值（默认）
	 * false: 只更新验证上下文中的值
	 *
	 * @return 是否设置回字段
	 */
	boolean set() default true;

	/**
	 * 空格去除验证器
	 * <p>
	 * 在预处理阶段执行，确保后续验证使用的是清洗后的数据
	 */
	class TrimValidator extends AbstractRuleValidator<Trim, String> {

		protected Trim rule;

		@Override
		public void init(Trim rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			String newVal = rule.all() ? StringUtils.deleteWhitespace(val) : StringUtil.trim(val);
			//noinspection StringEquality
			context.setCurrentValue(newVal, val != newVal && rule.set());
			return null;
		}

		@Override
		public int getOrder() {
			return ORDER_PRE - 10;
		}

	}

}