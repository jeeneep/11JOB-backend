package com.the11job.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    // 모든 커스텀 예외 (BaseException 상속) 통합 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("Custom Exception [{}]: {}", errorCode.getCode(), ex.getMessage(), ex);

        // BaseException의 ErrorCode와 메시지를 사용하여 응답 생성
        ExceptionResponse response = ExceptionResponse.from(errorCode, ex.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    // ----------------------------------------------------------------------------------
    // Spring/Java 내장 예외 처리 (별도로 관리)

    // MethodArgumentNotValidException 처리 (검증 예외 처리)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);

        // ErrorCode 기반의 ExceptionResponse.from(MethodArgumentNotValidException) 호출
        ExceptionResponse response = ExceptionResponse.from(ex);
        // HTTP 상태는 ErrorCode에 정의된 BAD_REQUEST를 따릅니다.
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 이미지 크기 예외 처리 -> ErrorCode로 대체
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ErrorCode errorCode = ErrorCode.FILE_SIZE_LIMIT_EXCEEDED;
        log.error("MaxUploadSizeExceededException 발생: {}", ex.getMessage(), ex);

        ExceptionResponse response = ExceptionResponse.from(errorCode);
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    //  모든 예외 처리 (최후의 방어선)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        // log.error에 Exception 객체 자체를 전달하여 Stack Trace를 출력
        log.error("예외 발생: {}", ex.getMessage(), ex);

        ExceptionResponse response = ExceptionResponse.from(errorCode);
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }
}
