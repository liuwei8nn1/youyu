package com.youyu.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String message) {
        super(message);
        this.code = "400";
    }

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }
}
