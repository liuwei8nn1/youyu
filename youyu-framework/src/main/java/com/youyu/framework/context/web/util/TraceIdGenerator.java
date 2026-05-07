package com.youyu.framework.context.web.util;

import java.util.function.Supplier;

import com.youyu.framework.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * TraceId 生成和提取工具类
 * <p>
 * 职责：
 * 1. 从请求头中提取 TraceId
 * 2. 如果不存在则生成新的 TraceId
 * 3. 保证全链路 TraceId 一致性
 */
public class TraceIdGenerator {

    public static final String TRACE_ID = "TraceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 从请求中获取或生成 TraceId
     * <p>
     * 如果请求头中包含 X-Trace-Id，则直接返回；
     * 否则生成一个新的 TraceId
     *
     * @param request HTTP 请求
     * @return TraceId（永远不会为 null）
     */
    @NonNull
    public static String generateOrGet(@Nullable HttpServletRequest request) {
        if (request != null) {
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId != null && !traceId.isEmpty()) {
                return traceId;
            }
        }
        return generate();
    }

    /**
     * 生成新的 TraceId
     * <p>
     * 格式：UUID 去掉横杠，32位十六进制字符串
     *
     * @return 新生成的 TraceId
     */
    @NonNull
    public static String generate() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }


    @Nullable
    public static String getTraceId(@Nullable HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        return null;
    }

    /**
     * 验证 TraceId 是否有效
     *
     * @param traceId 待验证的 TraceId
     * @return 是否有效
     */
    public static boolean isValid(@Nullable String traceId) {
        return traceId != null && !traceId.isEmpty();
    }

    /**
     * 获取当前 TraceId
     * 优先级：MDC > UserContextHolder
     */
    public static String getCurrentTraceIdOrGenerate(Supplier<String> traceIdSupplier) {
        // 方式1：从 MDC 获取（GlobalLogFilter 已设置）
        String traceId = ThreadContext.get(TraceIdGenerator.TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        // 方式2：从 UserContextHolder 获取
        traceId = UserContextHolder.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        return traceIdSupplier.get();
    }

    /**
     * 获取当前 TraceId
     * 优先级：MDC > UserContextHolder
     */
    public static String getCurrentTraceIdOrGenerate() {
        // 方式1：从 MDC 获取（GlobalLogFilter 已设置）
        String traceId = ThreadContext.get(TraceIdGenerator.TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        // 方式2：从 UserContextHolder 获取
        traceId = UserContextHolder.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        return TraceIdGenerator.generate();
    }

    @Nullable
    public static String getCurrentTraceId() {
        // 方式1：从 MDC 获取（GlobalLogFilter 已设置）
        String traceId = ThreadContext.get(TraceIdGenerator.TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        // 方式2：从 UserContextHolder 获取
        traceId = UserContextHolder.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        return null;
    }

}
