package com.demo.myapplication.member.application.port.in;

import java.time.LocalDateTime;

/**
 * 회원 정보 읽기 모델.
 * Service → Controller 로 반환되는 출력 데이터.
 */
public record MemberInfo(
        String id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}
