package com.youyu.framework.validator;

/**
 * 验证异常
 * <p>
 * 当表单验证失败时抛出的运行时异常。
 * 包含了所有验证失败的错误信息。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // Controller 中自动处理
 * @PostMapping("/register")
 * public Result register(@Valid @RequestBody UserRegisterForm form) {
 *     // 如果验证失败，会自动抛出 ValidateException
 *     // Spring 的全局异常处理器会捕获并返回友好的错误信息
 * }
 * 
 * // 手动抛出
 * if (password != confirmPassword) {
 *     throw new ValidateException("两次密码输入不一致");
 * }
 * }</pre>
 *
 * @see ValidateError 验证错误消息
 * @since 1.0
 */
public class ValidateException extends RuntimeException {

	/**
	 * 构造空验证异常
	 */
	public ValidateException() {
	}

	/**
	 * 构造带消息的验证异常
	 *
	 * @param message 错误消息
	 */
	public ValidateException(String message) {
		super(message);
	}

	/**
	 * 构造带消息和原因的验证异常
	 *
	 * @param message 错误消息
	 * @param cause   原始异常
	 */
	public ValidateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 构造带原因的验证异常
	 *
	 * @param cause 原始异常
	 */
	public ValidateException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造完整的验证异常
	 *
	 * @param message            错误消息
	 * @param cause              原始异常
	 * @param enableSuppression  是否启用抑制
	 * @param writableStackTrace 是否可写堆栈跟踪
	 */
	public ValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
