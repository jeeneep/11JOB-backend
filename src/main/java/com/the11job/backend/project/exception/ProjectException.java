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

    // 오류 해결을 위해 추가된 생성자
    // ProjectService에서 IOException을 감싸서 던질 때 사용됩니다.
    public ProjectException(ErrorCode errorCode, String customMessage, Throwable cause) {
        // BaseException의 적절한 생성자를 호출해야 합니다.
        // BaseException에 이 시그니처가 없으면 BaseException을 먼저 수정해야 합니다.
        super(errorCode, customMessage, cause);
    }

}