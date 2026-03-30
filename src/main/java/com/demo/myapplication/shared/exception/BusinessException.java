package com.demo.myapplication.shared.exception;

import com.demo.myapplication.shared.api.ErrorCode;

/**
 * 비즈니스 예외 기본 클래스.
 * ErrorCode를 담아 GlobalExceptionHandler에서 일관된 응답으로 변환한다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
