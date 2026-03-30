package com.demo.myapplication.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    @DisplayName("유효한 이름과 이메일로 회원을 생성할 수 있다")
    void createMember() {
        Member member = Member.create("John", new Email("john@test.com"));

        assertNotNull(member.getId());
        assertEquals("John", member.getName());
        assertEquals("john@test.com", member.getEmail().value());
        assertNotNull(member.getCreatedAt());
    }

    @Test
    @DisplayName("이름이 null이면 예외가 발생한다")
    void createWithNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> Member.create(null, new Email("test@test.com")));
    }

    @Test
    @DisplayName("이름이 빈 문자열이면 예외가 발생한다")
    void createWithBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> Member.create("  ", new Email("test@test.com")));
    }

    @Test
    @DisplayName("이름이 50자를 초과하면 예외가 발생한다")
    void createWithLongName() {
        String longName = "a".repeat(51);
        assertThrows(IllegalArgumentException.class,
                () -> Member.create(longName, new Email("test@test.com")));
    }

    @Test
    @DisplayName("reconstitute로 기존 회원을 복원할 수 있다")
    void reconstituteMember() {
        MemberId id = MemberId.generate();
        Email email = new Email("test@test.com");
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

        Member member = Member.reconstitute(id, "Test", email, createdAt);

        assertEquals(id, member.getId());
        assertEquals("Test", member.getName());
        assertEquals(email, member.getEmail());
        assertEquals(createdAt, member.getCreatedAt());
    }
}
