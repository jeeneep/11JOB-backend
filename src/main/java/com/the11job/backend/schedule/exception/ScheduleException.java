package com.the11job.backend.schedule.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;

/**
 * Schedule 도메인 예외 클래스
 */
public class ScheduleException extends BaseException {

    // 에러 코드와 기본 메시지를 받는 생성자
    public ScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }

    // 에러 코드와 상세 메시지를 받는 생성자 (디버깅 또는 사용자에게 더 자세한 정보 전달용)
    public ScheduleException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}