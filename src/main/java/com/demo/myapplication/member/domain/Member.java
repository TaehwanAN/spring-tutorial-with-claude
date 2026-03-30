package com.demo.myapplication.member.domain;

import java.time.LocalDateTime;

/**
 * Member Aggregate Root.
 * 순수 Java — Spring/Jakarta 의존성 없음.
 *
 * <p>Clean Architecture 핵심 원칙:
 * 도메인 엔티티는 프레임워크에 의존하지 않는다.
 * @Entity, @Id, @Component 등 어노테이션을 절대 사용하지 않는다.
 * JPA가 필요하면 adapter 레이어에서 별도 엔티티로 매핑한다.</p>
 */
public class Member {

    private final MemberId id;
    private String name;
    private Email email;
    private final LocalDateTime createdAt;

    private Member(MemberId id, String name, Email email, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    /**
     * 새 회원 생성 팩토리 메서드.
     * MemberId는 내부에서 자동 생성한다.
     */
    public static Member create(String name, Email email) {
        validateName(name);
        return new Member(MemberId.generate(), name, email, LocalDateTime.now());
    }

    /**
     * 저장소에서 복원할 때 사용하는 팩토리 메서드.
     */
    public static Member reconstitute(MemberId id, String name, Email email, LocalDateTime createdAt) {
        return new Member(id, name, email, createdAt);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("회원 이름은 비어있을 수 없습니다.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("회원 이름은 50자를 초과할 수 없습니다.");
        }
    }

    public MemberId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
