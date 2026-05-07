package com.youyu.framework.logging.converter;

import java.io.PrintWriter;
import java.util.List;

import com.youyu.common.util.*;
import com.youyu.framework.context.Env;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.*;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 精简堆栈的异常日志输出转换器
 *
 * @see ThrowablePatternConverter
 */
@Plugin(name = "ThinThrowablePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "thinEx" })
public class ThinThrowablePatternConverter extends LogEventPatternConverter {

	/** 指示包含还是排除指定包前缀，默认是包含 */
	private final boolean include;
	/** 需要过滤的包名前缀数组，本地环境为 null（打印完整堆栈） */
	@Nullable
	private final String[] packages;
	/** 每个异常栈的打印深度（行数） */
	private final int stackDepth;

	/**
	 * 构造函数 - 已废弃
	 *
	 * @param name 转换器名称
	 * @param style CSS 样式（未使用）
	 * @param options 配置选项数组
	 * @deprecated 使用 ThinThrowablePatternConverter(String name, String style, String[] options, Configuration config)
	 */
	@Deprecated
	protected ThinThrowablePatternConverter(final String name, final String style, final String[] options) {
		this(name, style, options, null);
	}

	/**
	 * 构造函数
	 *
	 * @param name 转换器名称
	 * @param style CSS 样式（未使用）
	 * @param options 配置选项数组，格式：[包名前缀列表(可选!前缀表示排除), 堆栈深度]
	 *                示例："com.youyu" 表示只打印 com.youyu 包的堆栈
	 *                      "!com.youyu" 表示排除 com.youyu 包的堆栈
	 *                      "com.youyu,3" 表示只打印 com.youyu 包，深度为 3
	 * @param config Log4j2 配置对象（可为 null）
	 */
	protected ThinThrowablePatternConverter(final String name, final String style, final String[] options, final Configuration config) {
		super(name, style);
		List<String> list = null;
		int stackDepth = 7; // 默认堆栈深度为 7 行
		boolean include = true; // 默认为包含模式
		if (options != null && options.length > 0) {
			String raw = options[0];
			// 如果以 '!' 开头，表示排除模式
			String pkgs = StringUtils.removeStart(raw, '!');
			//noinspection StringEquality
			include = pkgs == raw;
			list = StringUtil.splitAsStringList(pkgs);
			if (options.length >= 2) {
				stackDepth = Math.max(NumberUtil.getInt(options[1], stackDepth), 1);
			}
		}
		this.include = include;
		// 非本地环境且配置了包名时，才启用堆栈过滤
		if(Env.inLocal() || Env.inDev()){
			this.packages = null;
		}else{
			this.packages = ObjUtil.isValid(list) ? list.toArray(new String[0]) : null;
		}
		this.stackDepth = stackDepth;
	}

	/**
	 * 创建转换器实例（工厂方法）
	 *
	 * @param config Log4j2 配置对象
	 * @param options 配置选项数组
	 * @return ThinThrowablePatternConverter 实例
	 */
	public static ThinThrowablePatternConverter newInstance(final Configuration config, final String[] options) {
		return new ThinThrowablePatternConverter("ThenEx", "throwable", options, config);
	}

	/**
	 * 格式化日志事件中的异常信息
	 *
	 * @param event 日志事件对象
	 * @param buffer 用于追加格式化内容的 StringBuilder
	 */
	@Override
	public void format(final LogEvent event, final StringBuilder buffer) {
		final Throwable t = event.getThrown();
		if (t != null) {
			formatOption(t, buffer);
		}
	}

	/**
	 * 格式化异常选项
	 * 根据配置决定是否过滤堆栈信息
	 *
	 * @param throwable 异常对象
	 * @param buffer 用于追加格式化内容的 StringBuilder
	 */
	private void formatOption(final Throwable throwable, final StringBuilder buffer) {
		final int len = buffer.length();
		// 确保前面有空格分隔
		if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
			buffer.append(' ');
		}
		if (packages != null) {
			// 启用堆栈过滤
			printStack(throwable, this.stackDepth, this.include, this.packages, buffer);
		} else {
			// 打印完整堆栈（本地环境或未配置过滤规则）
			throwable.printStackTrace(new PrintWriter(new StringBuilderWriter(buffer)));
		}
	}

	/**
	 * 标识此转换器是否处理异常信息
	 *
	 * @return true 表示处理异常
	 */
	@Override
	public boolean handlesThrowable() {
		return true;
	}

	/**
	 * 递归逆向打印异常堆栈及 cause（从最底层的异常开始往上打印）
	 * 特殊处理：MyBatisPlus 异常直接打印完整堆栈
	 *
	 * @param t 原始异常
	 * @param stackDepth 每个异常栈的打印深度（行数）
	 * @param include 是否为包含模式（true=只打印指定包，false=排除指定包）
	 * @param packages 包名前缀数组
	 * @param sb 字符串构造器
	 */
	public static void printStack(Throwable t, int stackDepth, boolean include, @Nullable String[] packages, StringBuilder sb) {
		// MyBatisPlus 异常保留完整堆栈，便于调试
		if (t instanceof com.baomidou.mybatisplus.core.exceptions.MybatisPlusException) {
			t.printStackTrace(new PrintWriter(new StringBuilderWriter(sb)));
			return;
		}
		// 最多打印 7 层 cause 链
		doPrintStack(t, 7, stackDepth, include, packages, sb);
	}

	/**
	 * 递归打印单个异常的堆栈信息
	 *
	 * @param t 当前异常对象
	 * @param remainCausedBy 剩余可打印的 cause 层数
	 * @param singleStackDepth 单个异常的堆栈打印深度
	 * @param include 是否为包含模式
	 * @param packages 包名前缀数组
	 * @param sb 字符串构造器
	 */
	public static void doPrintStack(Throwable t, int remainCausedBy, final int singleStackDepth, boolean include, @Nullable String[] packages, StringBuilder sb) {
		// 打印异常类名和消息
		sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append('\n');
		StackTraceElement[] elements = t.getStackTrace();
		// 遍历堆栈元素，按深度限制和包过滤规则打印
		for (int i = 0, remainDepth = singleStackDepth; i < elements.length && remainDepth > 0; i++) {
			StackTraceElement line = elements[i];
			// 前 2 行不跳过（通常是业务代码入口），后续行根据配置过滤
			if (i > 1 && shouldSkipLine(line, include, packages)) {
				continue;
			}
			// 使用优化格式打印堆栈行（包含模块名、文件名、行号）
			appendBetterLine(line, sb);
			// appendSimpleLine(line, sb); // 简化格式（备用）
			remainDepth--;
		}
		// 递归打印 cause 异常链
		if (remainCausedBy-- > 0 && t.getCause() != null) {
			sb.append("Caused by: ");
			doPrintStack(t.getCause(), remainCausedBy, singleStackDepth, include, packages, sb);
		}
	}

	/**
	 * 判断是否应该跳过当前堆栈行
	 *
	 * @param line 堆栈跟踪元素
	 * @param include 是否为包含模式（true=只保留指定包，false=排除指定包）
	 * @param packages 包名前缀数组
	 * @return true 表示跳过该行，false 表示保留
	 */
	protected static boolean shouldSkipLine(StackTraceElement line, boolean include, @Nullable String[] packages) {
		if (packages != null) {
			final String className = line.getClassName();
			final int length = className.length();
			// include=true 时，不匹配的跳过；include=false 时，匹配的跳过
			return include != matchPrefix(packages, className, length);
		}
		return false;
	}

	/**
	 * 检查类名是否匹配任意包前缀
	 *
	 * @param packages 包名前缀数组
	 * @param className 完整的类名
	 * @param length 类名长度
	 * @return true 表示匹配成功
	 */
	private static boolean matchPrefix(@NonNull String[] packages, String className, int length) {
		for (String pkg : packages) {
			int prefixSize = pkg.length();
			// 包名前缀必须后跟 '.' 才是有效匹配（避免部分匹配）
			if (prefixSize >= length || className.charAt(prefixSize) != '.') {
				continue;
			}
			if (className.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 以优化格式追加堆栈行信息（推荐）
	 * 格式：at [模块名/]类名.方法名(文件名:行号)
	 * 示例：at com.youyu.service.UserService.getUser(UserService.java:42)
	 *
	 * @param e 堆栈跟踪元素
	 * @param sb 字符串构造器
	 * @see StackTraceElement#toString()
	 */
	public static void appendBetterLine(StackTraceElement e, StringBuilder sb) {
		sb.append("\tat ");
		String moduleName = e.getModuleName();
		if (moduleName != null && !moduleName.isEmpty()) {
			sb.append(moduleName).append("/").append(e.getClassName());
		} else {
			sb.append(e.getClassName());
		}
		sb.append('.').append(e.getMethodName());
		if (e.isNativeMethod()) {
			sb.append("(Native Method)");
		} else {
			String fileName = e.getFileName();
			if (fileName != null) {
				sb.append('(').append(fileName).append(':');
				final int lineNumber = e.getLineNumber();
				if (lineNumber >= 0) {
					sb.append(lineNumber);
				}
				sb.append(')');
			} else {
				sb.append("(Unknown Source)");
			}
		}
		sb.append('\n');
	}

	/**
	 * 以简化格式追加堆栈行信息（备用）
	 * 格式：类名#方法名:行号
	 * 示例：com.youyu.service.UserService#getUser:42
	 *
	 * @param line 堆栈跟踪元素
	 * @param sb 字符串构造器
	 * @see StackTraceElement#toString()
	 */
	protected static void appendSimpleLine(StackTraceElement line, StringBuilder sb) {
		sb.append('\t').append(line.getClassName()).append('#').append(line.getMethodName()).append(':').append(line.getLineNumber()).append('\n');
	}

	/**
	 * 以标准格式追加堆栈行信息（备用）
	 * 格式：at 类名.方法名(文件名:行号)
	 * 示例：at com.youyu.service.UserService.getUser(UserService.java:42)
	 *
	 * @param line 堆栈跟踪元素
	 * @param sb 字符串构造器
	 * @see StackTraceElement#toString()
	 */
	protected static void appendStdLine(StackTraceElement line, StringBuilder sb) {
		sb.append("\tat ").append(line).append('\n');
	}

}