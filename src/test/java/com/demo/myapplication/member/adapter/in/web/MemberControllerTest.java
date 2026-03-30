package com.demo.myapplication.member.adapter.in.web;

import com.demo.myapplication.member.application.port.in.GetMemberUseCase;
import com.demo.myapplication.member.application.port.in.MemberInfo;
import com.demo.myapplication.member.application.port.in.RegisterMemberUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterMemberUseCase registerMemberUseCase;

    @MockitoBean
    private GetMemberUseCase getMemberUseCase;

    @Test
    @DisplayName("POST /api/members - 회원 등록 성공")
    void registerMember() throws Exception {
        MemberInfo info = new MemberInfo("test-id", "John", "john@test.com", LocalDateTime.now());
        when(registerMemberUseCase.register(any())).thenReturn(info);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John", "email": "john@test.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    @DisplayName("GET /api/members/{id} - 회원 조회 성공")
    void getMember() throws Exception {
        MemberInfo info = new MemberInfo("test-id", "Jane", "jane@test.com", LocalDateTime.now());
        when(getMemberUseCase.getById("test-id")).thenReturn(info);

        mockMvc.perform(get("/api/members/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    @DisplayName("GET /api/members/{id} - 존재하지 않는 회원 조회 시 400 반환")
    void getMemberNotFound() throws Exception {
        when(getMemberUseCase.getById("not-exist")).thenThrow(new IllegalArgumentException("회원을 찾을 수 없습니다"));

        mockMvc.perform(get("/api/members/not-exist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }
}
