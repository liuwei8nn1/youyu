package com.youyu.gateway.config;

import com.youyu.common.constant.BaseI18nKey;
import com.youyu.common.model.Result;
import com.youyu.common.util.JsonUtil;
import com.youyu.framework.context.I18N;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关全局异常处理器
 * <p>
 * 用于捕获网关层的所有异常，包括：
 * - 路由匹配失败（404）
 * - Filter执行异常
 * - 下游服务调用异常
 * - 其他运行时异常
 */
@Slf4j
@Order(-1)
@Component
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ServerHttpResponse response = exchange.getResponse();

		// 记录详细的异常信息
		log.error("========== 网关异常捕获 ==========");
		log.error("请求路径: {}", exchange.getRequest().getPath());
		log.error("请求方法: {}", exchange.getRequest().getMethod());
		log.error("请求头: {}", exchange.getRequest().getHeaders());
		log.error("异常类型: {}", ex.getClass().getName());
		log.error("异常消息: {}", ex.getMessage());

		// 打印完整堆栈
		if (log.isDebugEnabled()) {
			log.debug("异常堆栈:", ex);
		}

		// 判断异常类型
		if (ex instanceof ResponseStatusException) {
			ResponseStatusException rse = (ResponseStatusException) ex;
			log.error("HTTP状态码: {}", rse.getStatusCode());
			log.error("响应原因: {}", rse.getReason());
		}

		log.error("====================================");

		// 如果响应已经提交，直接返回
		if (response.isCommitted()) {
			return Mono.error(ex);
		}
		DataBufferFactory bufferFactory = response.bufferFactory();

		// 设置响应状态码和类型
		response.setStatusCode(HttpStatus.OK);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		String errorMessage = JsonUtil.toJson(Result.error(Result.ERROR, I18N.msg(BaseI18nKey.COMMON_SERVER_ERROR)));


		return response.writeWith(Mono.just(bufferFactory.wrap(errorMessage.getBytes())));
	}
}
