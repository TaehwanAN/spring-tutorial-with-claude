package com.demo.myapplication.member.application.service;

import com.demo.myapplication.member.application.port.in.GetMemberUseCase;
import com.demo.myapplication.member.application.port.in.MemberInfo;
import com.demo.myapplication.member.application.port.in.RegisterMemberCommand;
import com.demo.myapplication.member.application.port.in.RegisterMemberUseCase;
import com.demo.myapplication.member.application.port.out.LoadMemberPort;
import com.demo.myapplication.member.application.port.out.SaveMemberPort;
import com.demo.myapplication.member.domain.Email;
import com.demo.myapplication.member.domain.Member;
import com.demo.myapplication.member.domain.MemberId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 회원 서비스 — UseCase 구현체.
 *
 * <p>Clean Architecture 핵심:
 * Output Port 인터페이스에만 의존하며, 구체적인 저장소 구현을 모른다.
 * 현재는 InMemory, 추후 JPA로 교체해도 이 클래스는 변경 없음.</p>
 */
@Service
public class MemberService implements RegisterMemberUseCase, GetMemberUseCase {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final SaveMemberPort saveMemberPort;
    private final LoadMemberPort loadMemberPort;

    public MemberService(SaveMemberPort saveMemberPort, LoadMemberPort loadMemberPort) {
        this.saveMemberPort = saveMemberPort;
        this.loadMemberPort = loadMemberPort;
    }

    @Override
    public MemberInfo register(RegisterMemberCommand command) {
        Email email = new Email(command.email());
        Member member = Member.create(command.name(), email);
        Member saved = saveMemberPort.save(member);

        log.info("회원 등록 완료: id={}, name={}", saved.getId(), saved.getName());
        return toMemberInfo(saved);
    }

    @Override
    public MemberInfo getById(String id) {
        MemberId memberId = MemberId.of(id);
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + id));

        return toMemberInfo(member);
    }

    private MemberInfo toMemberInfo(Member member) {
        return new MemberInfo(
                member.getId().toString(),
                member.getName(),
                member.getEmail().value(),
                member.getCreatedAt()
        );
    }
}
