package com.the11job.backend.project.exception; // project 도메인 내부 패키지

import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ProjectException extends BaseException {

    public ProjectException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProjectException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

}