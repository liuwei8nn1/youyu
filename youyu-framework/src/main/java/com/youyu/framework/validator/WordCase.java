package com.youyu.framework.validator;

import java.lang.annotation.*;

import com.youyu.common.util.StringUtil;
import jakarta.validation.ConstraintDeclarationException;
import org.apache.commons.lang3.StringUtils;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 单词大小写验证/转换注解
 * <p>
 * 验证或转换字符串的大小写格式。
 * 支持四种模式：
 * - lower: 全部小写
 * - upper: 全部大写
 * - ucfirst: 首字母大写
 * - lcfirst: 首字母小写
 * <p>
 * 执行顺序：
 * - set=true: ORDER_PRE（预处理阶段，会修改值）
 * - set=false: ORDER_DEFAULT（正常校验阶段，只验证）
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @WordCase("lower")  // 转换为小写
 *     private String email;  // "John@Example.COM" -> "john@example.com"
 *     
 *     @WordCase("ucfirst")  // 首字母大写
 *     private String name;  // "john" -> "John"
 *     
 *     @WordCase(value = "upper", set = false)  // 只验证，不修改
 *     private String code;  // 必须全部大写
 * }
 * }</pre>
 *
 * @see StringUtil#capitalize(String) 首字母大写
 * @see StringUtil#decapitalize(String) 首字母小写
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(WordCase.WordCaseValidator.class)
public @interface WordCase {

	/** 小写 */
	String LOWER = "lower",
	/** 大写 */
	UPPER = "upper",
	/** 首字母大写 */
	UCFIRST = "ucfirst",
	/** 首字母小写 */
	LCFIRST = "lcfirst";

	/**
	 * 大小写模式
	 * <p>
	 * 可选值：LOWER, UPPER, UCFIRST, LCFIRST
	 *
	 * @return 大小写模式
	 */
	String value();

	/**
	 * 是否将转换后的值设置回原字段
	 * <p>
	 * true: 会修改原对象的字段值（默认）
	 * false: 只验证格式，不修改
	 *
	 * @return 是否设置回字段
	 */
	boolean set() default true;

	/**
	 * 单词大小写验证器
	 * <p>
	 * 根据 set 参数决定是转换还是验证
	 */
	class WordCaseValidator extends AbstractRuleValidator<WordCase, String> {

		protected WordCase rule;

		@Override
		public void init(WordCase rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			if (rule.set()) {
				switch (rule.value()) {
					case LOWER:
						return val.toLowerCase();
					case UPPER:
						return val.toUpperCase();
					case UCFIRST:
						return StringUtil.capitalize(val);
					case LCFIRST:
						return StringUtil.decapitalize(val);
				}
				throw new ConstraintDeclarationException("不支持的格式转换操作：" + rule.value());
			}
			// TODO 未使用到，暂不国际化
			switch (rule.value()) {
				case LOWER:
					return StringUtils.isAllLowerCase(val) ? null : "{0}必须全部小写！";
				case UPPER:
					return StringUtils.isAllUpperCase(val) ? null : "{0}必须全部大写！";
				case UCFIRST:
					return Character.isUpperCase(val.charAt(0)) ? null : "{0}首字母必须大写！";
				case LCFIRST:
					return Character.isLowerCase(val.charAt(0)) ? null : "{0}首字母必须小写！";
			}
			throw new ConstraintDeclarationException("无法识别指定格式：" + rule.value());
		}

		@Override
		public int getOrder() {
			return rule.set() ? ORDER_PRE : ORDER_DEFAULT;
		}

	}

}
