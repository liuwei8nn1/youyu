package com.youyu.framework.validator;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 整数集合验证注解
 * <p>
 * 验证整数值是否在指定的集合中。
 * 适用于枚举类型的整数表示或固定的选项列表。
 * <p>
 * 支持类型：Integer, Long, Short
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class OrderForm {
 *     @IntSet({1, 2, 3, 4})
 *     private Integer status;  // 状态只能是 1/2/3/4
 *     
 *     @IntSet({0, 1})
 *     private Byte isPaid;  // 是否支付：0=未支付, 1=已支付
 * }
 * }</pre>
 *
 * @see StrSet 字符串集合验证
 * @see EnumSet 枚举集合验证
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(IntSet.IntSetValidator.class)
public @interface IntSet {

	/**
	 * 允许的整数值集合
	 * <p>
	 * 注意：不能有重复值，否则初始化时会抛出异常
	 *
	 * @return 整数数组
	 */
	int[] value();

	/**
	 * 整数集合验证器
	 * <p>
	 * 在初始化时检查是否有重复值，并转换为 HashSet 提高查找效率
	 */
	class IntSetValidator extends AbstractRuleValidator<IntSet, Number> {

		protected Set<Integer> range;

		@Override
		public void init(IntSet rule) {
			int[] values = rule.value();
			range = Arrays.stream(values).boxed().collect(Collectors.toSet());
			if (values.length != range.size()) {
				throw new ValidateException("@IntSet 注解定义有误，不能出现重复的枚举值！");
			}
		}

		@Override
		protected String validateInternal(Number val, ValidateContext context) {
			boolean inSet = false;
			if (val instanceof Integer) {
				inSet = range.contains(val);
			} else if (val instanceof Long || val instanceof Short) {
				inSet = range.contains(val.intValue());
			}
			return inSet ? null : ValidateError.invalid;
		}

	}

}
