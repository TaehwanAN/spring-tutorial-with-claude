package com.demo.myapplication.shared.api;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 에러 코드.
 * 각 코드는 HTTP 상태와 메시지를 포함한다.
 */
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "error.invalid-input"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.resource-not-found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.member-not-found");

    private final HttpStatus httpStatus;
    private final String messageKey;  // messages.properties 키

    ErrorCode(HttpStatus httpStatus, String messageKey) {
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
