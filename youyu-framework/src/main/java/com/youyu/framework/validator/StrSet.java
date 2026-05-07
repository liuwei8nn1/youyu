package com.youyu.framework.validator;

import java.lang.annotation.*;
import java.util.Set;

import com.youyu.common.util.CollectionUtil;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 字符串集合验证注解
 * <p>
 * 验证字符串值是否在指定的集合中。
 * 适用于枚举类型的字符串表示或固定的选项列表。
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @StrSet({"male", "female", "other"})
 *     private String gender;  // 性别只能是 male/female/other
 *     
 *     @StrSet({"active", "inactive", "pending"})
 *     private String status;  // 状态只能是 active/inactive/pending
 * }
 * }</pre>
 *
 * @see IntSet 整数集合验证
 * @see EnumSet 枚举集合验证
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(StrSet.StrSetValidator.class)
public @interface StrSet {

	/**
	 * 允许的字符串值集合
	 *
	 * @return 字符串数组
	 */
	String[] value();

	/**
	 * 字符串集合验证器
	 * <p>
	 * 在初始化时将数组转换为 HashSet，提高查找效率
	 */
	class StrSetValidator extends AbstractRuleValidator<StrSet, String> {

		protected Set<String> range;

		@Override
		public void init(StrSet rule) {
			String[] values = rule.value();
			range = CollectionUtil.asHashSet(values);
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return range.contains(val) ? null : ValidateError.invalid;
		}

	}

}