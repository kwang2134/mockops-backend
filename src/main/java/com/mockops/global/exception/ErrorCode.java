package com.mockops.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {
    // 시스템
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),

    // auth 관련
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "유효하지 않은 provider입니다."),

    // 인증 관련
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

    // OAuth2 관련
    OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 Provider입니다."),
    OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "OAuth2 인증에 실패했습니다."),
    OAUTH2_USER_INFO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth2 사용자 정보를 가져오는데 실패했습니다."),
    OAUTH2_TOKEN_EXCHANGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth2 토큰 교환에 실패했습니다."),
    OAUTH2_PROVIDER_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "OAuth2 Provider 연결에 실패했습니다."),

    // 유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다."),
    DUPLICATE_AUTH_SOCIAL(HttpStatus.CONFLICT,"이미 연결된 소셜 로그인 플랫폼입니다."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST,"존재하지 않는 토큰입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 프로젝트 관련
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다."),
    PROJECT_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 프로젝트 이름입니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트 멤버입니다."),
    PROJECT_MEMBER_DUPLICATED(HttpStatus.CONFLICT, "이미 프로젝트에 참여 중인 멤버입니다."),
    PROJECT_CORS_ORIGIN_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 CORS Origin입니다."),
    PROJECT_CORS_ORIGIN_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 CORS Origin입니다."),

    // 초대 관련
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 초대장입니다."),
    INVITATION_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "이미 수락된 초대장입니다."),
    INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 초대장입니다."),
    INVITATION_CANCELED(HttpStatus.BAD_REQUEST, "취소된 초대장입니다."),
    INVITATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 이메일로 초대장이 발송되었습니다."),

    // 메일 관련
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public BusinessException serviceException() {
        return new BusinessException(this, ErrorType.SERVICE);
    }

    public BusinessException serviceException(String detail) {
        return new BusinessException(this, detail, ErrorType.SERVICE);
    }

    public BusinessException domainException() {
        return new BusinessException(this, ErrorType.DOMAIN);
    }

    public BusinessException domainException(String detail) {
        return new BusinessException(this, detail, ErrorType.DOMAIN);
    }


}
