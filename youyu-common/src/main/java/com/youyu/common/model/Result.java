package com.youyu.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Supplier;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result<T> implements Serializable {

    public static Supplier<String> successMsgSupplier = () -> "操作成功";

    @Serial
    private static final long serialVersionUID = 1L;
    public static final String SUCCESS = "200";
    public static final String ERROR = "500";
    public static final String TOO_MANY_REQUESTS = "429";
    /**
     * 未授权
     */
    public static final String UNAUTHORIZED = "401001";
    /**
     * 禁止访问响应
     */
    public static final String FORBIDDEN = "403001";
    /**
     * 权限不足
     */
    public static final String PERMISSION_DENIED = "403002";
    /** 响应码 */
    private String code;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;
    /** 预留扩展字段 */
    private Object ext;
    /** 跟踪ID */
    private String traceId;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(SUCCESS)
                .message(successMsgSupplier.get())
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(SUCCESS)
                .message(successMsgSupplier.get())
                .data(data)
                .build();
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(SUCCESS)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(ERROR)
                .message(message)
                .build();
    }

    /**
     * 失败响应（自定义错误码）
     */
    public static <T> Result<T> error(String code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> Result<T> error(String code, String message, String traceId) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .traceId(traceId)
                .build();
    }

	public static <T> Result<T> status(String status, String message) {
        return Result.<T>builder()
                .code(status)
                .message(message)
                .build();
	}

    public static <T> Result<T> status(String status, String message, String traceId) {
        return Result.<T>builder()
                .code(status)
                .message(message)
                .traceId(traceId)
                .build();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code.equals(SUCCESS);
    }
}
