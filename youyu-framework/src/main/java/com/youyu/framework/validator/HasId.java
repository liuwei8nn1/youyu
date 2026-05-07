package com.youyu.framework.validator;

import java.lang.annotation.*;

import com.youyu.framework.datasource.mybatis.ID;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * ID有效性验证注解
 * <p>
 * 验证 ID 对象是否包含有效的 ID 值。
 * 通常用于更新操作，确保传入的 ID 不为空且有效。
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class UserUpdateForm {
 *     @HasId
 *     private ID userId;  // 必须有有效的 ID
 * }
 * }</pre>
 *
 * @see ID ID 类型
 * @since 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(HasId.HasIdValidator.class)
public @interface HasId {

	/**
	 * ID有效性验证器
	 */
	class HasIdValidator extends AbstractRuleValidator<HasId, ID> {

		@Override
		protected String validateInternal(ID val, ValidateContext context) {
			return val != null && val.hasValidId() ? null : ValidateError.required;
		}

	}

}
