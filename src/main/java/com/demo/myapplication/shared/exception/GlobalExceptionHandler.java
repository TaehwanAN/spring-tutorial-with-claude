package com.demo.myapplication.shared.exception;

import com.demo.myapplication.shared.api.ApiResponse;
import com.demo.myapplication.shared.api.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.Objects;

/**
 * 전역 예외 처리.
 * 모든 예외를 ApiResponse 형식으로 변환하여 일관된 에러 응답을 제공한다.
 * MessageSource를 통해 Accept-Language 헤더에 따라 메시지를 현지화한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, Locale locale) {
        ErrorCode errorCode = e.getErrorCode();
        // 커스텀 메시지가 있으면 그대로, 없으면 MessageSource로 현지화
        String message = isCustomMessage(e, errorCode)
                ? e.getMessage()
                : resolve(errorCode.getMessageKey(), locale);
        log.warn("비즈니스 예외 발생: code={}, message={}", errorCode, message);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e, Locale locale) {
        log.warn("잘못된 입력: {}", e.getMessage());
        String message = resolve(ErrorCode.INVALID_INPUT.getMessageKey(), locale);
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, Locale locale) {
        log.error("예상치 못한 오류 발생", e);
        String message = resolve(ErrorCode.INTERNAL_ERROR.getMessageKey(), locale);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, message));
    }

    /**
     * BusinessException에 커스텀 메시지가 담겨 있는지 확인한다.
     * 기본 생성자(ErrorCode만)로 만들면 메시지 = messageKey이므로 현지화 대상이다.
     */
    private boolean isCustomMessage(BusinessException e, ErrorCode errorCode) {
        return e.getMessage() != null && !e.getMessage().equals(errorCode.getMessageKey());
    }

    /**
     * MessageSource로 현지화된 메시지를 조회한다.
     * defaultMessage를 key로 전달하므로 null이 반환될 수 없다.
     * (@Nullable로 선언된 Spring API 반환값에 대한 Eclipse 경고는 suppresss)
     */
    @SuppressWarnings("null")
    private String resolve(String key, Locale locale) {
        return Objects.requireNonNullElse(
                messageSource.getMessage(key, null, key, locale),
                key);
    }
}
