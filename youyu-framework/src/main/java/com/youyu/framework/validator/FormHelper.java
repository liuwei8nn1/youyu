package com.youyu.framework.validator;

import java.util.Collection;
import java.util.function.Predicate;

import com.youyu.common.util.Assert;
import com.youyu.common.util.ObjUtil;
import org.jspecify.annotations.Nullable;

/**
 * 表单辅助工具接口
 * <p>
 * 提供常用的数据转换和断言方法，简化表单处理逻辑。
 * 所有实现 {@link Form} 接口的类都可以直接使用这些方法。
 * <p>
 * 主要功能：
 * 1. 数值转换（0 <-> null）
 * 2. 集合转换（空集合 <-> null）
 * 3. 参数断言（大于0、匹配条件、互斥检查等）
 * <p>
 * 使用示例：
 * <pre>{@code
 * @CheckForm(label = "商品查询")
 * public class ProductQueryForm implements Form {
 *     private Integer pageNum;
 *     private Integer pageSize;
 *     private List<Long> categoryIds;
 *     
 *     @Override
 *     public void preHandle() {
 *         // 将 0 转换为 null
 *         pageNum = zeroAsNull(pageNum);
 *         pageSize = zeroAsNull(pageSize);
 *         
 *         // 将空集合转换为 null
 *         categoryIds = emptyAsNull(categoryIds);
 *     }
 *     
 *     @Override
 *     public void validate() {
 *         // 断言页码和每页数量都 > 0
 *         assertGtZero(pageNum, pageSize);
 *     }
 * }
 * }</pre>
 *
 * @see Form 表单接口
 * @since 1.0
 */
public interface FormHelper {

	/**
	 * 如果参数为 0 则转为 null
	 * <p>
	 * 常用于分页参数，将前端传来的 0 转换为 null，使用默认值
	 *
	 * @param val 输入值
	 * @return 如果为 0 则返回 null，否则返回原值
	 */
	@Nullable
	static Integer zeroAsNull(@Nullable Integer val) {
		return val != null && val == 0 ? null : val;
	}

	/**
	 * 如果集合为空则转为 null
	 * <p>
	 * 常用于查询条件，将空集合转换为 null，避免生成无效的 SQL IN 条件
	 *
	 * @param c 输入集合
	 * @return 如果为空集合则返回 null，否则返回原集合
	 */
	@Nullable
	default <T extends Collection<?>> T emptyAsNull(@Nullable T c) {
		return c != null && c.isEmpty() ? null : c;
	}

	/**
	 * 如果参数为 null 则转为 0
	 * <p>
	 * 常用于数值计算，确保不会出现 NullPointerException
	 *
	 * @param val 输入值
	 * @return 如果为 null 则返回 0，否则返回原值
	 */
	static Integer nullAsZero(@Nullable Integer val) {
		return val == null ? 0 : val;
	}

	/**
	 * 如果参数为 null 则转为 0L
	 * <p>
	 * 常用于长整型数值计算
	 *
	 * @param val 输入值
	 * @return 如果为 null 则返回 0L，否则返回原值
	 */
	static Long nullAsZero(@Nullable Long val) {
		return val == null ? 0L : val;
	}

	/**
	 * 断言指定的参数 > 0
	 * <p>
	 * 如果验证失败，抛出 IllegalArgumentException
	 *
	 * @param val 要验证的参数
	 * @throws IllegalArgumentException 如果 val <= 0
	 */
	default void assertGtZero(Integer val) {
		Assert.isTrue(val > 0);
	}

	/**
	 * 断言指定的参数都 > 0
	 * <p>
	 * 如果任何一个参数 <= 0，抛出 IllegalArgumentException
	 *
	 * @param a 第一个参数
	 * @param b 第二个参数
	 * @throws IllegalArgumentException 如果任一参数 <= 0
	 */
	default void assertGtZero(Integer a, Integer b) {
		Assert.isTrue(a > 0 && b > 0);
	}

	/**
	 * 断言指定的参数都 > 0
	 * <p>
	 * 如果任何一个参数 <= 0，抛出 IllegalArgumentException
	 *
	 * @param a 第一个参数
	 * @param b 第二个参数
	 * @param c 第三个参数
	 * @throws IllegalArgumentException 如果任一参数 <= 0
	 */
	default void assertGtZero(Integer a, Integer b, Integer c) {
		Assert.isTrue(a > 0 && b > 0 && c > 0);
	}

	/**
	 * 断言指定的参数都符合指定的 {@code matcher} 条件
	 * <p>
	 * 如果任何一个参数不符合条件，抛出 IllegalArgumentException
	 *
	 * @param matcher 条件判断器
	 * @param a       第一个参数
	 * @param b       第二个参数
	 * @throws IllegalArgumentException 如果任一参数不符合条件
	 */
	default <T> void assertMatchAll(Predicate<T> matcher, T a, T b) {
		Assert.isTrue(matcher.test(a) && matcher.test(b));
	}

	/**
	 * 断言指定的参数都符合指定的 {@code matcher} 条件
	 * <p>
	 * 如果任何一个参数不符合条件，抛出 IllegalArgumentException
	 *
	 * @param matcher 条件判断器
	 * @param a       第一个参数
	 * @param b       第二个参数
	 * @param c       第三个参数
	 * @throws IllegalArgumentException 如果任一参数不符合条件
	 */
	default <T> void assertMatchAll(Predicate<T> matcher, T a, T b, T c) {
		Assert.isTrue(matcher.test(a) && matcher.test(b) && matcher.test(c));
	}

	/**
	 * 断言两个布尔条件是互斥的
	 * <p>
	 * 互斥意味着：如果一个为 true，则另一个必定为 false
	 * 如果两个条件相同（都为 true 或都为 false），则抛出异常
	 * <p>
	 * 适用场景：
	 * - 两个选项只能选其一
	 * - 两种状态不能同时存在
	 *
	 * @param a     第一个条件
	 * @param b     第二个条件
	 * @param error 错误消息（可以是 String 或 Supplier<String>）
	 * @throws IllegalArgumentException 如果两个条件不互斥
	 */
	default void assertMutex(boolean a, boolean b, Object error) {
		if (a == b) {
			throw new IllegalArgumentException((String) ObjUtil.tryUnwrap(error));
		}
	}

}