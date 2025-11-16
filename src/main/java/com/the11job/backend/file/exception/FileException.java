// src/main/java/com/the11job/backend/file/exception/FileException.java

package com.the11job.backend.file.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;

public class FileException extends BaseException {

    // ErrorCode와 메시지만 받는 생성자
    public FileException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ErrorCode와 Exception (Cause)를 받는 생성자
    public FileException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}