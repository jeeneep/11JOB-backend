package com.the11job.backend.job.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;

/**
 * Job 도메인 예외 클래스
 */
public class JobException extends BaseException {

    // 필요한 ErrorCode를 생성자로 전달
    public JobException(ErrorCode errorCode) {
        super(errorCode);
    }

    // (선택적) 특정 상황에 맞는 메시지를 전달하고 싶을 때
    public JobException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

