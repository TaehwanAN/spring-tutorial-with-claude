# 정적 콘텐츠 (Static Content)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.static-content

---

## 목차
1. [기본 정적 리소스 경로](#1-기본-정적-리소스-경로)
2. [spring.web.resources.* 주요 프로퍼티](#2-springwebresources-주요-프로퍼티)
3. [Cache Busting (캐시 무효화)](#3-cache-busting-캐시-무효화)
4. [WebJars 지원](#4-webjars-지원)
5. [이 프로젝트 적용](#5-이-프로젝트-적용)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. 기본 정적 리소스 경로

Spring Boot는 아래 4개 classpath 경로를 기본으로 서빙한다 (우선순위 높은 순):

```
classpath:/META-INF/resources/   ← WebJars, 라이브러리 리소스
classpath:/resources/
classpath:/static/               ← 이 프로젝트에서 사용 (static/error/*.html 등)
classpath:/public/
```

URL 매핑: `/리소스명` → 위 경로에서 파일 탐색
예: `GET /style.css` → `classpath:/static/style.css`

---

## 2. spring.web.resources.* 주요 프로퍼티

```yaml
spring:
  web:
    resources:
      static-locations:           # 정적 리소스 탐색 경로 (기본값: 위 4개)
        - classpath:/static/
        - classpath:/public/
      add-mappings: true          # false 시 정적 리소스 서빙 완전 비활성화 (REST API 전용 서버에 사용)
      cache:
        period: 0                 # Cache-Control max-age (초, 0 = 캐시 없음)
        cachecontrol:
          max-age: 7d             # 운영 환경: 7일 캐시 (period와 중복 시 이 설정 우선)
          cache-public: true      # public 캐시 허용 (CDN 등)
```

| 프로퍼티 | 기본값 | 설명 |
|---------|-------|------|
| `add-mappings` | `true` | `false` 시 `/**` 정적 리소스 핸들러 미등록 |
| `cache.period` | (없음) | 단순 Cache-Control max-age 설정 |
| `static-locations` | 위 4개 경로 | 커스텀 경로로 변경 가능 |

---

## 3. Cache Busting (캐시 무효화)

파일 내용이 변경될 때 브라우저 캐시를 자동으로 무효화하는 전략.

```yaml
spring:
  web:
    resources:
      chain:
        strategy:
          content:
            enabled: true        # 콘텐츠 해시 전략 활성화
            paths: /**           # 적용 경로
```

활성화 시 URL에 콘텐츠 해시가 자동으로 포함된다:
```
/css/app.css → /css/app-a3b4c5d6.css
```

`ResourceUrlEncodingFilter`가 자동 등록되어 Thymeleaf, FreeMarker 등
템플릿 엔진의 리소스 URL을 자동으로 해시 포함 URL로 변환한다.

### 고정 버전 전략 (Fixed Version Strategy)

```yaml
spring:
  web:
    resources:
      chain:
        strategy:
          fixed:
            enabled: true
            paths: /js/**
            version: v1.2.0     # 버전 문자열 명시
# 결과: /js/app.js → /v1.2.0/js/app.js
```

---

## 4. WebJars 지원

npm/Bower 패키지를 JAR로 패키징한 라이브러리. `/webjars/**` URL로 자동 서빙.

```xml
<!-- pom.xml 예시 -->
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>bootstrap</artifactId>
    <version>5.3.0</version>
</dependency>
```

```html
<!-- 버전 포함 URL -->
<link href="/webjars/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">

<!-- webjar-locator-lite classpath에 있으면 버전 없는 URL도 지원 -->
<link href="/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet">
```

---

## 5. 이 프로젝트 적용

### 정적 에러 페이지

```
src/main/resources/static/error/
├── 404.html   ← 404 Not Found
└── 5xx.html   ← 500, 502, 503, 504 등 모든 5xx 오류
```

`DefaultErrorViewResolver` 탐색 순서:
1. `templates/error/404.html` (템플릿 엔진 사용 시)
2. `static/error/404.html` ← 이 프로젝트
3. `static/error/4xx.html` (4xx 전체 와일드카드)

### 개발 환경 캐시 설정 (application.yml dev 프로파일)

```yaml
spring:
  web:
    resources:
      cache:
        period: 0   # 개발 중 캐시 비활성화
```

---

## 6. 전문가의 노트

> ⚠️ **spring.web.resources.add-mappings=false**
> REST API 전용 서버에서 유용하다.
> 이 설정 시 `DispatcherServlet`이 `/favicon.ico`, `/index.html` 등을
> 정적 리소스로 서빙하지 않고 404를 반환한다.
> `BasicErrorController`(`/error`)는 영향받지 않는다.

> 💡 **server.servlet.context-path와 정적 리소스**
> `server.servlet.context-path=/app`으로 설정하면
> `/static/style.css`가 아닌 `/app/style.css`로 접근해야 한다.

> 🔍 **정적 리소스와 DispatcherServlet 우선순위**
> `HandlerMapping` 순서:
> `RouterFunctionMapping`(3) → `RequestMappingHandlerMapping`(0) → `WelcomePageHandlerMapping` → `ResourceHttpRequestHandler`
> → `@Controller`에 없는 경로면 정적 리소스 핸들러가 처리한다.
