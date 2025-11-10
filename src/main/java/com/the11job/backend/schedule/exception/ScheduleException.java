package com.the11job.backend.schedule.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;

/**
 * Schedule 도메인 예외 클래스
 */
public class ScheduleException extends BaseException {

    public ScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ScheduleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}