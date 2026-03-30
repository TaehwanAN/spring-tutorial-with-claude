package com.demo.myapplication.member.adapter.out.persistence;

import com.demo.myapplication.member.application.port.out.LoadMemberPort;
import com.demo.myapplication.member.application.port.out.SaveMemberPort;
import com.demo.myapplication.member.domain.Member;
import com.demo.myapplication.member.domain.MemberId;
import com.demo.myapplication.member.domain.MemberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemory 회원 저장소.
 *
 * <p>Clean Architecture의 adapter/out 레이어.
 * 도메인의 MemberRepository와 application의 Output Port를 모두 구현한다.
 * 추후 JPA 도입 시 JpaMemberRepository로 교체 가능 — Service 코드 변경 없음.</p>
 */
@Repository
public class InMemoryMemberRepository implements MemberRepository, SaveMemberPort, LoadMemberPort {

    private static final Logger log = LoggerFactory.getLogger(InMemoryMemberRepository.class);

    private final ConcurrentHashMap<MemberId, Member> store = new ConcurrentHashMap<>();

    @Override
    public Member save(Member member) {
        store.put(member.getId(), member);
        log.debug("회원 저장: id={}", member.getId());
        return member;
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return Optional.ofNullable(store.get(id));
    }
}
