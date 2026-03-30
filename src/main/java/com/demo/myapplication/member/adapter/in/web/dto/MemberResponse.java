package com.demo.myapplication.member.adapter.in.web.dto;

import com.demo.myapplication.member.application.port.in.MemberInfo;

import java.time.LocalDateTime;

/**
 * 회원 응답 DTO.
 * 외부(HTTP)로 나가는 데이터를 담는다.
 */
public record MemberResponse(
        String id,
        String name,
        String email,
        LocalDateTime createdAt
) {

    public static MemberResponse from(MemberInfo info) {
        return new MemberResponse(info.id(), info.name(), info.email(), info.createdAt());
    }
}
