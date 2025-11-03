package com.the11job.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전역의 에러 코드를 통합 관리하는 Enum HTTP 상태, 고유 에러 코드, 사용자 메시지를 포함
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ---------------------- 1. Global (공통) Error ----------------------

    // 요청 처리 관련
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G400", "유효하지 않은 입력 값입니다. (MethodArgumentNotValidException 처리용)"),
    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G999", "서버 처리 중 오류가 발생했습니다."),
    UNSUPPORTED_ENCODING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G501", "데이터 인코딩 중 오류가 발생했습니다."),

    // 파일/요청 크기 초과
    FILE_SIZE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "G401", "업로드 파일 크기가 제한을 초과했습니다."),


    // ---------------------- 2. 외부 API 공통 오류 코드 추가 ----------------------
    API_EXTERNAL_COMMUNICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G500", "외부 API 통신에 실패했습니다."),
    API_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G501", "외부 API 응답 데이터 처리 중 오류가 발생했습니다."),
    API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "G429", "외부 API 호출 한도를 초과했습니다.");

    // 이 외에 필요한 도메인별 에러 코드를 계속 추가하여 통합 관리

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
