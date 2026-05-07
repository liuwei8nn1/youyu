package com.youyu.framework.validator;

import java.lang.Character.UnicodeScript;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.youyu.common.util.*;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jspecify.annotations.Nullable;

/**
 * 验证辅助工具类
 * <p>
 * 提供常用的验证相关工具方法，包括：
 * 1. 格式验证（邮箱、手机号、真实姓名等）
 * 2. 日期转换
 * 3. 金额计算
 * 4. 中文字符判断
 * <p>
 * 该类包含多个静态匹配器和解析器，供验证器使用。
 *
 * @since 1.0
 */
public class ValidateHelper {

	/** 邮箱正则表达式模式 */
	public static Pattern EMAIL_PATTERN = Pattern.compile("^\\w+(?:\\.?[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+$");

	/** 四舍五入模式 */
	public static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

	/** 手机号匹配器：11位数字，以1开头 */
	public static Predicate<String> PHONE_MATCHER = phone -> NumberUtil.isNumber(phone, 11) && phone.charAt(0) == '1';

	/** 真实姓名匹配器：2-6位中文 */
	public static Predicate<String> REAL_NAME_MATCHER = ValidateHelper::matchRealName;

	/** 日期解析器：自动识别多种日期格式 */
	public static Function<String, Date> DATE_PARSER = str -> convertToDate(str, false);

	/** 邮箱匹配器 */
	public static Predicate<String> EMAIL_MATCHER = ValidateHelper::matchEmail;

	/**
	 * 校验用户真实姓名是否合规（必须是 2~6位 中文）
	 */
	public static boolean matchRealName(String realName) {
		if (realName == null) {
			return false;
		}
		final int length = realName.length();
		if (length < 2 || length > 6) {
			return false;
		}
		return isAllChinese(realName);
	}

	/**
	 * 匹配邮箱
	 */
	public static boolean matchEmail(String email) {
		return StringUtil.notEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
	}

	/**
	 * 完整的判断中文汉字和符号
	 *
	 * @param strName 需要判断的字符串
	 */
	public static boolean isAllChinese(String strName) {
		final int len = strName.length();
		for (int i = 0; i < len; ) {
			int codepoint = strName.codePointAt(i);
			if (UnicodeScript.of(codepoint) != UnicodeScript.HAN) {
				return false;
			}
			i += Character.charCount(codepoint);
		}
		return len > 0;
	}

	@SuppressWarnings("ConstantValue")
	public static Date convertToDate(String source, final boolean ignoreException) {
		if (StringUtil.isEmpty(source)) {
			return null;
		}
		final int len = source.length();
		String pattern;
		switch (len) {
			case 6 -> pattern = "yyyyMM";
			case 7 -> pattern = "yyyy-MM";
			case 8 -> pattern = "yyyyMMdd";
			case 10 -> pattern = "yyyy-MM-dd";
			case 11 -> pattern = "yyyy年MM月dd日";
			case 13 -> {
				Long val = NumberUtil.getLong(source, null); // 兼容时间戳
				if (val != null) {
					return new Date(val);
				}
				pattern = "yyyy-MM-dd HH";
			}
			case 16 -> pattern = "yyyy-MM-dd HH:mm";
			case 19 -> pattern = "yyyy-MM-dd HH:mm:ss";
			case 23 -> pattern = "yyyy-MM-dd HH:mm:ss.SSS";
			default -> {
				return handleEx(ignoreException, source, null);
			}
		}
		FastDateFormat format = FastDateFormat.getInstance(pattern);
		try {
			return format.parse(source);
		} catch (ParseException e) {
			return handleEx(ignoreException, source, e);
		}
	}

	@Nullable
	private static Date handleEx(boolean ignoreException, String source, @Nullable Throwable clause) {
		if (ignoreException) {
			return null;
		}
		throw new IllegalArgumentException("Invalid value: " + source, clause);
	}

	/**
	 * 基于 基准数值 {@code baseAmount}、固定值{@code fixedAmount} 和 系数 {@code rate} ，返回一个符合期望的计算结果： 1、如果 {@code fixedAmount} 不为 null，则返回该值 2、如果 {@code rate} 不为 null，则返回 {@code baseAmount * rate} 3、如果 {@code fixedAmount} 和 {@code rate} 都不为 null，则抛出异常
	 *
	 * @param baseAmount 基准数值（总额）
	 * @param fixedAmount 固定数值
	 * @param rate 系数（基于‱，即万分之一）
	 */
	public static Integer calcAmountInt(Integer baseAmount, @Nullable Integer fixedAmount, @Nullable Integer rate) throws IllegalArgumentException {
		return castAsInt(calcAmount(baseAmount, fixedAmount, rate, 1));
	}

	/**
	 * 基于 基准数值 {@code baseAmount}、固定值{@code fixedAmount} 和 系数 {@code rate} ，返回一个符合期望的计算结果： 1、如果 {@code fixedAmount} 不为 null，则返回该值 2、如果 {@code rate} 不为 null，则返回 {@code baseAmount * rate} 3、如果 {@code fixedAmount} 和 {@code rate} 都不为 null，则抛出异常
	 *
	 * @param baseAmount 基准数值（总额）
	 * @param fixedAmount 固定数值
	 * @param rate 系数（基于‱，即万分之一）
	 * @param fixedCount 当基于固定值进行计算时，需要用到的单位数量
	 */
	public static Integer calcAmountInt(Integer baseAmount, @Nullable Integer fixedAmount, @Nullable Integer rate, int fixedCount) throws IllegalArgumentException {
		return castAsInt(calcAmount(baseAmount, fixedAmount, rate, fixedCount));
	}

	/**
	 * 基于 基准数值 {@code baseAmount}、固定值{@code fixedAmount} 和 系数 {@code rate} ，返回一个符合期望的计算结果： 1、如果 {@code fixedAmount} 不为 null，则返回该值 2、如果 {@code rate} 不为 null，则返回 {@code baseAmount * rate} 3、如果 {@code fixedAmount} 和 {@code rate} 都不为 null，则抛出异常
	 *
	 * @param baseAmount 基准数值（总额）
	 * @param fixedAmount 固定数值
	 * @param rate 系数（基于‱，即万分之一）
	 * @param fixedCount 当基于固定值进行计算时，需要用到的单位数量
	 */
	public static long calcAmount(Number baseAmount, @Nullable Number fixedAmount, @Nullable Integer rate, int fixedCount) throws IllegalArgumentException {
		if (fixedAmount != null) {
			Assert.isNull(rate);
			return (long) fixedAmount * fixedCount;
		}
		if (rate != null) {
			return multiplyRate((long) baseAmount, rate);
		}
		return 0L;
	}

	/**
	 * 计算 数值 乘以 系数，并返回与数值 {@code baseAmount} 保持相同单位的计算结果
	 *
	 * @param baseAmount 基准数值
	 * @param rate 系数（基于‱，即万分之一）
	 */
	public static long multiplyRate(long baseAmount, int rate) {
		return BigDecimal.valueOf(baseAmount * rate).divide(Arith.MYRIAD, 0, ValidateHelper.ROUNDING_MODE).longValue();
	}

	/**
	 * 将 long 转换为 int 类型，并确保不会发生数据截断（暂不验证负数值的截取范围）
	 */
	public static int castAsInt(long val) throws IllegalArgumentException {
		// 确保数据转换时不会发生整数数据截断
		Assert.isTrue(val <= Integer.MAX_VALUE, () -> "number is too small: " + val);
		return (int) val;
	}

	/**
	 * 检查指定的整数是否会超出 int 的最大有效值
	 */
	public static void checkInt(long val) throws IllegalArgumentException {
		// 确保数据转换时不会发生整数数据截断
		Assert.isTrue(val <= Integer.MAX_VALUE, () -> "number is too large: " + val);
	}

}