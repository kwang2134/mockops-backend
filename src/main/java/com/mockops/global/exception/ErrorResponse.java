package com.mockops.global.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonPropertyOrder({"success", "code", "message", "status", "path"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private final boolean success = false;
    private final String code;
    private final String message;
    private final Integer status;
    private final String path;

    public static ErrorResponse from(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                errorCode.getHttpStatus().value(),
                path
        );
    }

    public static ErrorResponse of(String message, int status, String path) {
        return new ErrorResponse(null, message, status, path);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                errorCode.name(),
                message,
                errorCode.getHttpStatus().value(),
                path
        );
    }
}
