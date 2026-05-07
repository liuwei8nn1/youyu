package com.youyu.gateway.util;

import com.youyu.common.constant.BaseI18nKey;
import com.youyu.common.model.Result;
import com.youyu.common.util.JsonUtil;
import com.youyu.framework.context.I18N;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author LiuWei
 * @since 2026/4/25
 */
public abstract class WebUtil {

	public static Mono<Void> writeTooManyRequests(ServerHttpResponse response){
		return writeTooManyRequests(response);
	}
	public static Mono<Void> writeTooManyRequests(ServerHttpResponse response, Object ext){
		response.setStatusCode(HttpStatus.OK);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Result<Object> error = Result.error(Result.TOO_MANY_REQUESTS, I18N.msg(BaseI18nKey.SENTINEL_FLOW_CONTROL)).setExt(ext);
		String body = JsonUtil.toJson(error);

		DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
		return response.writeWith(Mono.just(buffer));
	}



}
