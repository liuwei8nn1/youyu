package com.youyu.common.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 *
 */
public abstract class ObjUtil {

	/**
	 * 尝试拆箱可能由 {@link Supplier } 接口包装的实体对象
	 *
	 * @return 如果指定参数实现了 {@link Supplier } 接口，则调用 get() 方法 并返回其值；否则直接返回 该对象本身
	 * @since 2.3.0
	 */
	@SuppressWarnings("unchecked")
	public static <E> E tryUnwrap(@Nullable Object supplier) {
		if (supplier instanceof Supplier) {
			return ((Supplier<E>) supplier).get();
		}
		return (E) supplier;
	}

	/**
	 * 将指定泛型对象进行泛型擦除，并转换为对应的泛型声明
	 */
	@SuppressWarnings("unchecked")
	public static <T> T castType(Object obj) {
		return (T) obj;
	}

	/**
	 * 判断指定Boolean值是否有效。<code>true</code> 即为有效。
	 *
	 * @param b 指定的 Boolean 对象
	 */
	public static boolean isValid(Boolean b) {
		return b != null && b;
	}

	/**
	 * 判断指定的数值对象是否有效。如果参数为 <code>null</code> 或 数值等于0，则为无效；其他均为有效。
	 *
	 * @param number 指定的
	 */
	public static boolean isValid(Number number) {
		return number != null && number.doubleValue() != 0;
	}

	/**
	 * 判断指定的数值对象是否有效。如果参数为 <code>null</code> 或 数值等于0，则为无效；其他均为有效。
	 *
	 * @param number 指定的
	 */
	public static boolean isValid(Integer number) {
		return number != null && number != 0;
	}

	/**
	 * 判断指定的数值对象是否有效。如果参数为 <code>null</code> 或 数值等于0，则为无效；其他均为有效。
	 *
	 * @param number 指定的
	 */
	public static boolean isValid(Long number) {
		return number != null && number != 0;
	}

	/**
	 * 判断指定的字符串序列是否有效。如果参数为 <code>null</code> 或空字符串 <code>""</code> ，则为无效；其他均为有效。
	 *
	 * @param sequence 指定的字符串序列对象
	 */
	public static boolean isValid(CharSequence sequence) {
		return StringUtil.notEmpty(sequence);
	}

	/**
	 * 判断指定的Map对象是否有效。如果参数为 <code>null</code> 或 <code>map.size() == 0</code>，则为无效，其他均为有效。
	 *
	 * @param map 指定的映射集合对象
	 */
	public static boolean isValid(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}

	/**
	 * 判断指定的Collection对象是否有效。如果参数为 <code>null</code> 或 <code>collection.size() == 0</code>，则为无效，其他均为有效。
	 *
	 * @param collection 指定的集合对象
	 */
	public static boolean isValid(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}

	/**
	 * 判断指定byte数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的 byte 数组
	 */
	public static boolean isValid(byte[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定int数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的 int 数组
	 */
	public static boolean isValid(int[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定long数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的 long 数组
	 */
	public static boolean isValid(long[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定char数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的 char 数组
	 */
	public static boolean isValid(char[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定float数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的 float 数组
	 */
	public static boolean isValid(float[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定double数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的 double 数组
	 */
	public static boolean isValid(double[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定的对象数组是否有效。如果参数为 <code>null</code> 或 <code>array.length == 0</code>，则为无效，其他均为有效。
	 *
	 * @param array 指定的对象数组
	 */
	public static boolean isValid(Object[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * 判断指定对象是否无效。只有以下情况视为无效，其他均为有效。<br>
	 * 无效的参数对象arg的定义如下(按照判断顺序排序)： <br>
	 * 1. <code>arg == null</code><br>
	 * 2. 如果<code>arg</code>是字符序列对象，去空格后，<code>arg.length() == 0</code><br>
	 * 3. 如果<code>arg</code>是数值对象，去空格后，<code>值  == 0</code><br>
	 * 4. 如果<code>arg</code>是映射集合(Map)对象，<code>arg.size() == 0</code><br>
	 * 5. 如果<code>arg</code>是集合(Collection)对象，<code>arg.size() == 0</code><br>
	 * 6. 如果<code>arg</code>是数组(Array)对象，<code>arg.length == 0</code><br>
	 * 7. 如果<code>arg</code>是布尔(Boolean)对象，<code>arg 等价于  false</code><br>
	 *
	 * @return 上述无效的情况返回 <code>false</code>，其他情况均返回 <code>true</code>
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isValid(Object arg) {
		if (arg == null) {
			return false;
		} else if (arg instanceof CharSequence) {
			return ((CharSequence) arg).length() > 0;
		} else if (arg instanceof Number) {
			return ((Number) arg).doubleValue() != 0;
		} else if (arg instanceof Map) {
			return !((Map) arg).isEmpty();
		} else if (arg instanceof Collection) {
			return !((Collection) arg).isEmpty();
		} else if (arg.getClass().isArray()) {
			return Array.getLength(arg) > 0;
		} else if (arg instanceof Boolean) {
			return (Boolean) arg;
		}
		return true;
	}
}
