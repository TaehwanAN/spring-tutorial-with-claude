# 함수형 라우팅 (Functional Routing with WebMvc.fn)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.functional

---

## 목차
1. [WebMvc.fn 개요](#1-webmvcfn-개요)
2. [핵심 타입](#2-핵심-타입)
3. [route() DSL — 기본 패턴](#3-route-dsl--기본-패턴)
4. [nest() — 경로 중첩 패턴](#4-nest--경로-중첩-패턴)
5. [@Controller와의 공존 및 우선순위](#5-controller와의-공존-및-우선순위)
6. [이 프로젝트 적용: RouterFunctionConfig.java](#6-이-프로젝트-적용-routerfunctionconfigjava)
7. [전문가의 노트](#7-전문가의-노트)

---

## 1. WebMvc.fn 개요

어노테이션(`@Controller`, `@RequestMapping`) 없이 **함수형 DSL**로 HTTP 엔드포인트를 등록하는 방식.

### @Controller vs RouterFunction 비교

| | `@Controller` / `@RequestMapping` | `RouterFunction` |
|--|----------------------------------|-----------------|
| 선언 방식 | 어노테이션 | @Bean 메서드 |
| 라우팅 규칙 | 어노테이션 메타데이터 | 함수 합성 |
| 테스트 | MockMvc | 순수 단위 테스트 가능 |
| 적합 상황 | 대부분의 REST API | 경량 엔드포인트, 함수형 스타일 |
| Spring Boot 지원 | 자동 감지 | RouterFunction 빈 자동 감지 |

---

## 2. 핵심 타입

```java
// 요청 → 핸들러 함수 라우팅 규칙
RouterFunction<ServerResponse>

// 실제 요청 처리: ServerRequest → ServerResponse
HandlerFunction<ServerResponse>

// 요청 정보 읽기 (HttpServletRequest 래퍼)
ServerRequest

// 응답 빌더 (상태 코드, 헤더, 바디)
ServerResponse
```

---

## 3. route() DSL — 기본 패턴

```java
@Bean
RouterFunction<ServerResponse> myRouter() {
    return RouterFunctions.route()
            .GET("/api/hello", req ->
                    ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("message", "Hello, World!")))
            .POST("/api/echo", req -> {
                String body = req.body(String.class);
                return ServerResponse.ok().body(body);
            })
            .build();
}
```

### RequestPredicates — 조건 조합

```java
import static org.springframework.web.servlet.function.RequestPredicates.*;

RouterFunctions.route()
    .route(GET("/api/users").and(accept(MediaType.APPLICATION_JSON)), handler::list)
    .route(path("/api/admin/**").and(headers(h -> h.firstHeader("X-Admin") != null)), handler::admin)
    .build();
```

---

## 4. nest() — 경로 중첩 패턴

공통 경로 prefix를 묶어 중복 제거.

```java
@Bean
RouterFunction<ServerResponse> apiInfoRouter() {
    return RouterFunctions.route()
            .nest(RequestPredicates.path("/api/info"), builder -> builder
                    .GET("", req -> ServerResponse.ok()
                            .body(Map.of("service", "spring-tutorial", "status", "UP")))
                    .GET("/time", req -> ServerResponse.ok()
                            .body(Map.of("serverTime", LocalDateTime.now().toString())))
            )
            .build();
}
// 등록 엔드포인트: GET /api/info, GET /api/info/time
```

---

## 5. @Controller와의 공존 및 우선순위

`RouterFunction`과 `@RequestMapping` 기반 컨트롤러는 **공존 가능**하다.

### HandlerMapping 실행 순서

| HandlerMapping | Order | 설명 |
|----------------|-------|------|
| `RequestMappingHandlerMapping` | `0` | `@Controller` 매핑 (먼저 평가) |
| `RouterFunctionMapping` | `3` | `RouterFunction` 매핑 (나중에 평가) |
| `ResourceHttpRequestHandler` | — | 정적 리소스 (마지막) |

> Order 숫자가 **낮을수록** 우선순위 높음.
> `@Controller`가 먼저 평가되고, 일치하지 않으면 `RouterFunction`이 처리한다.

---

## 6. 이 프로젝트 적용: RouterFunctionConfig.java

```java
// global/web/RouterFunctionConfig.java
@Configuration(proxyBeanMethods = false)
public class RouterFunctionConfig {

    @Bean
    RouterFunction<ServerResponse> apiInfoRouter() {
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/info"), builder -> builder
                        .GET("", req -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                        "service", "spring-tutorial",
                                        "version", "0.1.0",
                                        "status", "UP",
                                        "timestamp", LocalDateTime.now().toString()
                                )))
                        .GET("/time", req -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                        "serverTime", LocalDateTime.now().toString()
                                )))
                )
                .build();
    }
}
```

**테스트:**
```bash
curl http://localhost:9999/api/info
curl http://localhost:9999/api/info/time
```

---

## 7. 전문가의 노트

> 💡 **WebMvc.fn vs WebFlux.fn**
> API 형태가 거의 동일하다:
> - `WebMvc.fn`: 동기 Servlet 스택, `ServerResponse`는 블로킹
> - `WebFlux.fn`: 비동기 Reactive 스택, `ServerResponse`는 `Mono<ServerResponse>`
> 나중에 WebFlux로 전환해도 라우팅 코드를 거의 그대로 재사용할 수 있다.

> 🔍 **핸들러 클래스 분리 패턴 (실무 권장)**
> 요청 처리 로직이 복잡해지면 별도 Handler 클래스로 분리한다:
> ```java
> @Component
> class InfoHandler {
>     ServerResponse getInfo(ServerRequest req) { ... }
>     ServerResponse getTime(ServerRequest req) { ... }
> }
>
> @Bean
> RouterFunction<ServerResponse> route(InfoHandler handler) {
>     return RouterFunctions.route()
>             .GET("/api/info", handler::getInfo)
>             .GET("/api/info/time", handler::getTime)
>             .build();
> }
> ```
> `@Controller`의 메서드 레벨과 유사하지만, Spring 없이 순수 Java로 단위 테스트 가능하다.

> ⚠️ **Map.of() 응답의 키 순서**
> `Map.of()`는 순서를 보장하지 않는다. JSON 응답 필드 순서가 중요하다면
> `LinkedHashMap` 또는 별도 DTO record를 사용한다.
