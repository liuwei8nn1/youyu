package com.youyu.common.exception;

import com.youyu.common.model.Result;
import lombok.Getter;

/**
 * 认证失败异常
 */
@Getter
public class AuthFailException extends RuntimeException {

	private String code = Result.UNAUTHORIZED;

	public AuthFailException(String code, String message) {
		super(message);
		this.code = code;
	}

    public AuthFailException(String message) {
	    super(message);
    }


}
