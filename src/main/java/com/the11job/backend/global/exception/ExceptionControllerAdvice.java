package com.the11job.backend.global.exception;

import com.the11job.backend.user.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.UnsupportedEncodingException;

@Slf4j // @SLf4j는 로깅 기능을 제공한다. log.error(..)등의 로그 출력을 사용할 수 있게 한다.
@RestControllerAdvice // @ControllerAdvice + @ResponseBody. 예외 발생 시 JSON 형태로 응답을 반환한다.
public class ExceptionControllerAdvice {

    // MethodArgumentNotValidException 처리 (검증 예외 처리)
    @ExceptionHandler(MethodArgumentNotValidException.class) // -> 클라이언트가 잘못된 형식의 데이터 전송?
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.from(ex);  // 검증 오류 메시지를 처리
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);  // 400 Bad Request
    }

    // 인코딩 관련 예외 처리
    @ExceptionHandler(UnsupportedEncodingException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedEncodingException(UnsupportedEncodingException ex) {
        log.error("UnsupportedEncodingException 발생: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.from("이메일 인증 발송에 실패했습니다. 다시 시도해주세요.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);  // 500 Internal Server Error
    }

    // 모든 예외 처리 (Generic)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("예외 발생: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.from("처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);  // 500 Internal Server Error
    }

    // UserException
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleCategoryException(UserException ex) {
        log.error("UserException 발생: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.from(ex.getExceptionType());
        return new ResponseEntity<>(response, ex.getExceptionType().getHttpStatus());
    }

}