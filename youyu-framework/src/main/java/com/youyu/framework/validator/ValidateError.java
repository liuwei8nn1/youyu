package com.youyu.framework.validator;

import java.text.MessageFormat;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 验证错误消息类
 * <p>
 * 封装验证失败时的错误信息，支持国际化和参数化。
 * 使用延迟生成策略，只有在真正需要展示错误时才计算消息内容。
 * <p>
 * 错误消息格式：
 * - 以 "@" 开头的 key 会被视为国际化 key，需要通过 errorResolver 解析
 * - 其他字符串直接作为错误消息返回
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 定义错误常量
 * public class ValidateError {
 *     public static final String required = "@validate.required";
 *     public static final String email_invalid = "@validate.email.invalid";
 * }
 * 
 * // 创建错误消息（带参数）
 * Supplier<String> error = new ValidateError(ValidateError.required, "用户名");
 * 
 * // 获取错误消息（此时才会进行国际化解析）
 * String message = error.get();  // "用户名不能为空"
 * }</pre>
 *
 * @see ValidateException 验证异常
 * @since 1.0
 */
public class ValidateError implements Supplier<String> {

	/** 必填验证失败 */
	public static final String required = "@validate.required",
	/** 通用无效验证失败 */
	invalid = "@validate.invalid",
	/** 格式无效验证失败 */
	format_invalid = "@validate.format.invalid",
	/** 邮箱格式无效 */
	email_invalid = format_invalid,
	/** 真实姓名格式无效 */
	real_name_invalid = "@validate.real_name.invalid",
	/** 范围验证：小于等于最大值失败 */
	range_le_max_invalid = "@validate.range.le.invalid",
	/** 范围验证：小于最大值失败 */
	range_lt_max_invalid = "@validate.range.lt.invalid",
	/** 范围验证：大于等于最小值失败 */
	range_ge_min_invalid = "@validate.range.ge.invalid",
	/** 范围验证：大于最小值失败 */
	range_gt_min_invalid = "@validate.range.gt.invalid",
	/** 范围验证：小数位数超出限制 */
	range_gt_scale_invalid = "@validate.range.scale.invalid",
	/** 时间验证：早于最小时间 */
	time_min_invalid = "@validate.time.min.invalid",
	/** 时间验证：晚于最大时间 */
	time_max_invalid = "@validate.time.max.invalid",
	/** 当前时间标记 */
	now = "@now",
	/** 字符串长度：小于最小长度 */
	size_string_min_invalid = "@validate.size.string.min.invalid",
	/** 字符串长度：超过最大长度 */
	size_string_max_invalid = "@validate.size.string.max.invalid",
	/** 集合大小：小于最小数量 */
	size_items_min_invalid = "@validate.size.items.min.invalid",
	/** 集合大小：超过最大数量 */
	size_items_max_invalid = "@validate.size.items.max.invalid";

	/** 错误消息 key（可能包含国际化前缀 "@"） */
	protected String key;
	/** 错误消息参数（第一个参数通常是字段标签） */
	protected Object[] args;

	/**
	 * 错误消息解析器
	 * <p>
	 * 默认使用 MessageFormat 进行格式化，可以自定义以支持国际化框架
	 */
	static BiFunction<String, Object[], String> errorResolver = MessageFormat::format;

	/**
	 * 构造空错误消息
	 */
	public ValidateError() {
	}

	/**
	 * 构造错误消息
	 *
	 * @param key  错误消息 key（以 "@" 开头表示需要国际化）
	 * @param args 错误消息参数（第一个参数通常是字段标签）
	 */
	public ValidateError(String key, Object... args) {
		this.key = key;
		this.args = args;
	}

	/**
	 * 设置错误消息解析器
	 * <p>
	 * 可以自定义解析器以支持不同的国际化框架
	 *
	 * @param errorResolver 新的解析器
	 */
	public static void setErrorResolver(BiFunction<String, Object[], String> errorResolver) {
		ValidateError.errorResolver = errorResolver;
	}

	/**
	 * 获取错误消息（延迟生成）
	 * <p>
	 * 只有在调用此方法时才会真正解析错误消息
	 *
	 * @return 解析后的错误消息
	 */
	public String get() {
		return resolveError(key, args);
	}

	/**
	 * 解析错误消息
	 * <p>
	 * 如果 key 以 "@" 开头，则通过 errorResolver 进行国际化解析
	 * 第一个参数（label）也会进行国际化处理
	 *
	 * @param key  错误消息 key
	 * @param args 错误消息参数
	 * @return 解析后的错误消息
	 */
	public static String resolveError(String key, Object... args) {
		if (args != null && args.length > 0) { // 第一个是 label，一般也需要国际化
			args[0] = errorResolver.apply((String) args[0], ArrayUtils.EMPTY_OBJECT_ARRAY);
		}
		return errorResolver.apply(key, args);
	}

	/**
	 * 尝试解析错误 key
	 * <p>
	 * 如果 key 以 "@" 开头，则进行国际化解析；否则直接返回原值
	 *
	 * @param key 错误消息 key
	 * @return 解析后的消息或原值
	 */
	public static String tryResolveKey(String key) {
		if (key != null && key.startsWith("@")) {
			return resolveError(key, ArrayUtils.EMPTY_OBJECT_ARRAY);
		}
		return key;
	}

	/**
	 * 解析必填错误消息
	 * <p>
	 * 快捷方法，用于生成“XXX不能为空”的错误消息
	 *
	 * @param label 字段标签
	 * @return 错误消息
	 */
	public static String resolveRequiredError(String label) {
		return resolveError(required, label);
	}

}
