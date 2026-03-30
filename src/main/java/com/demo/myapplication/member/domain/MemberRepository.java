package com.demo.myapplication.member.domain;

import java.util.Optional;

/**
 * Member Repository 포트 (인터페이스).
 * 도메인이 정의하고, adapter 레이어가 구현한다.
 * 순수 Java — Spring/Jakarta 의존성 없음.
 */
public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(MemberId id);
}
