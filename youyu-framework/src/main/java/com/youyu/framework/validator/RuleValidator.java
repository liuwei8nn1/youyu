package com.youyu.framework.validator;

import java.lang.annotation.Annotation;

/**
 * 规则验证器接口
 * <p>
 * 所有验证器的基础接口，定义了验证器的基本行为。
 * 验证器负责检查某个值是否符合特定的规则。
 * <p>
 * 验证器执行顺序：
 * 1. ORDER_BEGIN (0) - 最先执行
 * 2. ORDER_PRE (20) - 预处理
 * 3. ORDER_PRE_VALIDATE (50) - 预校验（如 @Optional, @NotEmpty）
 * 4. ORDER_DEFAULT (100) - 正常校验（默认）
 * 5. ORDER_END (10000) - 最后执行
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class NotEmptyValidator implements RuleValidator<NotEmpty, String> {
 *     @Override
 *     public Result validate(String val, ValidateContext context) {
 *         if (StringUtil.notEmpty(val)) {
 *             return Result.YES;  // 验证通过
 *         }
 *         context.addError(() -> "不能为空");
 *         return Result.NO;  // 验证失败
 *     }
 *     
 *     @Override
 *     public int getOrder() {
 *         return ORDER_PRE_VALIDATE;  // 在预校验阶段执行
 *     }
 * }
 * }</pre>
 *
 * @param <A> 验证规则注解类型
 * @param <T> 被验证值的类型
 * @see Rule 验证规则注解
 * @see ValidateContext 验证上下文
 * @since 1.0
 */
public interface RuleValidator<A extends Annotation, T> extends Comparable<RuleValidator<?, ?>> {

	/** 最先执行：用于初始化或特殊处理 */
	int ORDER_BEGIN = 0,
	/** 预处理：数据清洗、格式转换等 */
	ORDER_PRE = 20,
	/** 预校验：空值检查、必填检查等 */
	ORDER_PRE_VALIDATE = 50,
	/** 默认（正常校验）：大部分验证规则在此阶段执行 */
	ORDER_DEFAULT = 100,
	/** 最后收尾：最终检查、清理工作等 */
	ORDER_END = 10000;

	/**
	 * 初始化验证器
	 * <p>
	 * 在验证器创建后调用一次，用于解析注解参数、编译正则表达式等耗时操作。
	 * 此方法只会被调用一次，可以安全地执行耗时初始化。
	 *
	 * @param rule 验证规则注解实例
	 */
	default void init(A rule) {
	}

	/**
	 * 执行验证
	 * <p>
	 * 验证器的核心方法，检查给定的值是否符合规则。
	 *
	 * @param val     被验证的值
	 * @param context 验证上下文（可获取字段信息、添加错误等）
	 * @return 验证结果
	 *         - YES: 验证通过，继续执行下一个验证器
	 *         - NO: 验证失败，继续执行下一个验证器
	 *         - YES_BREAK: 验证通过，中断后续验证器执行
	 */
	Result validate(T val, ValidateContext context);

	/**
	 * 被验证的值是否允许为 null
	 * <p>
	 * true: 不允许为 null，null 值会直接验证失败
	 * false: 允许为 null，null 值会跳过验证（如 @Optional）
	 *
	 * @return 是否不允许为 null
	 */
	default boolean notNull() {
		return true;
	}

	/**
	 * 获取验证器的执行顺序
	 * <p>
	 * 值越小越先执行。可以通过重写此方法来控制验证器的执行顺序。
	 *
	 * @return 执行顺序值
	 * @see #ORDER_BEGIN
	 * @see #ORDER_PRE
	 * @see #ORDER_PRE_VALIDATE
	 * @see #ORDER_DEFAULT
	 * @see #ORDER_END
	 */
	default int getOrder() {
		return ORDER_DEFAULT;
	}

	/**
	 * 比较两个验证器的执行顺序
	 * <p>
	 * 用于对验证器进行排序，确保按正确的顺序执行
	 *
	 * @param o 另一个验证器
	 * @return 比较结果（负数表示先执行，正数表示后执行）
	 */
	@Override
	default int compareTo(RuleValidator<?, ?> o) {
		return getOrder() - o.getOrder();
	}

	/**
	 * 验证结果枚举
	 */
	enum Result {
		/** 验证通过，继续执行下一个验证器 */
		YES,
		/** 验证失败，继续执行下一个验证器 */
		NO,
		/** 验证通过，中断后续验证器执行（用于短路优化） */
		YES_BREAK;
	}

}
