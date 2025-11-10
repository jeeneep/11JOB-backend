package com.the11job.backend.global.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 생성자: ErrorCode의 기본 메시지를 사용
    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 커스텀 메시지 생성자: ErrorCode를 사용하되, 메시지는 CustomMessage를 사용
    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    // 추가 요청 생성자: 커스텀 메시지와 근본 원인(Throwable)을 함께 전달

    /**
     * ErrorCode와 함께 사용자 정의 메시지, 근본 원인(e.g., JAXBException)을 전달
     *
     * @param errorCode     발생한 에러 코드
     * @param customMessage 사용자에게 보여줄 메시지 (또는 로그 메시지)
     * @param cause         예외의 근본 원인 (Throwable)
     */
    public BaseException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause); // RuntimeException의 생성자 (message, cause) 호출
        this.errorCode = errorCode;
    }

    // 근본 원인만 전달 (메시지는 ErrorCode의 기본 메시지를 사용)
    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}