package com.youyu.framework.validator;

import java.io.Serializable;
import java.lang.annotation.*;


import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 枚举值验证注解
 * <p>
 * 验证值是否为指定枚举类型中的有效值。
 * 支持三种匹配方式：
 * 1. FROM_ORDINAL (0): 通过枚举序号匹配（ordinal）
 * 2. FROM_NAME (1): 通过枚举名称匹配（name）
 * 3. FROM_VALUE_ENUM (2): 通过自定义值匹配（需要枚举实现 ValueEnum 接口）
 * <p>
 * 默认情况下会自动判断匹配方式：
 * - 如果枚举实现了 ValueEnum 接口，使用 FROM_VALUE_ENUM
 * - 如果值是 Integer，使用 FROM_ORDINAL
 * - 如果值是 String，使用 FROM_NAME
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 方式1：通过枚举名称
 * public class UserForm {
 *     @EnumSet(UserStatus.class)
 *     private String status;  // "ACTIVE", "INACTIVE"
 * }
 * 
 * // 方式2：通过枚举序号
 * public class OrderForm {
 *     @EnumSet(OrderStatus.class)
 *     private Integer status;  // 0, 1, 2
 * }
 * 
 * // 方式3：通过自定义值
 * public enum UserType implements ValueEnum<Integer, Integer> {
 *     ADMIN(1, "管理员"),
 *     USER(2, "普通用户");
 *     
 *     private final Integer value;
 *     
 *     @Override
 *     public Integer getValue() { return value; }
 * }
 * 
 * public class UserForm {
 *     @EnumSet(value = UserType.class, valueFrom = EnumSet.FROM_VALUE_ENUM)
 *     private Integer userType;  // 1 或 2
 * }
 * }</pre>
 *
 * @see ValueEnum 值枚举接口
 * @see EnumUtil 枚举工具类
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(EnumSet.EnumSetValidator.class)
public @interface EnumSet {

	/** 通过枚举序号匹配（ordinal） */
	int FROM_ORDINAL = 0;
	/** 通过枚举名称匹配（name） */
	int FROM_NAME = 1;
	/** 通过自定义值匹配（需要实现 ValueEnum 接口） */
	int FROM_VALUE_ENUM = 2;

	/**
	 * 枚举类型
	 *
	 * @return 枚举类
	 */
	Class<? extends Enum<?>> value();

	/**
	 * 指定参数值在枚举中的取值来源
	 * <p>
	 * -1: 自动判断（默认）
	 *  0: FROM_ORDINAL - 通过序号匹配
	 *  1: FROM_NAME - 通过名称匹配
	 *  2: FROM_VALUE_ENUM - 通过自定义值匹配
	 *
	 * @return 取值来源
	 * @see #FROM_ORDINAL
	 * @see #FROM_NAME
	 * @see #FROM_VALUE_ENUM
	 */
	int valueFrom() default -1;

	/**
	 * 枚举值验证器
	 * <p>
	 * 支持多种匹配方式，自动适配不同类型的输入值
	 */
	class EnumSetValidator extends AbstractRuleValidator<EnumSet, Serializable> {

		protected Enum<?>[] values;
		protected boolean isValueEnum;
		protected int valueFrom = -1;

		@Override
		public void init(EnumSet rule) {
			Class<? extends Enum<?>> enumClass = rule.value();
			isValueEnum = ValueEnum.class.isAssignableFrom(enumClass);
			values = enumClass.getEnumConstants();
			valueFrom = rule.valueFrom();
			if (valueFrom < -1 || valueFrom > 2) {
				throw new ValidateException("@EnumSet 注解配置无效，valueFrom 属性值无效！");
			}
			if (valueFrom == FROM_VALUE_ENUM && !isValueEnum) {
				throw new ValidateException("@EnumSet 注解配置无效，指定枚举类必须实现 ValueEnum 接口！");
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected String validateInternal(Serializable value, ValidateContext context) {
			if (valueFrom == -1) {
				if (isValueEnum) {
					valueFrom = FROM_VALUE_ENUM;
				} else if (value instanceof Integer) {
					valueFrom = FROM_ORDINAL;
				} else if (value instanceof String) {
					valueFrom = FROM_NAME;
				} else {
					throw new ValidateException("@EnumSet 不支持校验该参数类型！");
				}
			}
			boolean valid = switch (valueFrom) {
				case FROM_ORDINAL -> {
					int val = (int) value;
					yield val >= 0 && val < values.length - 1;
				}
				case FROM_NAME -> {
					String name = (String) value;
					yield EnumUtil.of(values[0].getClass(), name) != null;
				}
				case FROM_VALUE_ENUM -> {
					ValueEnum<?, Serializable> valueEnum = (ValueEnum<?, Serializable>) values[0];
					yield valueEnum.getValueOf(value) != null;
				}
				default -> false;
			};
			return valid ? null : ValidateError.invalid;
		}

	}

}