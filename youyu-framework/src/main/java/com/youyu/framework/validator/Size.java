package com.youyu.framework.validator;

import java.lang.annotation.*;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 长度/大小验证注解
 * <p>
 * 验证字符串长度、集合大小、Map 大小或数组长度是否在指定范围内。
 * <p>
 * 支持类型：
 * - String: 验证字符长度
 * - Collection: 验证元素数量
 * - Map: 验证键值对数量
 * - Array: 验证数组长度
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserForm {
 *     @Size(min = 3, max = 20)
 *     private String username;  // 用户名长度 3-20 个字符
 *     
 *     @Size(min = 1, max = 5)
 *     private List<String> tags;  // 标签数量 1-5 个
 * }
 * }</pre>
 *
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Size.SizeValidator.class)
public @interface Size {

	/**
	 * 最小长度/大小
	 *
	 * @return 最小值
	 */
	int min() default 0;

	/**
	 * 最大长度/大小
	 *
	 * @return 最大值
	 */
	int max() default Integer.MAX_VALUE;

	/**
	 * 长度/大小验证器
	 * <p>
	 * 根据值的类型自动选择验证逻辑
	 */
	class SizeValidator extends AbstractRuleValidator<Size, Object> {

		protected Size rule;

		@Override
		public void init(Size rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(Object val, ValidateContext context) {
			return null;
		}

		@Override
		protected Supplier<String> validateInternal(Object val, String label, ValidateContext context) {
			if (val instanceof String) {
				int length = ((String) val).length();
				return makeError(length, label, true);
			} else if (val instanceof Collection) {
				return makeError(((Collection<?>) val).size(), label, false);
			} else if (val instanceof Map) {
				return makeError(((Map<?, ?>) val).size(), label, false);
			} else if (val.getClass().isArray()) {
				return makeError(Array.getLength(val), label, false);
			} else {
				throw new UnsupportedOperationException("参数校验出错，请重新输入！");
			}
		}

		protected Supplier<String> makeError(int val, String label, boolean charOrItem) {
			if (val < rule.min()) {
				// return () -> label + noun + "不能小于 " + rule.min();
				return new ValidateError(charOrItem ? ValidateError.size_string_min_invalid : ValidateError.size_items_min_invalid, label, rule.min());
			}
			if (val > rule.max()) {
				// return () -> label + noun + "不能大于 " + rule.max();
				return new ValidateError(charOrItem ? ValidateError.size_string_max_invalid : ValidateError.size_items_max_invalid, label, rule.max());
			}
			return null;
		}

	}

}
