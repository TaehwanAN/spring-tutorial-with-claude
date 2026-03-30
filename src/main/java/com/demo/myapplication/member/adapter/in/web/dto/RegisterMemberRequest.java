package com.demo.myapplication.member.adapter.in.web.dto;

/**
 * 회원 등록 요청 DTO.
 * 외부(HTTP)에서 들어오는 데이터를 담는다.
 */
public record RegisterMemberRequest(String name, String email) {
}
