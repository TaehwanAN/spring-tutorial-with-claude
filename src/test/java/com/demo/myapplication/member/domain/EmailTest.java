package com.demo.myapplication.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("유효한 이메일을 생성할 수 있다")
    void validEmail() {
        Email email = new Email("test@example.com");
        assertEquals("test@example.com", email.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "no-at-sign", "@no-local.com", "no-domain@", "spaces in@email.com"})
    @DisplayName("유효하지 않은 이메일은 예외가 발생한다")
    void invalidEmail(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    @DisplayName("null 이메일은 예외가 발생한다")
    void nullEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("빈 이메일은 예외가 발생한다")
    void blankEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Email("  "));
    }

    @Test
    @DisplayName("동일한 이메일 값은 equals가 true이다")
    void emailEquality() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");
        assertEquals(email1, email2);
    }
}
