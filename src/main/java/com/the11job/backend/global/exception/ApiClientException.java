package com.the11job.backend.global.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;

// BaseException을 상속하여 ExceptionControllerAdvice에서 통합 처리되도록 합니다.
public class ApiClientException extends BaseException {

    // ErrorCode만 받는 생성자
    public ApiClientException(ErrorCode errorCode) {
        super(errorCode);
    }

    // ErrorCode와 사용자 정의 메시지, 근본 원인(Throwable)을 받는 생성자
    public ApiClientException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause); // BaseException에 이 생성자가 있어야 함
    }

    // (참고: BaseException에 customMessage와 cause를 받는 생성자를 추가해야 합니다.)
}
