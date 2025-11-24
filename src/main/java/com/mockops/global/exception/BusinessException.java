package com.mockops.global.exception;

import lombok.Getter;


@Getter
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;
    private final String developerMessage;
    private final ErrorType errorType;

    public BusinessException(ErrorCode errorCode, String developerMessage, ErrorType errorType) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
        this.errorType = errorType;
    }

    public BusinessException(ErrorCode errorCode, ErrorType errorType) {
        this.errorCode = errorCode;
        this.developerMessage = null;
        this.errorType = errorType;
    }
}
