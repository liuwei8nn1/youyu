package com.youyu.framework.validator;

import java.lang.annotation.*;
import java.util.Date;
import java.util.function.Supplier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 时间范围验证注解
 * <p>
 * 验证日期是否在指定的时间范围内。
 * 支持固定日期和特殊标记（如 "now" 表示当前时间）。
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class EventForm {
 *     @TimeRange(min = "2024-01-01", max = "2024-12-31")
 *     private Date eventDate;  // 事件日期必须在 2024 年内
 *     
 *     @TimeRange(min = "now")  // 不能早于当前时间
 *     private Date startTime;
 *     
 *     @TimeRange(max = "now", maxTitle = "@current.time")  // 不能晚于当前时间
 *     private Date birthDate;
 * }
 * }</pre>
 *
 * @see ValidateHelper#DATE_PARSER 日期解析器
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(TimeRange.TimeRangeValidator.class)
public @interface TimeRange {

	/**
	 * 最小时间（日期字符串或 "now"）
	 * <p>
	 * 空字符串表示不限制
	 *
	 * @return 最小时间字符串
	 */
	String min() default "";

	/**
	 * 最小时间的显示标题（用于错误提示）
	 * <p>
	 * 如果为空，则使用 min 的值
	 *
	 * @return 最小时间标题
	 */
	String minTitle() default "";

	/**
	 * 最大时间（日期字符串或 "now"）
	 * <p>
	 * 空字符串表示不限制
	 *
	 * @return 最大时间字符串
	 */
	String max() default "";

	/**
	 * 最大时间的显示标题（用于错误提示）
	 * <p>
	 * 如果为空，则使用 max 的值
	 *
	 * @return 最大时间标题
	 */
	String maxTitle() default "";

	/** 当前时间标记 */
	String NOW = "now";

	/**
	 * 时间范围验证器
	 * <p>
	 * 支持固定日期和动态日期（now）
	 */
	class TimeRangeValidator extends AbstractRuleValidator<TimeRange, Date> {

		protected TimeRange rule;
		protected Date min, max;

		@Override
		public void init(TimeRange rule) {
			this.rule = rule;
			min = parse(rule.min());
			max = parse(rule.max());
		}

		protected Date parse(String dateStr) {
			if (dateStr.isEmpty() || NOW.equals(dateStr)) {
				return null;
			}
			return ValidateHelper.DATE_PARSER.apply(dateStr);
		}

		@Override
		protected String validateInternal(Date val, ValidateContext context) {
			return null;
		}

		@Override
		protected Supplier<String> validateInternal(Date val, String label, ValidateContext context) {
			long time = val.getTime();
			long now = System.currentTimeMillis();
			Date min = getMutableDate(this.min, now, rule.min()),
					max = getMutableDate(this.max, now, rule.max());

			// {0} must be not less than {1}
			if (min != null && time < min.getTime()) {
				// return () -> label + "不能小于" + semanticDate(rule.min(), rule.minTitle());
				return new ValidateError(ValidateError.time_min_invalid, label, semanticDate(rule.min(), rule.minTitle()));
			}
			if (max != null && time > max.getTime()) {
				// return () -> label + "不能大于" + semanticDate(rule.max(), rule.maxTitle());
				return new ValidateError(ValidateError.time_max_invalid, label, semanticDate(rule.max(), rule.maxTitle()));
			}
			return null;
		}

		private String semanticDate(String dateStr, String title) {
			if (!title.isEmpty()) {
				return ValidateError.tryResolveKey(title);
			}
			return NOW.equals(dateStr) ? ValidateError.tryResolveKey(ValidateError.now) : ValidateError.tryResolveKey(dateStr);
		}

		private Date getMutableDate(Date date, long now, String dateStr) {
			if (date == null && NOW.equals(dateStr)) {
				date = new Date(now);
			}
			return date;
		}

	}

}
