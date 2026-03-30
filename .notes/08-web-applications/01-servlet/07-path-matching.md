# 경로 매칭 & 콘텐츠 협상 (Path Matching & Content Negotiation)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.content-negotiation

---

## 목차
1. [PathPatternParser — Spring Boot 기본값](#1-pathpatternparser--spring-boot-기본값)
2. [경로 패턴 문법](#2-경로-패턴-문법)
3. [콘텐츠 협상 (Content Negotiation)](#3-콘텐츠-협상-content-negotiation)
4. [spring.mvc.pathmatch.* 설정](#4-springmvcpathmatch-설정)
5. [전문가의 노트](#5-전문가의-노트)

---

## 1. PathPatternParser — Spring Boot 기본값

Spring Boot 2.6+ / Spring Framework 5.3+부터 `PathPatternParser`가 기본 경로 매칭 전략이다.

### PathPatternParser vs AntPathMatcher 비교

| | `PathPatternParser` | `AntPathMatcher` |
|--|--------------------|--------------------|
| 패턴 파싱 시점 | 앱 시작 시 1회 | 매 요청마다 파싱 |
| 성능 | 높음 (파싱 결과 캐시) | 낮음 |
| `**` 위치 제약 | 경로 끝에만 허용 | 중간도 허용 |
| Spring Boot 기본 | ✅ (2.6+) | (레거시 호환) |

```yaml
# 전략 변경 (레거시 호환 필요 시)
spring:
  mvc:
    pathmatch:
      matching-strategy: ant-path-matcher  # 기본값: path-pattern-parser
```

---

## 2. 경로 패턴 문법

| 패턴 | 의미 | 예시 매핑 URL |
|------|------|-------------|
| `/api/members` | 정확히 일치 | `/api/members` |
| `/api/*` | 한 세그먼트 와일드카드 | `/api/members`, `/api/orders` |
| `/api/**` | 다중 세그먼트 (끝에만) | `/api/members/123/orders` |
| `/api/{id}` | 경로 변수 | `/api/123` → `id=123` |
| `/api/{*path}` | 나머지 전체 캡처 | `/api/a/b/c` → `path=/a/b/c` |

```java
@GetMapping("/api/{*path}")  // 나머지 경로 전체를 path 변수로
public String catchAll(@PathVariable String path) { ... }
```

---

## 3. 콘텐츠 협상 (Content Negotiation)

`ContentNegotiationManager`가 클라이언트의 요청을 분석하여 적합한 미디어 타입을 결정한다.

### 전략 1: Accept 헤더 기반 (기본값, 권장)

```http
GET /api/members/1
Accept: application/json    → JSON 응답
Accept: application/xml     → XML 응답 (jackson-dataformat-xml 있을 때)
```

### 전략 2: URL 파라미터 기반 (선택적 활성화)

```yaml
spring:
  mvc:
    contentnegotiation:
      favor-parameter: true       # ?format=json 파라미터 활성화
      parameter-name: format      # 파라미터 이름 (기본값: format)
      media-types:
        json: application/json    # 파라미터값 → 미디어 타입 매핑
        xml: application/xml
```

```http
GET /api/members/1?format=json
```

### 전략 3: 경로 확장자 기반 (비활성화됨)

`/api/members/1.json` 같은 확장자 매핑은 Spring Boot 2.6+부터 **기본 비활성화**되었다.
보안 취약점(Reflected File Download 등) 방지 목적.

```yaml
# 활성화하려면 (권장하지 않음)
spring:
  mvc:
    contentnegotiation:
      favor-path-extension: true
```

### 커스텀 미디어 타입 추가

```yaml
spring:
  mvc:
    contentnegotiation:
      media-types:
        v1: application/vnd.myapp.v1+json
        v2: application/vnd.myapp.v2+json
```

---

## 4. spring.mvc.pathmatch.* 설정

```yaml
spring:
  mvc:
    pathmatch:
      matching-strategy: path-pattern-parser  # 기본값
      use-suffix-pattern: false               # .json 확장자 매핑 비활성화 (기본값)
      use-registered-suffix-pattern: false    # 등록된 확장자만 허용 (use-suffix-pattern=true일 때)
```

---

## 5. 전문가의 노트

> 💡 **PathPatternParser의 `**` 제약**
> `PathPatternParser`에서 `**`는 경로의 **끝 세그먼트**에만 올 수 있다.
> `/a/**/b` — 허용 (`**`가 중간)
> `/a/**b` — 불허 (세그먼트 중간에 `**`)
> AntPathMatcher에서는 `/a/**b`도 허용됐기 때문에 마이그레이션 시 주의한다.

> ⚠️ **Actuator와 경로 매칭 전략**
> Spring Actuator는 내부적으로 `PathPatternParser`를 사용한다.
> `ant-path-matcher`로 변경하면 일부 Actuator 매핑(`/actuator/**`)이 예상과 다르게 동작할 수 있다.

> 🔍 **API 버전 관리 (Spring Boot 3.5+)**
> Spring Boot 3.5에서 `spring.mvc.apiversion.*` 프로퍼티가 추가되었다.
> 헤더 기반 버전 관리(`Accept: application/vnd.myapp+json;version=2`)를 설정으로 지원한다.
> `ApiVersionResolver`, `ApiVersionParser` 빈으로 커스터마이징 가능.
