package com.youyu.gateway.filter;

import com.youyu.auth.api.model.PermissionLevel;
import com.youyu.framework.context.UserType;
import com.youyu.common.util.CollectionUtil;
import com.youyu.common.util.StringUtil;
import com.youyu.common.constant.BaseI18nKey;
import com.youyu.framework.context.I18N;
import com.youyu.framework.context.UserInfo;
import com.youyu.common.model.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.youyu.framework.context.UserInfo.JWT_USERINFO;

/**
 * 权限检查过滤器（路由过滤器）
 * <p>
 * 执行顺序：在 GlobalFilter 之后执行，在 yml 中配置
 * <p>
 * 职责：
 * 1. 从 exchange attributes 中获取 UserInfo（由 JwtAndTraceIdFilter 设置）
 * 2. 检查用户类型是否满足权限要求（requiredUserType）
 * 3. 权限不足则返回 401（未登录）或 403（权限不足）
 * <p>
 * 配置示例：
 * <pre>
 * filters:
 *   - name: PermissionFilter
 *     args:
 *       requiredUserType: LOGIN        # 只需登录
 *       # requiredUserType: ADMIN      # 需要管理员
 *       # requiredUserType: ADMIN,EMP  # 管理员或员工
 * </pre>
 * <p>
 * 注意：
 * - 如果不配置 requiredUserType 或配置为空，表示无需权限，直接放行
 * - 如果配置了权限要求但 userInfo 为 null（未登录），返回 401
 * - 如果已登录但用户类型不满足要求，返回 403
 */
@Component
public class PermissionFilter extends AbstractGatewayFilterFactory<PermissionFilter.Config> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public PermissionFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		// 预解析权限配置并缓存（Config 是单例，只需解析一次）
		Set<PermissionLevel> requiredLevels = config.getRequiredLevels();

		return (exchange, chain) -> {
			// 如果无需权限，直接放行
			if (requiredLevels.isEmpty() || requiredLevels.contains(PermissionLevel.NONE)) {
				return chain.filter(exchange);
			}

			// 从 attributes 中获取用户信息（由 JwtAndTraceIdFilter 设置）
			Object userInfoObj = exchange.getAttributes().get(JWT_USERINFO);
			
			if (userInfoObj == null || !(userInfoObj instanceof UserInfo)) {
				// 未登录
				return writeUnauthorizedResponse(exchange, BaseI18nKey.AUTH_TOKEN_REQUIRED);
			}

			UserInfo userInfo = (UserInfo) userInfoObj;
			
			// 检查用户类型权限
			UserType userType = UserType.of(userInfo.getUserType());
			boolean hasPermission = PermissionLevel.hasPermission(requiredLevels, userType);
			
			if (!hasPermission) {
				// 权限不足
				return writeForbiddenResponse(exchange, BaseI18nKey.AUTH_PERMISSION_DENIED);
			}

			// 权限验证通过，继续执行
			return chain.filter(exchange);
		};
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
			return response.setComplete();
		}
	}

	@Data
	public static class Config {
		/**
		 * 要求的权限级别，支持以下值：
		 * - "" 或 "NONE": 无需权限，直接放行
		 * - "LOGIN": 只需登录即可
		 * - "USER": 仅普通用户
		 * - "EMP": 仅员工
		 * - "ADMIN": 仅管理员
		 * <p>
		 * 支持逗号分隔多个权限，满足其一即可，例如：
		 * - "ADMIN,EMP": 管理员或员工都可以访问
		 * - "USER,EMP,ADMIN": 所有登录用户都可以访问
		 */
		private String requiredUserType = "";
		
		/**
		 * 缓存解析后的权限级别列表（懒加载，线程安全）
		 */
		private volatile Set<PermissionLevel> cachedLevels;
		
		/**
		 * 获取解析后的权限级别列表（带缓存）
		 * 
		 * @return 权限级别列表
		 */
		public Set<PermissionLevel> getRequiredLevels() {
			// 双重检查锁定，确保线程安全且只解析一次
			if (cachedLevels == null) {
				synchronized (this) {
					if (cachedLevels == null) {
						cachedLevels = parseRequiredLevels();
					}
				}
			}
			return cachedLevels;
		}
		
		/**
		 * 解析权限配置（使用高性能的 StringUtil.splitAsStringList）
		 * 
		 * @return 权限级别列表
		 */
		private Set<PermissionLevel> parseRequiredLevels() {
			if (requiredUserType == null || requiredUserType.trim().isEmpty()) {
				return Collections.emptySet();
			}
			
			// 使用 StringUtil.splitAsStringList 高性能分割
			List<String> parts = StringUtil.splitAsStringList(requiredUserType);
			if (parts.isEmpty()) {
				return Collections.emptySet();
			}
			return CollectionUtil.toSet(parts, PermissionLevel::fromCode);
		}
	}
}
