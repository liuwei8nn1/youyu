package com.youyu.framework.validator;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * 抽象规则验证器基类
 * <p>
 * 提供了验证器的通用实现，简化了具体验证器的开发。
 * 子类只需实现 {@link #validateInternal(Object, ValidateContext)} 方法即可。
 * <p>
 * 主要功能：
 * 1. 统一处理验证结果和错误收集
 * 2. 自动构建错误消息（支持国际化）
 * 3. 提供两种验证方法签名（带/不带 label 参数）
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class NotEmptyValidator extends AbstractRuleValidator<NotEmpty, String> {
 *     @Override
 *     protected String validateInternal(String val, ValidateContext context) {
 *         return StringUtil.notEmpty(val) ? null : ValidateError.required;
 *     }
 * }
 * }</pre>
 *
 * @param <A> 验证规则注解类型
 * @param <T> 被验证值的类型
 * @see RuleValidator 验证器接口
 * @see ValidateContext 验证上下文
 * @since 1.0
 */
public abstract class AbstractRuleValidator<A extends Annotation, T> implements RuleValidator<A, T> {

	/**
	 * 执行内部验证逻辑（子类必须实现）
	 * <p>
	 * 返回值说明：
	 * - null: 验证通过
	 * - 非null: 验证失败，返回错误模板 key（如 ValidateError.required）
	 *
	 * @param val     被验证的值
	 * @param context 验证上下文
	 * @return 错误模板 key，验证通过返回 null
	 */
	@Nullable
	protected abstract String validateInternal(T val, ValidateContext context);

	/**
	 * 执行内部验证逻辑并构建错误消息
	 * <p>
	 * 默认实现会调用 {@link #validateInternal(Object, ValidateContext)}，
	 * 如果验证失败则构建带有 label 的错误消息
	 *
	 * @param val     被验证的值
	 * @param label   字段显示标签
	 * @param context 验证上下文
	 * @return 错误消息提供者，验证通过返回 null
	 */
	@Nullable
	protected Supplier<String> validateInternal(T val, String label, ValidateContext context) {
		String error = validateInternal(val, context);
		if (error != null) {
			return makeError(error, val, label, context);
		}
		return null;
	}

	/**
	 * 执行验证（最终方法，不可重写）
	 * <p>
	 * 验证流程：
	 * 1. 调用内部验证方法
	 * 2. 如果验证失败，将错误添加到上下文
	 * 3. 返回验证结果
	 *
	 * @param val     被验证的值
	 * @param context 验证上下文
	 * @return 验证结果（YES/NO/YES_BREAK）
	 */
	@Override
	public final RuleValidator.Result validate(T val, ValidateContext context) {
		Supplier<String> error = validateInternal(val, context.getCurrentLabel(), context);
		if (error == null) {
			return Result.YES;
		}
		context.addError(error);
		return Result.NO;
	}

	/**
	 * 构建错误消息提供者
	 * <p>
	 * 使用延迟生成策略，只有在真正需要展示错误时才计算消息内容
	 *
	 * @param errorTemplate 错误模板 key（如 ValidateError.required）
	 * @param val           被验证的值
	 * @param label         字段显示标签
	 * @param context       验证上下文
	 * @return 错误消息提供者
	 * @see ValidateError 错误消息类
	 */
	protected Supplier<String> makeError(String errorTemplate, T val, String label, ValidateContext context) {
		return new ValidateError(errorTemplate, label);
	}

}