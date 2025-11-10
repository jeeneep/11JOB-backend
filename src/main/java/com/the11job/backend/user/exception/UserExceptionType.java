package com.the11job.backend.user.exception;

import com.the11job.backend.global.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum UserExceptionType implements BaseExceptionType {

    // 회원가입 관련
    ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "JOIN_001", "이미 가입된 이메일입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "JOIN_002", "잘못된 형식의 이메일입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "JOIN_003", "잘못된 형식의 비밀번호입니다."),
    WRONG_EMAIL_AUTHCODE(HttpStatus.BAD_REQUEST, "JOIN_004", "이메일 인증 번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "JOIN_005","이메일 인증이 완료되지 않았습니다."),

    // 사용자 관련
    USER_NOT_EXIST(HttpStatus.NOT_FOUND, "USER_001", "사용자가 존재하지 않습니다."),
    USER_INVALID_ID_AND_PASSWORD(HttpStatus.UNAUTHORIZED, "USER_002", "아이디나 비밀번호가 다릅니다."),
    USER_WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "USER_003", "비밀번호가 일치하지 않습니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER_004", "사용자가 인증되지 않았습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "USER_005", "이 리소스에 접근할 권한이 없습니다.");


    private HttpStatus httpStatus;
    private String errorCode;
    private String errorMessage;

    UserExceptionType(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}