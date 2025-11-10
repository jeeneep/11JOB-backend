package com.the11job.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    private String errorCode;
    private String message;

    // ErrorCode Enum을 받아 응답 생성
    public static ExceptionResponse from(ErrorCode errorCode) {
        return new ExceptionResponse(errorCode.getCode(), errorCode.getMessage());
    }

    // ErrorCode와 커스텀 메시지를 받아 응답 생성 (BaseException 처리용)
    public static ExceptionResponse from(ErrorCode errorCode, String customMessage) {
        // 커스텀 메시지가 전달된 경우 사용하고, 아니면 ErrorCode 기본 메시지를 사용
        String finalMessage =
                (customMessage != null && !customMessage.isEmpty()) ? customMessage : errorCode.getMessage();
        return new ExceptionResponse(errorCode.getCode(), finalMessage);
    }

    // MethodArgumentNotValidException 처리
    public static ExceptionResponse from(MethodArgumentNotValidException e) {
        // 첫 번째 유효성 검사 실패 필드의 오류 메시지를 사용합니다.
        // 여러 개의 오류가 있을 경우 리스트로 응답을 구성할 수도 있지만, 간단하게 첫 오류만 사용합니다.

        // 기본 에러 코드 (ex: G400)
        ErrorCode baseCode = ErrorCode.INVALID_INPUT_VALUE; // <--- ErrorCode enum에 추가 필요

        FieldError fieldError = e.getBindingResult().getFieldError();
        String field = (fieldError != null) ? fieldError.getField() : "N/A";
        String defaultMessage = (fieldError != null) ? fieldError.getDefaultMessage() : "유효하지 않은 입력 값입니다.";

        // 반환 메시지: "필드명: 오류 메시지"
        String finalMessage = String.format("필드 오류 [%s]: %s", field, defaultMessage);

        return new ExceptionResponse(baseCode.getCode(), finalMessage);
    }
}
