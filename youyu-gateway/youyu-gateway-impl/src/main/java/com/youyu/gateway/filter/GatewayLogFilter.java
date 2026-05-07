package com.youyu.gateway.filter;

import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson2.JSONObject;
import com.youyu.common.util.StringUtil;
import com.youyu.framework.context.UserInfo;
import com.youyu.framework.logging.rest.Logs;
import com.youyu.framework.context.web.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Gateway 全局请求日志过滤器（响应式 WebFlux 版本）
 * <p>
 * 执行顺序：第一个执行（order = -2），在 JwtFilter 之前
 * <p>
 * 职责：
 * 1. 生成或获取 TraceId，设置到 MDC 和 exchange attributes
 * 2. 装饰响应，捕获响应状态码和响应体
 * 3. 在响应式链完成后延迟记录完整日志
 * 4. 从 exchange attributes 中获取用户信息（由 JwtFilter 设置）
 * 5. 支持配置化开关和采样率
 * 6. 通过 bodyEnabled 开关控制请求体和响应体的记录
 * 7. doFinally 中清理 MDC
 * <p>
 * 注意：
 * - 这是 Gateway 专用的过滤器，使用 WebFlux 的 GlobalFilter
 * - TraceId 在此 Filter 中生成，通过 attributes 传递给 JwtFilter
 * - 用户信息由 JwtFilter 解析并设置到 attributes
 * - 生产环境建议关闭 bodyEnabled，以减少性能开销
 */
@Slf4j
@Component
public class GatewayLogFilter implements GlobalFilter, Ordered {

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
	 * 是否记录请求体和响应体
	 * <p>
	 * 默认为 false，生产环境建议关闭以减少性能开销
	 */
	@Value("${logging.request.body.enabled:false}")
	protected boolean bodyEnabled;

	private static final String BODY_DISABLED = "[BODY DISABLED]";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		final long startTime = System.currentTimeMillis();
		ServerHttpRequest request = exchange.getRequest();

		// 1️⃣ 生成或获取 TraceId（所有请求都需要）
		String traceId = request.getHeaders().getFirst(TraceIdGenerator.TRACE_ID_HEADER);
		if (StringUtil.isEmpty(traceId)) {
			traceId = TraceIdGenerator.generate();
			log.debug("Generated new TraceId: {}", traceId);
		} else {
			log.debug("Using existing TraceId: {}", traceId);
		}

		// 设置 TraceId 到 MDC（供 Log4j2 使用）
		MDC.put(TraceIdGenerator.TRACE_ID, traceId);

		// 将 TraceId 存入 exchange attributes（供其他 Filter 使用）
		exchange.getAttributes().put(TraceIdGenerator.TRACE_ID, traceId);

		// 2. 检查是否启用日志功能（快速失败，避免不必要的开销）
		if (!enabled) {
			return chain.filter(exchange)
					.doFinally(signalType -> MDC.clear()); // 即使不记录日志也要清理 MDC
		}

		// 3. 采样判断（减少日志量，降低性能影响）
		if (sampleRate < 1.0 && Math.random() > sampleRate) {
			return chain.filter(exchange)
					.doFinally(signalType -> MDC.clear()); // 采样过滤的请求也要清理 MDC
		}

		final String uri = request.getURI().getPath();
		final String method = request.getMethod().name();
		final String clientIp = request.getRemoteAddress() != null ?
				request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

		// 4. 条件化读取请求体（仅在 bodyEnabled 且 Content-Type 适合时才读取）
		Mono<String> requestBodyMono = bodyEnabled ? readRequestBody(request) : Mono.just(BODY_DISABLED);

		// 5. 装饰响应对象，捕获响应体和状态码
		ServerHttpResponse originalResponse = exchange.getResponse();
		DataBufferFactory bufferFactory = originalResponse.bufferFactory();

		// 用于存储响应体的引用（在响应式链中传递）
		final String[] responseBodyRef = new String[1];
		final Integer[] statusCodeRef = new Integer[1];
		final String finalTraceId = traceId;

		ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				if (body instanceof Flux) {
					Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

					return super.writeWith(fluxBody.map(dataBuffer -> {
						// 保存状态码（只设置一次）
						if (statusCodeRef[0] == null) {
							statusCodeRef[0] = getStatusCode() != null ? getStatusCode().value() : 200;
							// 设置 TraceId 到响应头（只设置一次）
							getHeaders().set(TraceIdGenerator.TRACE_ID_HEADER, finalTraceId);
						}
					
						// 只有在启用响应体记录时才读取内容
						if (bodyEnabled) {
							byte[] content = new byte[dataBuffer.readableByteCount()];
							dataBuffer.read(content);
							DataBufferUtils.release(dataBuffer); // 释放缓冲区，防止内存泄漏
							responseBodyRef[0] = new String(content, StandardCharsets.UTF_8);
							return bufferFactory.wrap(content);
						} else {
							responseBodyRef[0] = null;
							return dataBuffer;
						}
					}));
				}
				return super.writeWith(body);
			}

			@Override
			public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
				return writeWith(Flux.from(body).flatMapSequential(p -> p));
			}
		};

		// 6. 使用装饰后的响应对象继续过滤器链，并在完成后记录日志
		ServerWebExchange modifiedExchange = exchange.mutate()
				.response(decoratedResponse)
				.build();

		return chain.filter(modifiedExchange)
				.then(Mono.defer(() -> {
					// ⭐ 成功完成时延迟记录日志（此时 JwtFilter 已执行完毕）
					long endTime = System.currentTimeMillis();
					long useTimeMs = endTime - startTime;
					UserInfo userInfo = extractUserInfoFromAttributes(exchange);
					String finalResponseBody = responseBodyRef[0];
					Integer finalStatusCode = statusCodeRef[0] != null ? statusCodeRef[0] : 200;
					return requestBodyMono.doOnNext(requestBody -> 
						logRequest(userInfo, method, uri, request.getHeaders(), requestBody,
								finalResponseBody, clientIp, startTime, useTimeMs, finalStatusCode, finalTraceId, null)
					).then();
				}))
				.onErrorResume(error -> {
					// ⭐ 发生错误时也要延迟记录日志
					long endTime = System.currentTimeMillis();
					long useTimeMs = endTime - startTime;
					UserInfo userInfo = extractUserInfoFromAttributes(exchange);
					Integer finalStatusCode = decoratedResponse.getStatusCode() != null ?
							decoratedResponse.getStatusCode().value() : 500;
					return requestBodyMono.doOnNext(requestBody -> 
						logRequest(userInfo, method, uri, request.getHeaders(), requestBody,
								null, clientIp, startTime, useTimeMs, finalStatusCode, finalTraceId, error)
					).then(Mono.error(error)); // 重新抛出异常，保持原有行为
				})
				.doFinally(signalType -> {
					// 清理 MDC（此 Filter 负责 MDC 的完整生命周期）
					MDC.clear();
				});
	}

	/**
	 * 从 exchange attributes 中提取用户信息（由 JwtFilter 设置）
	 * <p>
	 * 注意：
	 * - 此方法在延迟记录日志时调用
	 * - 此时 JwtFilter 已经执行完毕，attributes 中已有用户信息
	 * - 对于公开接口（无 Token），返回 null
	 *
	 * @param exchange 交换对象
	 * @return 用户信息，未登录时返回 null
	 */
	private UserInfo extractUserInfoFromAttributes(ServerWebExchange exchange) {
		Object userInfoObj = exchange.getAttributes().get(UserInfo.JWT_USERINFO);
		if (userInfoObj instanceof UserInfo) {
			return (UserInfo) userInfoObj;
		}
		return null;
	}

	/**
	 * 读取请求体（仅当配置启用且 Content-Type 适合记录时）
	 * <p>
	 * 支持的 Content-Type：
	 * - application/json：JSON 格式的请求体
	 * - application/x-www-form-urlencoded：表单提交
	 * <p>
	 * 不支持的 Content-Type（返回空字符串）：
	 * - multipart/form-data：文件上传（避免记录大体积数据）
	 * - 其他二进制或非文本格式
	 * <p>
	 * 注意：
	 * - 如果 bodyEnabled = false，返回 "[BODY DISABLED]"
	 * - 读取请求体会消耗性能，生产环境建议关闭
	 *
	 * @param request 请求对象
	 * @return 请求体内容的 Mono，可能为空或禁用标记
	 */
	private Mono<String> readRequestBody(ServerHttpRequest request) {
		// 1. 检查 Content-Type
		MediaType contentType = request.getHeaders().getContentType();
		if (contentType == null) {
			return Mono.just("");
		}

		// 2. 排除文件上传（multipart/form-data）
		if (contentType.includes(MediaType.MULTIPART_FORM_DATA)) {
			// 文件上传不记录请求体，避免性能问题和敏感信息泄露
			return Mono.just("");
		}

		// 3. 只记录文本格式的请求体（JSON、表单等）
		if (contentType.includes(MediaType.APPLICATION_JSON) ||
				contentType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
			// 读取并拼接请求体（WebFlux 中请求体是分块的）
			return request.getBody()
					.map(dataBuffer -> {
						byte[] bytes = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(bytes);
						DataBufferUtils.release(dataBuffer); // 释放缓冲区，防止内存泄漏
						return bytes;
					})
					.reduce(new StringBuilder(), (sb, bytes) -> sb.append(new String(bytes, StandardCharsets.UTF_8)))
					.map(StringBuilder::toString)
					.defaultIfEmpty(""); // 如果没有请求体，返回空字符串
		}

		// 4. 其他 Content-Type 不记录
		return Mono.just("");
	}

	/**
 * 记录请求日志（JSON 格式）
 * <p>
 * 日志内容包括：
 * - 用户信息（userId，可能为 null）
 * - 应用名称、请求方法、URI
 * - 请求头（过滤掉不必要的头）
 * - 请求体（如果启用且为 JSON 或表单）
 * - 响应体（如果启用）
 * - 客户端 IP
 * - 耗时（毫秒）
 * - 响应状态码
 * - 异常信息（如果有）
 * <p>
 * 注意：
 * - 请求体和响应体的记录受 bodyEnabled 开关控制
 * - 表单提交（application/x-www-form-urlencoded）会被记录
 * - 文件上传（multipart/form-data）不会被记录
 *
 * @param userInfo     用户信息（由 JwtFilter 设置，可能为 null）
 * @param method       HTTP 方法（GET、POST 等）
 * @param uri          请求路径
 * @param headers      请求头
 * @param requestBody  请求体（可能为空或 "[BODY DISABLED]"）
 * @param responseBody 响应体（可能为 null，表示未启用记录）
 * @param clientIp     客户端 IP 地址
 * @param startTime    请求开始时间戳
 * @param useTimeMs    请求耗时（毫秒）
 * @param httpCode   HTTP 响应状态码
 * @param error        异常信息（正常请求为 null）
 */
	private void logRequest(UserInfo userInfo, String method, String uri, HttpHeaders headers,
							String requestBody, String responseBody, String clientIp,
							long startTime, long useTimeMs, Integer httpCode, String traceId, Throwable error) {
		try {
			// 1. 过滤请求头（移除不必要的头信息）
			JSONObject headersJson = new JSONObject();
			headers.forEach((name, values) -> {
				// 过滤掉 Cloudflare、AWS 等 CDN/代理的头
				if (name.startsWith("sec-") || 
					(name.startsWith("x-forwarded-") && !"x-forwarded-for".equals(name))) {
					return;
				}
				// 过滤掉其他不必要的头
				if ("x-amzn-trace-id".equals(name) || "cf-ray".equals(name) || 
						"cf-visitor".equals(name) || "prefer".equals(name) || 
						"dnt".equals(name) || "priority".equals(name)) {
					return;
				}
				// 保留第一个值（大多数头只有一个值）
				if (!values.isEmpty()) {
					headersJson.put(name, values.get(0));
				}
			});

			// 2. 记录 JSON 格式日志
			Logs.logJSON(
					userInfo != null ? userInfo.getUserId() : null, // userId 可能为 null
					appName,
					method,
					uri,
					headersJson,
					requestBody,
					clientIp,
					startTime,
					useTimeMs,
					responseBody,
					httpCode,
					traceId,
					error,
					null
			);
		} catch (Exception e) {
			log.error("Failed to log request", e);
		}
	}

	@Override
	public int getOrder() {
		// 第一个执行，在 JwtFilter 之前
		// 这样可以包裹整个过滤器链，准确统计耗时，并管理 TraceId 和 MDC 的生命周期
		return -99;
	}
}