package com.the11job.backend.user.exception;


import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.BaseExceptionType;

public class UserException extends BaseException {

    private final BaseExceptionType exceptionType;

    public UserException(BaseExceptionType exceptionType) {
        super(exceptionType.getErrorMessage());
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return this.exceptionType;
    }

}
