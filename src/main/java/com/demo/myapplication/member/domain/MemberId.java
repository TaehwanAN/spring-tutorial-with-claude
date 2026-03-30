package com.demo.myapplication.member.domain;

import java.util.UUID;

/**
 * Member 식별자 Value Object.
 * UUID를 래핑하여 타입 안전성을 제공한다.
 * 순수 Java — Spring/Jakarta 의존성 없음.
 */
public record MemberId(UUID value) {

    public MemberId {
        if (value == null) {
            throw new IllegalArgumentException("MemberId는 null일 수 없습니다.");
        }
    }

    public static MemberId generate() {
        return new MemberId(UUID.randomUUID());
    }

    public static MemberId of(String uuid) {
        return new MemberId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
