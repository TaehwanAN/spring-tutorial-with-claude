package com.demo.myapplication.member.adapter.in.web;

import com.demo.myapplication.member.adapter.in.web.dto.MemberResponse;
import com.demo.myapplication.member.adapter.in.web.dto.RegisterMemberRequest;
import com.demo.myapplication.member.application.port.in.GetMemberUseCase;
import com.demo.myapplication.member.application.port.in.MemberInfo;
import com.demo.myapplication.member.application.port.in.RegisterMemberCommand;
import com.demo.myapplication.member.application.port.in.RegisterMemberUseCase;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * 회원 Web Adapter (Controller).
 *
 * <p>Clean Architecture 핵심:
 * Input Port 인터페이스(UseCase)에만 의존하며, Service 구현체를 직접 참조하지 않는다.</p>
 *
 * <h2>Spring HATEOAS</h2>
 * <p>{@link EntityModel}로 응답을 감싸 HAL 형식의 {@code _links}를 포함한다.
 * 링크 조립은 웹 어댑터 계층(이 클래스)에서만 처리하며, {@code MemberResponse} 자체는 수정하지 않는다.
 * Clean Architecture 관점에서 HATEOAS는 HTTP 표현(표현 계층) 관심사이다.</p>
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
    public ResponseEntity<EntityModel<MemberResponse>> register(@RequestBody RegisterMemberRequest request) {
        RegisterMemberCommand command = new RegisterMemberCommand(request.name(), request.email());
        MemberInfo info = registerMemberUseCase.register(command);
        MemberResponse response = MemberResponse.from(info);

        EntityModel<MemberResponse> model = EntityModel.of(response,
                linkTo(methodOn(MemberController.class).getById(response.id())).withSelfRel(),
                linkTo(MemberController.class).withRel("members"));

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MemberResponse>> getById(@PathVariable String id) {
        MemberInfo info = getMemberUseCase.getById(id);
        MemberResponse response = MemberResponse.from(info);

        EntityModel<MemberResponse> model = EntityModel.of(response,
                linkTo(methodOn(MemberController.class).getById(id)).withSelfRel(),
                linkTo(MemberController.class).withRel("members"));

        return ResponseEntity.ok(model);
    }
}
