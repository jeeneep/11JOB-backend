package com.the11job.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String errorCode;

    //BaseExceptionType을 통해 메시지와 에러 코드 생성
    public static ErrorResponse from(BaseExceptionType exceptionType) {
        return new ErrorResponse(exceptionType.getErrorMessage(), exceptionType.getErrorCode());
    }

    //String 메시지를 처리하는 메서드 추가
    public static ErrorResponse from(String message) {
        return new ErrorResponse(message, "UNKNOWN_ERROR");
    }

    // MethodArgumentNotValidException 처리 (검증 예외 처리)
    public static ErrorResponse from(MethodArgumentNotValidException e) {
        StringBuilder message = new StringBuilder();
        StringBuilder errorCode = new StringBuilder();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            message.append(fieldError.getDefaultMessage()).append(" ");
            errorCode.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(" ");
        }

        return new ErrorResponse(message.toString(), errorCode.toString());
    }


}
