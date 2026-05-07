package com.youyu.framework.validator;

import java.io.Serializable;

/**
 * 表单接口
 * <p>
 * 所有需要验证的表单类都应该实现此接口。
 * 提供了三个生命周期方法，可以在验证的不同阶段执行自定义逻辑。
 * <p>
 * 生命周期：
 * 1. preHandle() - 验证前处理（如数据清洗、格式转换）
 * 2. validate() - 自定义验证逻辑（补充注解无法表达的复杂验证）
 * 3. postHandle() - 验证后处理（如数据整理、计算衍生字段）
 * <p>
 * 使用示例：
 * <pre>{@code
 * @CheckForm(label = "用户注册")
 * public class UserRegisterForm implements Form {
 *     @NotEmpty
 *     private String username;
 *     
 *     @Email
 *     private String email;
 *     
 *     @Override
 *     public void preHandle() {
 *         // 去除首尾空格
 *         if (username != null) {
 *             username = username.trim();
 *         }
 *     }
 *     
 *     @Override
 *     public void validate() {
 *         // 自定义验证：用户名不能包含特殊字符
 *         if (username != null && username.contains("@")) {
 *             throw new ValidateException("用户名不能包含@符号");
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see CheckForm 表单验证注解
 * @see FormHelper 表单辅助工具
 * @since 1.0
 */
@CheckForm(label = "表单数据")
public interface Form extends FormHelper, Serializable {

	/**
	 * 前置处理：在验证规则执行前调用
	 * <p>
	 * 适用场景：
	 * - 数据清洗（去除空格、转换大小写等）
	 * - 格式标准化（日期格式、数字格式等）
	 * - 默认值设置
	 */
	default void preHandle() {
	}

	/**
	 * 自定义验证：在注解验证完成后调用
	 * <p>
	 * 适用场景：
	 * - 多字段联合验证（如密码和确认密码是否一致）
	 * - 业务规则验证（如库存是否充足）
	 * - 数据库查询验证（如用户名是否已存在）
	 * <p>
	 * 注意：如果验证失败，应该抛出 ValidateException
	 */
	default void validate() {
	}

	/**
	 * 后置处理：在验证通过后调用
	 * <p>
	 * 适用场景：
	 * - 数据整理（组装复合字段）
	 * - 计算衍生字段（如根据生日计算年龄）
	 * - 数据转换（如将字符串转为枚举）
	 */
	default void postHandle() {
	}

}
