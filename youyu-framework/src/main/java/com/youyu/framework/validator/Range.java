package com.youyu.framework.validator;

import java.lang.annotation.*;
import java.math.BigDecimal;
import java.util.function.Supplier;

import com.youyu.common.util.Arith;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数值范围验证注解
 * <p>
 * 验证数值是否在指定范围内，支持整数和浮点数。
 * 可以配置是否包含边界值，以及小数位数限制。
 * <p>
 * 支持类型：
 * - 整数：Integer, Long, Short, Byte
 * - 浮点数：Float, Double, BigDecimal
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class ProductForm {
 *     @Range(min = 1, max = 9999)
 *     private Integer stock;  // 库存 1-9999
 *     
 *     @Range(min = 0.01, max = 99999.99, maxScale = 2)
 *     private BigDecimal price;  // 价格 0.01-99999.99，最多2位小数
 *     
 *     @Range(min = 18, max = 100, canEqMin = true, canEqMax = true)
 *     private Integer age;  // 年龄 18-100（包含边界）
 * }
 * }</pre>
 *
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Range.RangeValidator.class)
public @interface Range {

	/**
	 * 最小值
	 *
	 * @return 最小值
	 */
	double min() default Double.MIN_VALUE;

	/**
	 * 最大值
	 *
	 * @return 最大值
	 */
	double max() default Double.MAX_VALUE;

	/**
	 * 是否可以等于最小值
	 * <p>
	 * true: 包含最小值（>=）
	 * false: 不包含最小值（>）
	 *
	 * @return 是否可以等于最小值
	 */
	boolean canEqMin() default true;

	/**
	 * 是否可以等于最大值
	 * <p>
	 * true: 包含最大值（<=）
	 * false: 不包含最大值（<）
	 *
	 * @return 是否可以等于最大值
	 */
	boolean canEqMax() default true;

	/**
	 * 最多允许输入几位小数
	 * <p>
	 * -1: 不限制小数位数（默认）
	 * >=0: 限制小数位数（仅对浮点数有效）
	 * <p>
	 * 注意：当字段为整型时，该设置无效
	 *
	 * @return 最大小数位数
	 */
	int maxScale() default -1;

	/**
	 * 数值范围验证器
	 * <p>
	 * 支持整数和浮点数的范围验证，自动处理边界条件和小数位数
	 */
	class RangeValidator extends AbstractRuleValidator<Range, Number> {

		protected Range rule;
		protected Number min, max;

		@Override
		public void init(Range rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(Number val, ValidateContext context) {
			return null;
		}

		@Override
		protected Supplier<String> validateInternal(Number val, String label, ValidateContext context) {
			Class<? extends Number> clazz = val.getClass();
			if (clazz == Integer.class || clazz == Long.class || clazz == Short.class || clazz == Byte.class) {
				if (min == null) {
					min = (long) rule.min();
					max = (long) rule.max();
				}
				final Long v = clazz == Long.class ? (Long) val : val.longValue();
				return checkRange(label, v, (Long) min, (Long) max, rule.canEqMin(), rule.canEqMax(), -1);
			}
			if (min == null) {
				min = Arith.toBigDecimal(rule.min());
				max = Arith.toBigDecimal(rule.max());
			}
			BigDecimal v = clazz == BigDecimal.class ? (BigDecimal) val : new BigDecimal(val.toString());
			return checkRange(label, v, (BigDecimal) min, (BigDecimal) max, rule.canEqMin(), rule.canEqMax(), rule.maxScale());
		}

		public static <T extends Comparable<T>> Supplier<String> checkRange(final String label, final T val, final T min, final T max,
		                                                                    final boolean canEqMin, final boolean canEqMax, final int maxScale) {
			int cmp = val.compareTo(min);
			// min
			if (cmp < 0 || (cmp == 0 && !canEqMin)) {
				// return () -> label + (canEqMin ? "不能小于 " : "必须大于 ") + min;
				return new ValidateError(canEqMin ? ValidateError.range_ge_min_invalid : ValidateError.range_gt_min_invalid, label, min);
			}
			// max
			cmp = val.compareTo(max);
			if (cmp > 0 || (cmp == 0 && !canEqMax)) {
				// return () -> label + (canEqMax ? "不能大于 " : "必须小于 ") + max;
				return new ValidateError(canEqMax ? ValidateError.range_le_max_invalid : ValidateError.range_lt_max_invalid, label, max);
			}
			if (maxScale >= 0 && val instanceof BigDecimal v) {
				v = v.stripTrailingZeros();
				if (maxScale < v.scale()) {
					// return () -> label + "最多只能输入" + maxScale + "位小数";
					return new ValidateError(ValidateError.range_gt_scale_invalid, label, maxScale);
				}
			}
			return null;
		}

	}

}