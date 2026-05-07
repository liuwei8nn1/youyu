package com.youyu.common.exception;

import com.youyu.common.model.Result;import lombok.Getter;


@Getter
public class PermissionException extends RuntimeException {

	private String code = Result.PERMISSION_DENIED;

	public PermissionException(String code, String message) {
		super(message);
		this.code = code;
	}

    public PermissionException(String message) {
	    super(message);
    }


}
