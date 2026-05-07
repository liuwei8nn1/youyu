package com.youyu.gateway.filter;

import com.youyu.auth.api.GateWayCheckUser;
import com.youyu.auth.api.JwtTokenProvider;
import com.youyu.framework.context.I18N;
import com.youyu.common.util.StringUtil;
import com.youyu.common.constant.BaseI18nKey;
import com.youyu.framework.context.*;
import com.youyu.framework.context.web.util.TraceIdGenerator;
import com.youyu.common.model.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.youyu.framework.context.UserInfo.JWT_USERINFO;

/**
 * JWT 认证过滤器（全局过滤器）
 * <p>
 * 执行顺序：第二个执行（order = -1），在 GatewayLogFilter 之后
 * <p>
 * 职责：
 * 1. 解析 JWT Token（如果存在）
 * 2. 验证用户状态（Redis 检查是否被禁用、设备是否在线）
 * 3. 将 UserInfo 存入 exchange attributes（供后续 Filter 使用）
 * 4. 从 exchange attributes 获取 TraceId，与用户信息一起添加到请求头传递给下游服务
 * <p>
 * 注意：
 * - 如果请求中没有 Token，userInfo = null，直接放行（由 PermissionFilter 决定是否需要登录）
 * - 如果有 Token 但验证失败，返回 401 错误
 * - 如果用户被禁用或设备不在线，返回 403 错误
 * - MDC 由 GatewayLogFilter 管理，此 Filter 不负责清理
 */
@Slf4j
@Component
public class JwtFilter implements GlobalFilter, Ordered {

	private final JwtTokenProvider jwtTokenProvider;
	private final ReactiveStringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JwtFilter(JwtTokenProvider jwtTokenProvider, 
	                 ReactiveStringRedisTemplate redisTemplate) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		// 1️⃣ 从 exchange attributes 获取 TraceId（由 GatewayLogFilter 设置）
		String traceId = (String) exchange.getAttributes().get(TraceIdGenerator.TRACE_ID);
		if (StringUtil.isEmpty(traceId)) {
			log.warn("TraceId not found in exchange attributes, this should not happen!");
			traceId = "UNKNOWN";
		}

		// 2️⃣ 提取并解析 JWT Token（可选）
		String token = extractToken(request);
		
		if (StringUtil.isEmpty(token)) {
			// 没有 Token，也直接放行
			UserInfo userInfo = UserInfo.of(traceId);
			exchange.getAttributes().put(JWT_USERINFO, userInfo);
			
			// 添加 TraceId 到请求头
			ServerHttpRequest modifiedRequest = request.mutate()
					.header(TraceIdGenerator.TRACE_ID_HEADER, traceId)
					.build();
			
			return chain.filter(exchange.mutate().request(modifiedRequest).build());
		}

		// 有 Token，需要验证
		try {
			// 验证 Token 签名和有效期
			Claims claims = jwtTokenProvider.validateToken(token);
			UserInfo userInfo = jwtTokenProvider.getUserInfo(claims);

			// 3️⃣ 验证用户状态（Redis 检查）
			List<String> checkRedisKeys = GateWayCheckUser.getCheckRedisKeys(userInfo);
			
			// 将 traceId 声明为 final，以便在 lambda 中使用
			final String finalTraceId = traceId;
			
			return redisTemplate.opsForValue().multiGet(checkRedisKeys)
					.flatMap(values -> {
						String checkRes = GateWayCheckUser.check4RedisValues(values);
						if (checkRes != null) {
							// 用户被禁用或设备不在线
							return writeForbiddenResponse(exchange, checkRes);
						}
						userInfo.setTraceId(finalTraceId);
						// 4️⃣ 将用户信息存入 attributes（供后续 Filter 使用）
						exchange.getAttributes().put(JWT_USERINFO, userInfo);

						// 5️⃣ 将 TraceId 和用户信息添加到请求头（传递给下游服务）
						ServerHttpRequest modifiedRequest = request.mutate()
								.header(TraceIdGenerator.TRACE_ID_HEADER, finalTraceId)
								.header(UserContextUtils.USER_ID_HEADER, userInfo.getUserId().toString())
								.header(UserContextUtils.USER_TYPE_HEADER, userInfo.getUserType().toString())
								.header(UserContextUtils.USER_NAME_HEADER, userInfo.getUsername())
								.header(UserContextUtils.USER_ROLES_HEADER, userInfo.getRoles())
								.header(UserContextUtils.DEVICE_ID_HEADER, userInfo.getDeviceId().toString())
								.build();

						return chain.filter(exchange.mutate().request(modifiedRequest).build());
					});

		} catch (ExpiredJwtException e) {
			// Token 过期
			return writeUnauthorizedResponse(exchange, BaseI18nKey.AUTH_TOKEN_INVALID);
		} catch (Exception e) {
			// Token 无效
			log.error("JWT Token 验证失败: {}", e.getMessage(), e);
			return writeUnauthorizedResponse(exchange, BaseI18nKey.AUTH_TOKEN_INVALID);
		}
	}

	/**
	 * 从请求头中提取 Token
	 */
	private String extractToken(ServerHttpRequest request) {
		String authorization = request.getHeaders().getFirst("Authorization");
		if (authorization != null && authorization.startsWith("Bearer ")) {
			return authorization.substring(7);
		}
		return null;
	}

	/**
	 * 写入未授权响应（401）
	 */
	private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String messageCode) {
		return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, Result.UNAUTHORIZED, messageCode);
	}

	/**
	 * 写入禁止访问响应（403）
	 */
	private Mono<Void> writeForbiddenResponse(ServerWebExchange exchange, String messageCode) {
		return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, Result.FORBIDDEN, messageCode);
	}

	/**
	 * 写入错误响应
	 */
	private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, 
	                                      String code, String messageCode) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		// 使用 I18N 获取国际化消息
		String message = I18N.msg(messageCode);
		Result<Void> result = Result.error(code, message);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(result);
			DataBuffer buffer = response.bufferFactory().wrap(bytes);
			return response.writeWith(Mono.just(buffer));
		} catch (JsonProcessingException e) {
			log.error("序列化错误响应失败", e);
			return response.setComplete();
		}
	}

	@Override
	public int getOrder() {
		// 第二个执行，在 GatewayLogFilter 之后
		return -1;
	}
}
