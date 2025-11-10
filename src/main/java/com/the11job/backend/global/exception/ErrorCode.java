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

    // A. 요청 처리 및 유효성
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G400", "유효하지 않은 입력 값입니다."),
    FILE_SIZE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "G401", "업로드 파일 크기가 제한을 초과했습니다."),

    // B. 서버 내부 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G999", "서버 처리 중 오류가 발생했습니다."),
    UNSUPPORTED_ENCODING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G501", "데이터 인코딩 중 오류가 발생했습니다."),


    // ---------------------- 2. 외부 API 통신 오류 ----------------------
    API_EXTERNAL_COMMUNICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A500", "외부 API 통신에 실패했습니다."),
    API_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A501", "외부 API 응답 데이터 처리 중 오류가 발생했습니다."),
    API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "A429", "외부 API 호출 한도를 초과했습니다."),

    // ---------------------- 3. Job 도메인 오류 (J-Codes) ----------------------
    NOT_FOUND_JOB(HttpStatus.NOT_FOUND, "J404", "요청하신 채용 공고 정보를 찾을 수 없습니다."),
    // ---------------------- 4. Company 도메인 오류 (C-Codes) ----------------------
    NOT_FOUND_COMPANY(HttpStatus.NOT_FOUND, "C404", "요청하신 기업 정보를 찾을 수 없습니다."),

    // ---------------------- 5. Schedule 도메인 오류 (S-Codes) ----------------------
    NOT_FOUND_SCHEDULE(HttpStatus.NOT_FOUND, "S404", "요청하신 일정 정보를 찾을 수 없습니다."),
    SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S403", "해당 일정에 접근 권한이 없습니다."),

    // ---------------------- 6. User 도메인 오류 (U-Codes, 미래 확장) ----------------------
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "U401", "인증 정보가 유효하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}