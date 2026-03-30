package com.demo.myapplication.member.domain;

import java.util.regex.Pattern;

/**
 * 이메일 Value Object.
 * 생성 시점에 형식을 검증하여 도메인 불변식을 보장한다.
 * 순수 Java — Spring/Jakarta 의존성 없음.
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
