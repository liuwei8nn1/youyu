package com.youyu.common.exception;

import com.youyu.common.model.Result;

/**
 * 错误信息异常类，专门用于抛出业务逻辑层面的错误信息内容
 */
public class ErrorMessageException extends RuntimeException {

	/** 报错时，用于对外输出的对象（如果是字符串，则直接输出；如果是其他对象，则由异常解析器自定义输出，一般是输出为对象的 JSON 字符串） */
	protected Object output;
	/** 是否上报异常告警通知，默认不上报 */
	protected boolean report;

	public ErrorMessageException(String message, boolean writableStackTrace) {
		super(message, null, false, writableStackTrace);
		output = Result.error(message);
	}

	public ErrorMessageException(String message) {
		this(message, true);
	}

	public ErrorMessageException(String message, Object ext) {
		super(message, null, false, true);
		output = Result.error(message).setExt(ext);
	}

	public ErrorMessageException(String message, String status, boolean writableStackTrace) {
		super(message, null, true, writableStackTrace);
		output = Result.status(status, message);
	}

	public ErrorMessageException(String message, String status) {
		this(message, status, true);
	}

	public ErrorMessageException(String message, Throwable cause, boolean writableStackTrace) {
		super(message, cause, true, writableStackTrace);
		output = Result.error(message);
	}

	public ErrorMessageException(String message, Throwable cause) {
		this(message, cause, true);
	}

	public ErrorMessageException(Result<?> result, boolean writableStackTrace) {
		this(result, null, writableStackTrace);
	}

	public ErrorMessageException(Result<?> message) {
		this(message, null, true);
	}

	public ErrorMessageException(Result<?> result, Throwable cause, boolean writableStackTrace) {
		super(result.getMessage(), cause, true, writableStackTrace);
	}

	public ErrorMessageException(Result<?> msger, Throwable cause) {
		this(msger, cause, true);
	}

	public Result<?> getResult() {
		return (Result<?>) output;
	}

	public ErrorMessageException report(boolean report) {
		this.report = report;
		return this;
	}

	public ErrorMessageException report() {
		return report(true);
	}

}
