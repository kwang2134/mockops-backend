package com.mockops.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();

        log.error("[{}] {} - {}",
                exception.getErrorType(),
                errorCode.name(),
                exception.getDeveloperMessage(),
                exception
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();

        log.error("validation 예외: {}", errorMessage, ex);

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, errorMessage, request.getRequestURI()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEEDED;

        log.error("파일 크기 초과: 최대 허용 크기를 초과한 파일 업로드 시도 - {}", ex.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error("예상하지 못한 에러 발생: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 공격성 요청(Actuator 스캔, 환경변수 파일 등)은 INFO 레벨로 짧게 기록하거나 DEBUG로 낮춤
        log.info("Blocked invalid access attempt (404): {}", uri);

        // 클라이언트에게는 그냥 404를 반환 (더 이상의 정보 노출 방지)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
