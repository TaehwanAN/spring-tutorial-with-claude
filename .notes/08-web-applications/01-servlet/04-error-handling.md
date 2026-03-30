# 에러 처리 (Error Handling)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.error-handling

---

## 목차
1. [Spring Boot 기본 에러 처리 흐름](#1-spring-boot-기본-에러-처리-흐름)
2. [정적 에러 페이지](#2-정적-에러-페이지)
3. [@ExceptionHandler — @RestControllerAdvice 패턴](#3-exceptionhandler--restcontrolleradvice-패턴)
4. [RFC 9457 Problem Details (Spring Boot 3.x)](#4-rfc-9457-problem-details-spring-boot-3x)
5. [이 프로젝트 적용: 혼합 전략](#5-이-프로젝트-적용-혼합-전략)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. Spring Boot 기본 에러 처리 흐름

```
예외 발생
    ↓
DispatcherServlet → /error 경로로 FORWARD
    ↓
BasicErrorController
    ├─ HTML 요청 (Accept: text/html) → 에러 뷰 렌더링
    │   └─ DefaultErrorViewResolver → static/error/{status}.html 탐색
    └─ JSON 요청 (Accept: application/json) → DefaultErrorAttributes 응답
         └─ { timestamp, status, error, message, path }
```

`/error`는 `ErrorMvcAutoConfiguration`이 자동 등록한다.

---

## 2. 정적 에러 페이지

### 탐색 우선순위 (높은 것부터)

1. `templates/error/404.html` — 템플릿 엔진 사용 시
2. `static/error/404.html` — 정확한 상태 코드 매핑 ← **이 프로젝트**
3. `static/error/4xx.html` — 4xx 전체 와일드카드
4. Whitelabel Error Page (Spring Boot 기본 제공 HTML)

```
src/main/resources/static/error/
├── 404.html   ← 404 Not Found
└── 5xx.html   ← 모든 5xx (500/502/503/504)
```

### 에러 페이지를 보려면?

```bash
# HTML 응답 요청 (브라우저처럼)
curl http://localhost:9999/nonexistent -H "Accept: text/html"

# JSON 응답 요청 (REST 클라이언트처럼)
curl http://localhost:9999/nonexistent -H "Accept: application/json"
```

---

## 3. @ExceptionHandler — @RestControllerAdvice 패턴

`@RestControllerAdvice`는 모든 `@Controller`에 적용되는 전역 예외 처리자다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e, Locale locale) {
        // 도메인 예외 → ApiResponse<Void> + 에러 코드 + 현지화 메시지
    }

    @ExceptionHandler(Exception.class)  // 최하위 catch-all
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, Locale locale) {
        // 예상치 못한 예외 → 500 Internal Server Error
    }
}
```

💡 `Locale` 파라미터: `AcceptHeaderLocaleResolver`가 Accept-Language 헤더를 파싱해 주입.

---

## 4. RFC 9457 Problem Details (Spring Boot 3.x)

HTTP API 에러 응답 표준 형식. `application/problem+json` 미디어 타입.

### 4.1 표준 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | URI | 문제 유형 식별자 (default: `about:blank`) |
| `title` | String | 짧은 요약 |
| `status` | int | HTTP 상태 코드 |
| `detail` | String | 사람이 읽을 수 있는 상세 설명 |
| `instance` | URI | 특정 발생 위치 URI |

### 4.2 활성화

```yaml
# application.yml
spring:
  mvc:
    problemdetails:
      enabled: true
```

활성화 시 `ProblemDetailsExceptionHandler`(`ResponseEntityExceptionHandler` 상속)가 자동 등록되어
Spring MVC 내장 예외 30여 개(`MethodArgumentNotValidException`, `HttpMessageNotReadableException` 등)를
자동으로 ProblemDetail 형식으로 변환한다.

### 4.3 ProblemDetail 직접 반환

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    String fieldErrors = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));

    ProblemDetail problem = ProblemDetail.forStatus(e.getStatusCode()); // 400
    problem.setTitle("Validation Failed");
    problem.setDetail(fieldErrors);
    return problem;
}
```

응답 예시:
```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "name: 이름은 필수입니다, email: 올바른 이메일 형식이 아닙니다",
  "instance": "/api/members"
}
```

---

## 5. 이 프로젝트 적용: 혼합 전략

```
예외 종류                   응답 형식          이유
─────────────────────────────────────────────────────
BusinessException        → ApiResponse<Void>  도메인 에러 코드 포함, 현지화 지원
MethodArgumentNotValid   → ProblemDetail      HTTP 표준 준수
IllegalArgumentException → ApiResponse<Void>  기존 동작 유지
Exception                → ApiResponse<Void>  500 fallback
```

---

## 6. 전문가의 노트

> 💡 **spring.mvc.problemdetails.enabled와 @RestControllerAdvice 우선순위**
> `problemdetails.enabled=true` 시 자동 등록되는 `ProblemDetailsExceptionHandler`보다
> `@RestControllerAdvice`의 구체적인 `@ExceptionHandler`가 **먼저 평가**된다.
> `GlobalExceptionHandler`에서 `MethodArgumentNotValidException`을 명시적으로 처리하면
> `ProblemDetailsExceptionHandler`는 해당 예외에 관여하지 않는다.

> ⚠️ **Exception.class catch-all과 ProblemDetail**
> `@ExceptionHandler(Exception.class)`가 있으면 Spring MVC 내장 예외까지 잡아버린다.
> ProblemDetail 자동 처리를 원하는 예외는 catch-all보다 구체적인 핸들러로 먼저 처리해야 한다.

> 🔍 **ErrorAttributes 커스터마이징**
> `BasicErrorController`가 `/error`에서 반환하는 JSON 필드를 변경하려면
> `DefaultErrorAttributes`를 상속하고 `getErrorAttributes()`를 오버라이드한다.
