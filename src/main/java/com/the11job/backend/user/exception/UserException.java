package com.the11job.backend.user.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends BaseException {


    // BaseException의 생성자를 호출하도록 수정합니다.
    public UserException(ErrorCode errorCode) {
        // BaseException(ErrorCode errorCode) 호출 -> ErrorCode의 기본 메시지 사용
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, String customMessage) {
        // BaseException(ErrorCode errorCode, String customMessage) 호출 -> 커스텀 메시지 사용
        super(errorCode, customMessage);
    }

    public UserException(ErrorCode errorCode, Throwable cause) {
        // BaseException(ErrorCode errorCode, Throwable cause) 호출 -> 근본 원인 전달
        super(errorCode, cause);
    }

    public UserException(ErrorCode errorCode, String customMessage, Throwable cause) {
        // BaseException(ErrorCode errorCode, String customMessage, Throwable cause) 호출
        super(errorCode, customMessage, cause);
    }
}