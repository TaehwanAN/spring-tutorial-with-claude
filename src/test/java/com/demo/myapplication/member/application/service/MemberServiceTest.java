package com.demo.myapplication.member.application.service;

import com.demo.myapplication.member.application.port.in.MemberInfo;
import com.demo.myapplication.member.application.port.in.RegisterMemberCommand;
import com.demo.myapplication.member.application.port.out.LoadMemberPort;
import com.demo.myapplication.member.application.port.out.SaveMemberPort;
import com.demo.myapplication.member.domain.Email;
import com.demo.myapplication.member.domain.Member;
import com.demo.myapplication.member.domain.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    private SaveMemberPort saveMemberPort;
    private LoadMemberPort loadMemberPort;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        saveMemberPort = mock(SaveMemberPort.class);
        loadMemberPort = mock(LoadMemberPort.class);
        memberService = new MemberService(saveMemberPort, loadMemberPort);
    }

    @Test
    @DisplayName("회원을 등록하면 MemberInfo가 반환된다")
    void register() {
        when(saveMemberPort.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterMemberCommand command = new RegisterMemberCommand("John", "john@test.com");
        MemberInfo result = memberService.register(command);

        assertNotNull(result.id());
        assertEquals("John", result.name());
        assertEquals("john@test.com", result.email());
        assertNotNull(result.createdAt());
        verify(saveMemberPort, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("존재하는 회원을 ID로 조회할 수 있다")
    void getById() {
        MemberId id = MemberId.generate();
        Member member = Member.reconstitute(id, "Jane", new Email("jane@test.com"), java.time.LocalDateTime.now());
        when(loadMemberPort.findById(id)).thenReturn(Optional.of(member));

        MemberInfo result = memberService.getById(id.toString());

        assertEquals(id.toString(), result.id());
        assertEquals("Jane", result.name());
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 예외가 발생한다")
    void getByIdNotFound() {
        MemberId id = MemberId.generate();
        when(loadMemberPort.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> memberService.getById(id.toString()));
    }
}
