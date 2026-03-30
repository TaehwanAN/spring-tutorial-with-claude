package com.demo.myapplication.member.application.port.in;

/**
 * 회원 등록 Input Port.
 * adapter/in 레이어(Controller)가 이 인터페이스에 의존한다.
 */
public interface RegisterMemberUseCase {

    MemberInfo register(RegisterMemberCommand command);
}
