package com.the11job.backend.company.exception;

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;

/**
 * Company 도메인 예외 클래스
 */
public class CompanyException extends BaseException {

    public CompanyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CompanyException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}