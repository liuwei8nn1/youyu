package com.youyu.framework.validator;

import java.lang.annotation.*;

/**
 * 验证规则注解
 * <p>
 * 用于标记一个注解是验证规则注解，并指定对应的验证器。
 * 这是验证框架的核心元注解，所有自定义验证规则都需要使用此注解。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Rule(NotEmptyValidator.class)
 * public @interface NotEmpty {
 *     String message() default "不能为空";
 * }
 * 
 * // 验证器实现
 * public class NotEmptyValidator implements RuleValidator<NotEmpty, String> {
 *     @Override
 *     public Result validate(String val, ValidateContext context) {
 *         return StringUtil.notEmpty(val) ? Result.YES : Result.NO;
 *     }
 * }
 * }</pre>
 *
 * @see RuleValidator 验证器接口
 * @since 1.0
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {

	/**
	 * 验证器类数组
	 * <p>
	 * 可以指定多个验证器，按顺序执行
	 *
	 * @return 验证器类数组
	 */
	Class<? extends RuleValidator<? extends Annotation, ?>>[] value();

}
