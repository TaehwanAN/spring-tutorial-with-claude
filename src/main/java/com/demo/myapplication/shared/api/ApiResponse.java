package com.demo.myapplication.shared.api;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 envelope.
 * 모든 API 응답은 이 형식으로 래핑된다.
 *
 * <pre>
 * 성공: { "success": true, "data": {...}, "error": null, "timestamp": "..." }
 * 실패: { "success": false, "data": null, "error": { "code": "...", "message": "..." }, "timestamp": "..." }
 * </pre>
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null,
                new ErrorDetail(errorCode.name(), errorCode.getMessageKey()),
                LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null,
                new ErrorDetail(errorCode.name(), message),
                LocalDateTime.now());
    }

    public record ErrorDetail(String code, String message) {
    }
}
