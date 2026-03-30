# Spring HATEOAS

> 참고: https://docs.spring.io/spring-boot/reference/web/spring-hateoas.html

---

## 목차
1. [HATEOAS란?](#1-hateoas란)
2. [의존성 추가](#2-의존성-추가)
3. [핵심 타입](#3-핵심-타입)
4. [EntityModel — 단일 리소스 래핑](#4-entitymodel--단일-리소스-래핑)
5. [CollectionModel — 컬렉션 래핑](#5-collectionmodel--컬렉션-래핑)
6. [WebMvcLinkBuilder — 링크 생성](#6-webmvclinkbuilder--링크-생성)
7. [이 프로젝트 적용: MemberController.java](#7-이-프로젝트-적용-membercontrollerjava)
8. [전문가의 노트](#8-전문가의-노트)

---

## 1. HATEOAS란?

**H**ypermedia **A**s **T**he **E**ngine **O**f **A**pplication **S**tate

REST API 응답에 **다음 가능한 행동(링크)**을 함께 포함하는 아키텍처 원칙.
클라이언트가 API 구조를 사전에 하드코딩하지 않아도 응답을 통해 탐색할 수 있다.

```json
{
  "id": "1",
  "name": "홍길동",
  "email": "hong@example.com",
  "_links": {
    "self": { "href": "http://localhost:9999/api/members/1" },
    "members": { "href": "http://localhost:9999/api/members" }
  }
}
```

---

## 2. 의존성 추가

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

자동 설정 (`HateoasAutoConfiguration`):
- `LinkDiscoverers` 빈 등록 (HAL, Collection+JSON 등 미디어 타입 파서)
- Jackson에 HATEOAS 직렬화 모듈 자동 등록 → `_links` 형식으로 직렬화

---

## 3. 핵심 타입

| 타입 | 설명 | 사용 상황 |
|------|------|---------|
| `RepresentationModel` | 링크를 포함하는 기반 클래스 | DTO 클래스에 직접 상속 |
| `EntityModel<T>` | 단일 리소스 + 링크 | `T`를 감싸는 래퍼 (권장) |
| `CollectionModel<T>` | 컬렉션 리소스 + 링크 | 목록 응답 |
| `PagedModel<T>` | 페이지 정보 + 링크 | 페이지네이션 응답 |
| `Link` | 단일 하이퍼미디어 링크 | href + rel |
| `IanaLinkRelations` | 표준 link relation 상수 | `SELF`, `NEXT`, `PREV` 등 |

---

## 4. EntityModel — 단일 리소스 래핑

```java
// 방법 1: EntityModel.of() (권장 - DTO 불변 유지)
EntityModel<MemberResponse> model = EntityModel.of(memberResponse,
        linkTo(methodOn(MemberController.class).getById(id)).withSelfRel(),
        linkTo(MemberController.class).withRel("members"));

// 방법 2: RepresentationModel 상속 (DTO 수정 필요)
public class MemberResponse extends RepresentationModel<MemberResponse> {
    // ... 필드
}
memberResponse.add(linkTo(methodOn(MemberController.class).getById(id)).withSelfRel());
```

`EntityModel.of()` 방식이 **Clean Architecture** 관점에서 더 적합하다.
도메인/응용 레이어의 DTO(`MemberResponse`)를 수정하지 않고
웹 어댑터 계층(`MemberController`)에서만 링크를 조립하기 때문이다.

---

## 5. CollectionModel — 컬렉션 래핑

```java
@GetMapping
public ResponseEntity<CollectionModel<EntityModel<MemberResponse>>> getAll() {
    List<EntityModel<MemberResponse>> members = memberService.findAll().stream()
            .map(info -> {
                MemberResponse resp = MemberResponse.from(info);
                return EntityModel.of(resp,
                        linkTo(methodOn(MemberController.class).getById(resp.id())).withSelfRel());
            })
            .toList();

    CollectionModel<EntityModel<MemberResponse>> collection = CollectionModel.of(members,
            linkTo(MemberController.class).withSelfRel());

    return ResponseEntity.ok(collection);
}
```

응답 형식:
```json
{
  "_embedded": {
    "memberResponseList": [
      { "id": "1", "name": "홍길동", "_links": { "self": { "href": "..." } } }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:9999/api/members" }
  }
}
```

---

## 6. WebMvcLinkBuilder — 링크 생성

```java
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

// methodOn(): 컨트롤러 메서드 호출을 프록시로 가로채어 URL 추출
Link selfLink = linkTo(methodOn(MemberController.class).getById("1")).withSelfRel();
// → href: http://localhost:9999/api/members/1, rel: self

// 컨트롤러 클래스 레벨 URL
Link collectionLink = linkTo(MemberController.class).withRel("members");
// → href: http://localhost:9999/api/members, rel: members

// IanaLinkRelations 표준 rel 사용
Link nextLink = linkTo(methodOn(MemberController.class).getAll(page + 1))
        .withRel(IanaLinkRelations.NEXT);
```

---

## 7. 이 프로젝트 적용: MemberController.java

```java
// member/adapter/in/web/MemberController.java

@PostMapping
public ResponseEntity<EntityModel<MemberResponse>> register(@RequestBody RegisterMemberRequest request) {
    MemberInfo info = registerMemberUseCase.register(new RegisterMemberCommand(request.name(), request.email()));
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
```

테스트:
```bash
curl -X POST http://localhost:9999/api/members \
     -H "Content-Type: application/json" \
     -d '{"name":"홍길동","email":"hong@example.com"}'
```

---

## 8. 전문가의 노트

> 💡 **HAL (Hypertext Application Language)**
> Spring HATEOAS의 기본 미디어 타입은 `application/hal+json`이다.
> `_links` 객체에 관계명(rel)과 href를 포함하는 표준 형식.
> `spring.hateoas.use-hal-as-default-json-media-type=true` (기본값)로
> `application/json` 요청에도 HAL 형식으로 응답한다.

> 🔍 **WebFlux에서의 HATEOAS**
> `spring-boot-starter-webflux`와 함께 사용 시 `WebFluxLinkBuilder`를 사용한다:
> ```java
> import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
> Mono<Link> selfLink = linkTo(methodOn(MemberController.class).getById(id)).withSelfRel().toMono();
> ```

> ⚠️ **Clean Architecture와 HATEOAS 적용 위치**
> HATEOAS는 HTTP 표현(표현 계층)의 관심사이므로,
> 도메인 레이어(`domain/`)나 응용 레이어(`application/`)는 `RepresentationModel`을 알아서는 안 된다.
> 반드시 웹 어댑터 계층(`adapter/in/web/`)에서만 `EntityModel.of()`로 조립한다.
