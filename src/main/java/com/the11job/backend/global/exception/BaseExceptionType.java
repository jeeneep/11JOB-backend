package com.the11job.backend.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseExceptionType {
    HttpStatus getHttpStatus();
    String getErrorCode();
    String getErrorMessage();
}
