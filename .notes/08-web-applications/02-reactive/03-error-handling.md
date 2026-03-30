# WebFlux 에러 처리 (Reactive Error Handling)

> 참고: https://docs.spring.io/spring-boot/reference/web/reactive.html#web.reactive.webflux.error-handling

---

## 목차
1. [WebFlux 에러 처리 흐름](#1-webflux-에러-처리-흐름)
2. [DefaultErrorWebExceptionHandler](#2-defaulterrorwebexceptionhandler)
3. [AbstractErrorWebExceptionHandler — 커스터마이징](#3-abstracterrorwebexceptionhandler--커스터마이징)
4. [RFC 9457 Problem Details (WebFlux)](#4-rfc-9457-problem-details-webflux)
5. [Spring MVC vs WebFlux 에러 처리 비교](#5-spring-mvc-vs-webflux-에러-처리-비교)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. WebFlux 에러 처리 흐름

```
Mono/Flux에서 에러 신호 발생
        ↓
WebExceptionHandler 체인 순서대로 처리
        ↓
DefaultErrorWebExceptionHandler (가장 마지막)
        ├─ HTML 요청 → DefaultErrorAttributes → 에러 뷰
        └─ JSON 요청 → DefaultErrorAttributes → { timestamp, path, status, error, message }
```

Spring MVC의 `BasicErrorController`와 달리, WebFlux는 **`/error` 경로 포워드 없이**
`WebExceptionHandler` 체인에서 직접 에러를 처리한다.

---

## 2. DefaultErrorWebExceptionHandler

`ErrorWebFluxAutoConfiguration`이 자동 등록한다.

기본 JSON 에러 응답:
```json
{
  "timestamp": "2024-01-01T00:00:00.000+00:00",
  "path": "/api/members/999",
  "status": 500,
  "error": "Internal Server Error",
  "requestId": "abc123"
}
```

### 정적 에러 페이지 (WebFlux도 동일)

```
src/main/resources/static/error/
├── 404.html
└── 5xx.html
```

---

## 3. AbstractErrorWebExceptionHandler — 커스터마이징

```java
@Component
@Order(-2)  // DefaultErrorWebExceptionHandler(-1)보다 먼저 실행
public class CustomErrorHandler extends AbstractErrorWebExceptionHandler {

    public CustomErrorHandler(ErrorAttributes errorAttributes,
                              WebProperties webProperties,
                              ApplicationContext applicationContext,
                              ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(),
                req -> {
                    Map<String, Object> error = getErrorAttributes(req,
                            ErrorAttributeOptions.defaults());
                    int status = (int) error.getOrDefault("status", 500);
                    return ServerResponse.status(status)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(error);
                }
        );
    }
}
```

---

## 4. RFC 9457 Problem Details (WebFlux)

```yaml
# application.yml
spring:
  webflux:
    problemdetails:
      enabled: true  # Spring MVC의 spring.mvc.problemdetails.enabled와 별개
```

활성화 시 `ProblemDetailsExceptionHandler`가 자동 등록되어
WebFlux 내장 예외들을 RFC 9457 형식으로 변환한다.

---

## 5. Spring MVC vs WebFlux 에러 처리 비교

| | Spring MVC (Servlet) | Spring WebFlux (Reactive) |
|--|---------------------|--------------------------|
| 에러 처리 진입점 | `BasicErrorController` (`/error` 포워드) | `WebExceptionHandler` 체인 |
| 자동 설정 클래스 | `ErrorMvcAutoConfiguration` | `ErrorWebFluxAutoConfiguration` |
| 커스터마이징 방법 | `@RestControllerAdvice` + `@ExceptionHandler` | `AbstractErrorWebExceptionHandler` 상속 |
| ProblemDetail 설정 | `spring.mvc.problemdetails.enabled` | `spring.webflux.problemdetails.enabled` |
| 정적 에러 페이지 | `static/error/*.html` | `static/error/*.html` (동일) |

---

## 6. 전문가의 노트

> 💡 **@ExceptionHandler in WebFlux**
> WebFlux에서도 `@RestControllerAdvice` + `@ExceptionHandler`를 사용할 수 있다.
> 다만 `@Controller` 레이어에서 발생한 예외만 처리하며,
> 핸들러 외부(라우터 수준 등)에서 발생한 예외는 `WebExceptionHandler`가 처리한다.

> ⚠️ **Mono.error() 전파**
> WebFlux에서 예외는 명시적으로 `Mono.error()`로 반환해야 전파된다.
> Java의 `throw` 키워드는 Mono/Flux 파이프라인 밖에서만 동작한다:
> ```java
> // ❌ 파이프라인 안에서 throw
> .map(m -> { throw new RuntimeException(); })  // 실제로는 동작하지만 권장 안 함
>
> // ✅ 리액티브 에러 신호
> .flatMap(m -> Mono.error(new BusinessException(...)))
> ```
