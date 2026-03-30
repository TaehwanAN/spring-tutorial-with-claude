package com.demo.myapplication.member.application.port.in;

/**
 * 회원 등록 커맨드.
 * Controller → Service 로 전달되는 입력 데이터.
 */
public record RegisterMemberCommand(String name, String email) {

    public RegisterMemberCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
    }
}
