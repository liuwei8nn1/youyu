package com.youyu.framework.logging.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.alibaba.cloud.commons.io.StringBuilderWriter;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.context.Env;
import com.youyu.framework.context.web.util.SpringUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.*;

public class Logs {

	public static final Logger LOGGER = LoggerFactory.getLogger(Logs.class);

	/** StringBuilder 的初始化容量 */
	@Getter
	static int initCapacity = 128;
	/** StringBuilder 的最大初始化容量，超过此值将会被丢弃 */
	@Getter
	static int maxCapacity = 1024;

	public static LogContextHandler HANDLER = new FastContextHandler();

	public static void setInitCapacity(int initCapacity) {
		Logs.initCapacity = Math.max(initCapacity, 64);
	}

	public static void setMaxCapacity(int maxCapacity) {
		Logs.maxCapacity = Math.min(maxCapacity, 16 * 1024);
	}

	public static void logJSON(@Nullable Long userId, String appName, String requestMethod, String requestURI, JSONObject headersJson,
	                           String params, String ip, long time, long useTimeMs, @Nullable Object retVal, Integer httpCode, String traceId, @Nullable Throwable e, @Nullable Object ext) {

		JSONObject json = new JSONObject(16, 1F);
		json.put("app", appName);
		if (userId != null) {
			json.put("userId", userId);
		}
		json.put("method", requestMethod);
		json.put("uri", requestURI);
		json.put("httpCode", httpCode);
		json.put("traceId", traceId);

		// 区分请求类型：GET/POST-Form/POST-JSON
		String contentType = headersJson.getString("content-type");
		if (contentType != null) {
			if (contentType.contains("application/json")) {
				json.put("requestType", "JSON");
			} else if (contentType.contains("application/x-www-form-urlencoded")) {
				json.put("requestType", "FORM");
			} else if (contentType.contains("multipart/")) {
				json.put("requestType", "MULTIPART");
			} else {
				json.put("requestType", "OTHER");
			}
		} else if ("GET".equalsIgnoreCase(requestMethod)) {
			json.put("requestType", "QUERY");
		}
		json.put("headers", headersJson);
		json.put("params", params);
		json.put("ip", ip);
		json.put("time", time);
		json.put("useTimeMs", useTimeMs);
		if (retVal != null) {
			json.put("response", retVal instanceof String ? retVal : JSON.toJSONString(retVal));
		}
		if (e != null) {
			json.put("ex", e.toString());
		}
		if (ext != null) {
			json.put("ext", ext);
		}
		// 注意：不传入 Throwable 参数，防止 Log4j2 自动追加堆栈信息
		LOGGER.info(json.toJSONString());
	}

	public static String toRequestBody(HttpServletRequest request) throws IOException {
		String body = toString(request.getInputStream(), StandardCharsets.UTF_8, request.getContentLength());
		Logs.setRequestBody(body, request);
		return body;
	}

	/**
	 * @param contentLength 请求报文的内容长度，如果传入 -1 表示未知
	 */
	public static int calcInitialCapacity(InputStream is, long contentLength) throws IOException {
		if (contentLength > Integer.MAX_VALUE) {
			return 1024;
		} else if (contentLength > -1) {
			return (int) Math.min(contentLength, 1024 * 1024);
		}
		int available = is.available();
		return available <= 1 /* 可能表示未知 */ ? 1024 : available;
	}

	/**
	 * @param contentLength 请求报文的内容长度，如果传入 -1 表示未知
	 */
	public static String toString(final InputStream input, final Charset charset, int contentLength) throws IOException {
		final int expectSize = calcInitialCapacity(input, contentLength);
		try (StringBuilderWriter sw = new StringBuilderWriter(expectSize)) {
			IOUtils.copy(input, sw, charset);
			return sw.toString();
		}
	}

	// see jdk.internal.util.ArraysSupport.SOFT_MAX_ARRAY_LENGTH
	protected static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

	/**
	 * @param in 指定的输入流
	 * @param contentLength // -1 表示未知
	 */
	public static byte[] readAll(final InputStream in, final long contentLength) throws IOException {
		final int expectSize = calcInitialCapacity(in, contentLength);
		byte[] body = new byte[expectSize];

		if (Env.inner()) {
			SpringUtil.log.info("available={}，contentLength={}", in.available(), contentLength);
		}

		int offset = in.read(body, 0, body.length);
		if (offset == -1) {
			body = ArrayUtils.EMPTY_BYTE_ARRAY;
		} else if (contentLength == -1 || offset != contentLength) {
			/* 如果需要预防错误的 contentLength，则可以再多读取一次
			int firstByte = -1;
			if (offset == contentLength && (firstByte = in.read()) == -1) {
				return body;
			}
			final byte[] buf = new byte[in.available() > 1024 ? 4096 : 1024];
			int pos = 0;
			if (firstByte != -1) {
				buf[0] = (byte) firstByte;
				pos = 1;
			}
			int len = in.read(buf, pos, buf.length) + pos;
			*/
			final byte[] buf = new byte[1024];
			int len = in.read(buf);
			while (len != -1) {
				final int minRequired = offset + len; // 可能溢出
				final int oldLength = body.length;
				if (minRequired > oldLength) {
					int newLength = newLength(oldLength, minRequired - oldLength, oldLength);
					byte[] newBody = Arrays.copyOf(body, newLength);
					System.arraycopy(buf, 0, newBody, offset, len);
					body = newBody;
				} else {
					System.arraycopy(buf, 0, body, offset, len);
				}
				offset = minRequired;
				len = in.read(buf);
			}
			if (offset != body.length) {
				body = Arrays.copyOf(body, offset);
			}
		}
		return body;
	}

	// see jdk.internal.util.ArraysSupport.newLength( )
	private static int newLength(int oldLength, int minGrowth, int prefGrowth) {
		// preconditions not checked because of inlining
		// assert oldLength >= 0
		// assert minGrowth > 0

		int prefLength = oldLength + Math.max(minGrowth, prefGrowth); // might overflow
		if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
			return prefLength;
		} else {
			// put code cold in a separate method
			return hugeLength(oldLength, minGrowth);
		}
	}

	// see jdk.internal.util.ArraysSupport.hugeLength( )
	private static int hugeLength(int oldLength, int minGrowth) {
		int minLength = oldLength + minGrowth;
		if (minLength < 0) { // overflow
			throw new OutOfMemoryError("Required array length " + oldLength + " + " + minGrowth + " is too large");
		} else if (minLength <= SOFT_MAX_ARRAY_LENGTH) {
			return SOFT_MAX_ARRAY_LENGTH;
		} else {
			return minLength;
		}
	}

	@NonNull
	public static LogContext getContext(ServletRequest request) {
		return HANDLER.getContext(request);
	}

	@NonNull
	public static HttpServletRequest getRequest() {
		return HANDLER.getRequest();
	}

	public static void setRequestBody(String responseBody, ServletRequest request) {
		HANDLER.setRequestBody(responseBody, request);
	}

	public static void setResponse(Object response, ServletRequest request) {
		HANDLER.setResponse(response, request);
	}

	public static void setException(Throwable ex, ServletRequest request) {
		HANDLER.setException(ex, request);
	}

	public static void setParamAppender(@NonNull ParamAppender appender, ServletRequest request) {
		HANDLER.setParamAppender(appender, request);
	}

	public static void addExtItem(@NonNull Object part, ServletRequest request) {
		HANDLER.addExtItem(part, request);
	}

	/**
	 * 记录指定请求的参数数据，格式为：
	 *
	 * <pre>
	 * $prefix[userId=$userId，IP=$IP]
	 * 参数：a=aValue&b=bValue
	 * </pre>
	 */
	public static StringBuilder logRequest(final HttpServletRequest request, @Nullable StringBuilder sb, @Nullable String prefix,
	                                       @Nullable final Consumer<String> jsonBodySetter,
	                                       @Nullable final Consumer<Map<String, String>> paramMapSetter,
	                                       @Nullable String... headerNames) {
		final Map<String, String[]> paramMap = request.getParameterMap();
		if (sb == null) {
			sb = new StringBuilder(32 + (paramMap.size() << 4));
		}
		if (prefix != null) {
			sb.append(prefix);
		}
		// sb.append("[IP=").append(Context.get().getClientIP(request)).append("] ").append(request.getMethod()).append(' ').append(request.getRequestURL());
		if (headerNames != null && headerNames.length > 0) {
			boolean first = true;
			for (String name : headerNames) {
				String value = request.getHeader(name);
				if (value == null) {
					continue;
				}
				if (first) {
					sb.append("\n请求头：");
					first = false;
				}
				sb.append('\n').append(name).append(": ").append(value);
			}
		}
		sb.append("\n请求数据：");
		final String contentType = request.getContentType();
		if (contentType != null && contentType.contains("json")) {
			String requestData;
			try {
				requestData = IOUtils.toString(request.getReader());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			if (jsonBodySetter != null) {
				jsonBodySetter.accept(requestData);
			}
			sb.append('\n').append(requestData);
		} else {
			final Map<String, String> map = CollectionUtil.newHashMap(paramMap.size());
			boolean notFirst = false;
			for (Entry<String, String[]> entry : paramMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue()[0];
				map.put(key, value);
				if (notFirst) {
					sb.append('&');
				} else {
					notFirst = true;
				}
				sb.append(key).append('=').append(value);
			}
			if (paramMapSetter != null) {
				paramMapSetter.accept(map);
			}
			// 如果是文件上传，追加结尾
			final boolean uploadFile = "POST".equals(request.getMethod()) && contentType != null && contentType.startsWith("multipart/");
			if (uploadFile) {
				if (!paramMap.isEmpty()) {
					sb.append('&');
				}
				sb.append("[[文件]]");
			}
		}
		return sb.append('\n');
	}

	@FunctionalInterface
	public interface ParamAppender {

		void append(String name, String[] values, StringBuilder sb, HttpServletRequest request);

		ParamAppender DEFAULT = (name, values, sb, request) -> {
			sb.append(name).append('=');
			if (values.length == 1) {
				sb.append(values[0]);
			} else {
				sb.append('[');
				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(values[i]);
				}
				sb.append(']');
			}
		};

	}

	public interface LogContextHandler {

		@NonNull
		LogContext getContext(ServletRequest request);

		/** 快速获取当前请求对象（仅供处理器自身使用），有可能返回 null */
		default HttpServletRequest getRequest() {
			RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				return ((ServletRequestAttributes) attributes).getRequest();
			}
			return null;
		}

		default void setRequestBody(String responseBody, ServletRequest request) {
			getContext(request).requestBody = responseBody;
		}

		default void setResponse(Object response, ServletRequest request) {
			getContext(request).response = response;
		}

		default void setException(Throwable ex, ServletRequest request) {
			getContext(request).exception = ex;
		}

		default void setParamAppender(@NonNull ParamAppender appender, ServletRequest request) {
			getContext(request).paramAppender = appender;
		}

		default void addExtItem(Object part, ServletRequest request) {
			getContext(request).addExtItem(part);
		}

	}

	public static class DefaultLogContextHandler implements LogContextHandler {

		public static final String ATTR_NAME = Logs.class.getName() + ".logContext";

		@Override
		public @NonNull LogContext getContext(ServletRequest request) {
			LogContext context = (LogContext) request.getAttribute(ATTR_NAME);
			if (context == null) {
				request.setAttribute(ATTR_NAME, context = new LogContext());
			}
			return context;
		}

	}

	public final static class FastContextHandler implements LogContextHandler {

		public static final ThreadLocal<LogContext> THREAD_LOCAL = ThreadLocal.withInitial(LogContext::new);

		@Override
		public @NonNull LogContext getContext(ServletRequest request) {
			return THREAD_LOCAL.get();
		}

		@Override
		public HttpServletRequest getRequest() {
			return null;
		}

	}

	public static class LogContext {

		@NonNull
		public StringBuilder sb = new StringBuilder(initCapacity);

		public String requestBody;
		public Object response;
		public Throwable exception;
		public ParamAppender paramAppender;
		/** 可能是一个 Object，也可能是一个 Object 数组 */
		public Object ext;

		public void init(@Nullable HttpServletRequest request) {
			int capacity = sb.capacity(), expected = request == null ? -1 : request.getContentLength();
			if (expected > capacity) {
				sb.ensureCapacity(Math.min(maxCapacity << 4, expected));
			} else if (capacity > maxCapacity) {
				sb = new StringBuilder(Math.max(expected, initCapacity));
			}
			if (paramAppender != null) { // 以防万一，检测到 cleanUp() 未执行就再次清理
				cleanUp();
			}
			paramAppender = ParamAppender.DEFAULT;
		}

		public void cleanUp() {
			sb.setLength(0);
			requestBody = null;
			response = null;
			exception = null;
			paramAppender = null;
			ext = null;
		}

		public void addExtItem(Object part) {
			if (ext == null) {
				ext = part;
			} else if (ext instanceof Object[] array) {
				Object[] parts = Arrays.copyOf(array, array.length + 1);
				parts[array.length] = part;
				ext = parts;
			} else {
				ext = new Object[] { ext, part };
			}
		}

	}

}