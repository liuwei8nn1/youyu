package com.youyu.framework.validator;

import java.lang.annotation.Annotation;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import com.youyu.common.util.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

/**
 * 验证上下文接口
 * <p>
 * 这是表单验证框架的核心组件，负责在验证过程中管理和传递上下文信息。
 * 类似于 Spring Validation 的 ConstraintValidatorContext，但更加轻量和灵活。
 * <p>
 * 主要职责：
 * 1. 跟踪当前验证的字段信息（字段名、值、标签等）
 * 2. 收集验证过程中产生的所有错误
 * 3. 提供字段值的读写能力（通过 VarHandle 高性能反射）
 * 4. 支持懒加载，避免不必要的字段访问
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
 * }
 * 
 * // 验证时，ValidateContext 会：
 * // 1. 遍历每个字段
 * // 2. 应用对应的验证规则（@NotEmpty, @Email 等）
 * // 3. 收集错误信息
 * // 4. 返回验证结果
 * }</pre>
 *
 * @see CheckForm 表单验证注解
 * @see RuleValidator 规则验证器接口
 * @since 1.0
 */
public interface ValidateContext {

	/**
	 * 获取根验证项（整个被验证的对象）
	 * <p>
	 * 例如：验证 UserRegisterForm 时，root 就是该表单对象本身
	 *
	 * @return 根验证项
	 */
	ValidateItem getRoot();

	/**
	 * 获取当前正在验证的字段项
	 * <p>
	 * 在验证过程中，current 会随着字段切换而变化
	 *
	 * @return 当前验证项
	 */
	ValidateItem getCurrent();

	/**
	 * 设置当前字段的值
	 * <p>
	 * 某些验证器可能会修改字段值（如 Trim 去除空格、WordCase 转换大小写）
	 *
	 * @param val        新的值
	 * @param setToField 是否同步设置到原对象的字段上
	 *                   true: 会修改原对象的字段值
	 *                   false: 只更新验证上下文中的值，不修改原对象
	 */
	default void setCurrentValue(Object val, boolean setToField) {
		ValidateItem current = getCurrent();
		current.val = val;
		if (setToField) {
			// 通过 VarHandle 直接修改原对象的字段值
			current.info.field.set(getRoot().val, val);
		}
	}

	/**
	 * 获取当前正在执行的验证器条目
	 * <p>
	 * 包含验证规则注解和对应的验证器实例
	 *
	 * @return 当前验证器条目
	 */
	AnnotatedValidatorEntry getCurrentEntry();

	/**
	 * 获取当前字段的显示标签
	 * <p>
	 * 用于生成友好的错误提示，如："用户名不能为空"
	 *
	 * @return 字段标签
	 */
	default String getCurrentLabel() {
		return getCurrent().info.label;
	}

	/**
	 * 根据字段名获取指定的验证项
	 *
	 * @param name 字段名
	 * @return 对应的验证项，如果不存在则返回 null
	 */
	ValidateItem getItem(String name);

	/**
	 * 获取所有字段的验证项映射
	 * <p>
	 * key: 字段名
	 * value: 验证项对象
	 *
	 * @return 字段名到验证项的映射
	 */
	Map<String, ValidateItem> getItems();

	/**
	 * 添加验证错误
	 * <p>
	 * 错误信息使用 Supplier 延迟生成，只有在真正需要展示时才计算，提升性能
	 *
	 * @param error 错误信息提供者
	 */
	void addError(Supplier<String> error);

	/**
	 * 获取所有收集到的验证错误
	 *
	 * @return 错误信息列表
	 */
	List<Supplier<String>> getErrors();

	/**
	 * 验证项：封装单个字段的验证相关信息
	 * <p>
	 * 每个被验证的字段都会对应一个 ValidateItem 实例，包含：
	 * - 字段的原始值和当前值
	 * - 字段的元信息（名称、类型、标签等）
	 * - 验证错误信息
	 */
	@Getter
	@Setter
	class ValidateItem {

		/** 字段的原始值（验证前的值） */
		protected Object originalVal;
		
		/** 字段的当前值（可能被验证器修改，如 Trim 去除空格） */
		protected Object val;
		
		/** 字段的元信息（名称、类型、标签、验证规则等） */
		protected final ValidateItemInfo info;
		
		/** 错误信息提供者（延迟生成） */
		protected Supplier<String> errorMsger;

		/**
		 * 构造验证项（指定初始值）
		 *
		 * @param info 字段元信息
		 * @param val  初始值
		 */
		public ValidateItem(ValidateItemInfo info, Object val) {
			this.info = info;
			this.originalVal = val;
			this.val = val;
		}

		/**
		 * 构造验证项（使用懒加载，值为未初始化状态）
		 * <p>
		 * 适用于大对象或复杂对象，避免提前读取字段值
		 *
		 * @param info 字段元信息
		 */
		public ValidateItem(ValidateItemInfo info) {
			this(info, LazyCacheLoader.uninitialized);
		}

		/**
		 * 获取字段值（支持懒加载）
		 * <p>
		 * 如果值还未初始化，则通过 VarHandle 从对象中读取
		 *
		 * @param obj 包含该字段的对象实例
		 * @return 字段值
		 */
		protected Object getFieldValue(Object obj) {
			if (val == LazyCacheLoader.uninitialized) {
				Object v = null;
				if (info.field != null) {
					// 通过 VarHandle 高性能读取字段值
					v = info.field.get(obj);
				}
				val = originalVal = v;
			}
			return val;
		}

	}

	/**
	 * 验证项元信息：描述字段的静态信息
	 * <p>
	 * 包含字段的反射信息、验证规则等，在验证开始前构建，验证过程中不变
	 */
	@Getter
	class ValidateItemInfo {

		/**
		 * 字段的 VarHandle（Java 9+ 高性能反射 API）
		 * <p>
		 * 比传统 Field.set/get 快 3-5 倍，支持直接读写私有字段
		 */
		protected final VarHandle field;
		
		/** 字段的 Java 类型 */
		protected final Class<?> type;
		
		/** 字段名 */
		protected final String name;
		
		/** 字段显示标签（用于错误提示） */
		protected final String label;
		
		/** 是否必填 */
		protected final boolean required;
		
		/** 需要应用的验证规则列表 */
		protected final AnnotatedValidatorEntry[] toValidates;

		/**
		 * 构造字段元信息
		 *
		 * @param target     目标字段（反射 Field 对象）
		 * @param type       字段类型
		 * @param name       字段名
		 * @param label      字段标签（为空时使用字段名）
		 * @param required   是否必填
		 * @param toValidates 验证规则数组
		 */
		@SneakyThrows
		public ValidateItemInfo(@Nullable Field target, Class<?> type, String name, String label, boolean required, AnnotatedValidatorEntry[] toValidates) {
			if (target != null && StringUtil.isEmpty(label)) {
				label = target.getName();
			}

			// 创建 VarHandle，用于后续高性能访问字段
			this.field = target == null ? null : JavaUtil.IMPL_LOOKUP.findVarHandle(target.getDeclaringClass(), name, type);
			this.type = type;
			this.name = name;
			this.label = label;
			this.required = required;
			this.toValidates = toValidates;
		}

		/**
		 * 构造根对象的元信息（没有具体字段）
		 *
		 * @param rootVal  根对象实例
		 * @param rootLabel 根对象标签
		 * @param required 是否必填
		 */
		public ValidateItemInfo(Object rootVal, String rootLabel, boolean required) {
			this(null, rootVal.getClass(), null, rootLabel, required, null);
		}

		/**
		 * 从反射 Field 构造元信息
		 *
		 * @param field       反射字段对象
		 * @param label       字段标签
		 * @param required    是否必填
		 * @param toValidates 验证规则数组
		 */
		public ValidateItemInfo(Field field, String label, boolean required, AnnotatedValidatorEntry[] toValidates) {
			this(field, field.getType(), field.getName(), label, required, toValidates);
		}

	}

	/**
	 * 带注解的验证器条目：将验证规则注解与验证器实例绑定
	 * <p>
	 * 例如：@NotEmpty 注解 + NotEmptyValidator 验证器
	 */
	@Getter
	class AnnotatedValidatorEntry implements Comparable<AnnotatedValidatorEntry> {

		/** 验证规则注解（如 @NotEmpty, @Email 等） */
		protected final Annotation rule;
		
		/** 对应的验证器实例 */
		protected final RuleValidator<? extends Annotation, Object> validator;

		/**
		 * 构造验证器条目
		 * <p>
		 * 构造时会调用验证器的 init 方法进行初始化
		 *
		 * @param rule      验证规则注解
		 * @param validator 验证器实例
		 */
		public AnnotatedValidatorEntry(Annotation rule, RuleValidator<Annotation, Object> validator) {
			this.rule = rule;
			validator.init(rule);  // 初始化验证器（如编译正则表达式）
			this.validator = validator;
		}

		/**
		 * 比较验证器的执行顺序
		 * <p>
		 * 值越小越先执行，用于控制验证规则的优先级
		 *
		 * @param o 另一个验证器条目
		 * @return 比较结果
		 */
		@Override
		public int compareTo(AnnotatedValidatorEntry o) {
			return validator.compareTo(o.validator);
		}

	}

	/**
	 * 默认的验证上下文实现
	 * <p>
	 * 管理验证过程中的所有状态，包括：
	 * - 根对象和所有字段的验证项
	 * - 当前正在验证的字段
	 * - 收集的所有错误信息
	 */
	class DefaultValidateContext implements ValidateContext {

		/** 根验证项（整个被验证的对象） */
		protected ValidateItem root;
		
		/** 所有字段的验证项映射（字段名 -> 验证项） */
		protected Map<String, ValidateItem> items;
		
		/** 收集的错误信息列表（使用 LinkedList 便于追加） */
		protected List<Supplier<String>> errors = new LinkedList<>();
		
		/** 当前正在验证的字段项 */
		@Setter
		protected ValidateItem current;
		
		/** 当前正在执行的验证器 */
		@Setter
		protected AnnotatedValidatorEntry currentEntry;

		/**
		 * 构造默认验证上下文
		 *
		 * @param root  根验证项
		 * @param items 所有字段的验证项映射
		 */
		public DefaultValidateContext(ValidateItem root, Map<String, ValidateItem> items) {
			this.root = root;
			this.items = items;
		}

		@Override
		public ValidateItem getRoot() {
			return root;
		}

		@Override
		public ValidateItem getCurrent() {
			return current;
		}

		@Override
		public AnnotatedValidatorEntry getCurrentEntry() {
			return currentEntry;
		}

		@Override
		public ValidateItem getItem(String name) {
			return items.get(name);
		}

		@Override
		public Map<String, ValidateItem> getItems() {
			return items;
		}

		@Override
		public void addError(Supplier<String> error) {
			errors.add(error);
		}

		@Override
		public List<Supplier<String>> getErrors() {
			return errors;
		}

	}

}