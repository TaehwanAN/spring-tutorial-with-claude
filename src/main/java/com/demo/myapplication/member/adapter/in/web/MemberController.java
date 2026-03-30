package com.demo.myapplication.member.adapter.in.web;

import com.demo.myapplication.member.adapter.in.web.dto.MemberResponse;
import com.demo.myapplication.member.adapter.in.web.dto.RegisterMemberRequest;
import com.demo.myapplication.member.application.port.in.GetMemberUseCase;
import com.demo.myapplication.member.application.port.in.MemberInfo;
import com.demo.myapplication.member.application.port.in.RegisterMemberCommand;
import com.demo.myapplication.member.application.port.in.RegisterMemberUseCase;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 Web Adapter (Controller).
 *
 * <p>Clean Architecture 핵심:
 * Input Port 인터페이스(UseCase)에만 의존하며, Service 구현체를 직접 참조하지 않는다.</p>
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final GetMemberUseCase getMemberUseCase;

    public MemberController(RegisterMemberUseCase registerMemberUseCase,
                            GetMemberUseCase getMemberUseCase) {
        this.registerMemberUseCase = registerMemberUseCase;
        this.getMemberUseCase = getMemberUseCase;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> register(@RequestBody RegisterMemberRequest request) {
        RegisterMemberCommand command = new RegisterMemberCommand(request.name(), request.email());
        MemberInfo info = registerMemberUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(info));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(@PathVariable String id) {
        MemberInfo info = getMemberUseCase.getById(id);
        return ResponseEntity.ok(MemberResponse.from(info));
    }
}
