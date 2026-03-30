package com.demo.myapplication.member.application.port.out;

import com.demo.myapplication.member.domain.Member;
import com.demo.myapplication.member.domain.MemberId;

import java.util.Optional;

/**
 * 회원 조회 Output Port.
 * application 레이어가 정의하고, adapter/out 레이어가 구현한다.
 */
public interface LoadMemberPort {

    Optional<Member> findById(MemberId id);
}
