package com.demo.myapplication.member.application.port.out;

import com.demo.myapplication.member.domain.Member;

/**
 * 회원 저장 Output Port.
 * application 레이어가 정의하고, adapter/out 레이어가 구현한다.
 */
public interface SaveMemberPort {

    Member save(Member member);
}
