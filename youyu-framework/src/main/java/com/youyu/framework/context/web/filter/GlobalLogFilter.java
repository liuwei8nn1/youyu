package com.youyu.framework.context.web.filter;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson2.JSONObject;
import com.youyu.framework.context.*;
import com.youyu.framework.logging.rest.Logs;
import com.youyu.framework.context.web.util.ServletUtil;
import com.youyu.framework.context.web.util.TraceIdGenerator;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

/**
 * 全局的请求日志过滤器
 * <p>
 * 功能：
 * 1. 记录请求和响应日志
 * 2. 提取用户上下文信息
 * 3. 设置 TraceId 到 MDC（供 Log4j2 使用）
 * 4. 支持配置化开关和采样率
 * <p>
 * 注意：此 Filter 通过 base-starter 中的 RequestLoggingAutoConfiguration 自动注册
 */
public class GlobalLogFilter implements Filter {

	@Value("${spring.application.name}-${spring.profiles.active}")
	protected String appName;

	/**
	 * 是否启用请求日志
	 */
	@Value("${logging.request.enabled:true}")
	protected boolean enabled;

	/**
	 * 采样率（0.0-1.0），1.0 表示记录所有请求
	 */
	@Value("${logging.request.sample-rate:1.0}")
	protected double sampleRate;

	/**
	 * 是否记录请求体
	 */
	@Value("${logging.request.body.enabled:false}")
	protected boolean bodyEnabled;

	private static final String BODY_DISABLED = "[BODY DISABLED]";

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		final long startTime = System.currentTimeMillis();
		final HttpServletRequest request = (HttpServletRequest) req;
		final String uri = request.getRequestURI();

		// 判断是否跳过日志记录
		boolean skipLog = shouldSkipLog();

		// 提取用户信息并设置到 ThreadLocal，同时提取 TraceId
		UserInfo userInfo = UserContextUtils.extractAndSet(request);
		String traceId = userInfo.getTraceId();
		// 设置 TraceId 到 MDC（供 Log4j2 使用）
		if (traceId == null) {
			traceId = TraceIdGenerator.generate();
		}
		MDC.put(TraceIdGenerator.TRACE_ID, traceId);
		userInfo.setTraceId(traceId);

		// 初始化日志上下文
		Logs.LogContext context = skipLog ? null : Logs.getContext(request);
		if (context != null) {
			context.init(request);
		}

		try {
			chain.doFilter(req, resp);
		} finally {
			if (skipLog) {
				cleanInfo();
			} else {
				logRequest(userInfo, request, (HttpServletResponse) resp, uri, startTime, context);
			}
		}
	}

	/**
	 * 判断是否应该跳过日志记录
	 */
	private boolean shouldSkipLog() {
		if (!enabled) {
			return true;
		}
		// 采样判断
		return sampleRate < 1.0 && Math.random() > sampleRate;
	}

	/**
	 * 记录请求日志
	 */
	private void logRequest(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response, String uri, long startTime, Logs.LogContext context) {
		final long endTime = System.currentTimeMillis();
		final long useTimeMs = endTime - startTime;

		// 构建请求体
		String requestBody = buildRequestBody(request, context);

		// 过滤并构建请求头
		JSONObject headersJson = filterHeaders(request);

		// 格式化扩展信息
		Object ext = formatExtInfo(context.ext);

		// 获取响应状态码
		int statusCode = response.getStatus();

		// 记录日志
		Logs.logJSON(userInfo.getUserId(), appName, request.getMethod(),
				uri, headersJson, requestBody, ServletUtil.getClientIP(request),
				startTime, useTimeMs, context.response, statusCode, userInfo.getTraceId(), context.exception, ext);

		// 清理上下文
		context.cleanUp();

		// 清理上下文信息
		cleanInfo();
	}

	/**
	 * 构建请求体字符串
	 */
	private String buildRequestBody(HttpServletRequest request, Logs.LogContext context) {
		String requestBody = context.requestBody;

		// 如果禁用了 Body 日志
		if (!bodyEnabled) {
			return BODY_DISABLED;
		}

		// 如果有 JSON 请求体，合并 queryString
		if (requestBody != null) {
			String queryString = request.getQueryString();
			if (!StringUtils.isEmpty(queryString)) {
				return "[Query]\n" + queryString + "\n\n[Body]\n" + requestBody;
			}
			return requestBody;
		}

		Map<String, String[]> paramMap = request.getParameterMap();
		if (paramMap.isEmpty()) {
			return "";
		}

		// 构建参数字符串
		Logs.ParamAppender appender = context.paramAppender != null ? context.paramAppender : Logs.ParamAppender.DEFAULT;
		StringBuilder params = context.sb;
		boolean notFirst = false;

		for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
			String name = entry.getKey();
			String[] values = entry.getValue();

			if (notFirst) {
				params.append('\n');
			} else {
				notFirst = true;
			}

			appender.append(name, values, params, request);
		}

		return params.toString();
	}

	/**
	 * 过滤并构建请求头 JSON 对象
	 */
	private JSONObject filterHeaders(HttpServletRequest request) {
		JSONObject headersJson = new JSONObject();
		Enumeration<String> headerNames = request.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();

			// 跳过不需要的请求头
			if (shouldSkipHeader(name)) {
				continue;
			}

			headersJson.put(name, request.getHeader(name));
		}

		return headersJson;
	}

	/**
	 * 判断是否应该跳过该请求头
	 */
	private boolean shouldSkipHeader(String name) {
		// 跳过 security 相关的头
		if (name.startsWith("sec-")) {
			return true;
		}

		// 跳过 x-forwarded-* 除了 x-forwarded-for
		if (name.startsWith("x-forwarded-") && !"x-forwarded-for".equals(name)) {
			return true;
		}

		// 跳过特定的头
		return switch (name) {
			case "x-amzn-trace-id" -> true; // AWS 负载均衡器跟踪 ID
			case "cf-ray" -> true;          // CF 跟踪ID
			case "cf-visitor" -> true;      // 固定值 {"scheme":"https"}
			case "prefer", "dnt", "priority" -> true;
			default -> false;
		};
	}

	/**
	 * 格式化扩展信息
	 */
	private Object formatExtInfo(Object ext) {
		if (ext == null) {
			return null;
		}

		if (ext instanceof String[] array) {
			return String.join(",", array);
		} else if (ext instanceof Object[] array) {
			return Arrays.toString(array);
		} else {
			return ext.toString();
		}
	}

	/**
	 * 清理上下文信息
	 */
	private void cleanInfo() {
		MDC.clear();
		UserContextHolder.clear();
	}

	/**
	 * 基于引用传递的原理修改底层 request.getParameterMap() 指定 key/name 的值
	 * <b>注意</b>：修改后，并不会影响 request.getParameter( name ) 的返回值（仍然返回原始值）
	 *
	 * @return 如果替换成功，则返回 true
	 */
	public static boolean replaceParameterMapValue(HttpServletRequest request, String name, String value) {
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			if (name.equals(entry.getKey())) {
				entry.getValue()[0] = value;
				return true;
			}
		}
		return false;
	}

}