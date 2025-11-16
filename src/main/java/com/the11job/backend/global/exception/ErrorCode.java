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

    // ---------------------- 6. Project 도메인 오류 (P-Codes) ----------------------
    NOT_FOUND_PROJECT(HttpStatus.NOT_FOUND, "P404", "요청하신 프로젝트 정보를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P403", "해당 프로젝트에 접근 권한이 없습니다."),

    // ---------------------- 7. User 도메인 오류 (U-Codes) ----------------------
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "U401", "인증 정보가 유효하지 않습니다."), // 일반적인 인증 실패/미인증
    ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "U400_1", "이미 가입된 이메일입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "U400_2", "잘못된 형식의 이메일입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "U400_3", "잘못된 형식의 비밀번호입니다."),
    WRONG_EMAIL_AUTHCODE(HttpStatus.BAD_REQUEST, "U400_4", "이메일 인증 번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "U403_1", "이메일 인증이 완료되지 않았습니다."),
    USER_NOT_EXIST(HttpStatus.NOT_FOUND, "U404_1", "사용자가 존재하지 않습니다."),
    USER_INVALID_ID_AND_PASSWORD(HttpStatus.UNAUTHORIZED, "U401_1", "아이디나 비밀번호가 다릅니다."),
    USER_WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "U401_2", "비밀번호가 일치하지 않습니다."),
    USER_FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "U403_2", "이 리소스에 접근할 권한이 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}