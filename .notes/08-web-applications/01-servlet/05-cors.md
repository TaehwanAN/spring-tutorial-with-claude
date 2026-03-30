# CORS (Cross-Origin Resource Sharing)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.cors

---

## 목차
1. [CORS 기본 개념](#1-cors-기본-개념)
2. [@CrossOrigin — 컨트롤러/메서드 레벨](#2-crossorigin--컨트롤러메서드-레벨)
3. [WebMvcConfigurer.addCorsMappings() — 전역 설정 (권장)](#3-webmvcconfigureradcorsmappings--전역-설정-권장)
4. [@CrossOrigin vs 전역 설정 우선순위](#4-crossorigin-vs-전역-설정-우선순위)
5. [이 프로젝트 적용: WebMvcConfig.java](#5-이-프로젝트-적용-webmvcconfigjava)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. CORS 기본 개념

**Same-Origin Policy**: 브라우저는 다른 출처(origin = scheme + host + port)의 응답을 차단한다.
**CORS**: 서버가 `Access-Control-*` 응답 헤더로 허용 출처를 명시하는 메커니즘.

### Preflight 요청 흐름

```
브라우저 → OPTIONS /api/members
           Origin: http://localhost:3000
           Access-Control-Request-Method: POST
           Access-Control-Request-Headers: Content-Type

서버 → 200 OK
       Access-Control-Allow-Origin: http://localhost:3000
       Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
       Access-Control-Allow-Headers: *
       Access-Control-Max-Age: 3600

브라우저 → 실제 요청 (POST /api/members) 전송
```

### 주요 CORS 응답 헤더

| 헤더 | 설명 |
|------|------|
| `Access-Control-Allow-Origin` | 허용 출처 |
| `Access-Control-Allow-Methods` | 허용 HTTP 메서드 |
| `Access-Control-Allow-Headers` | 허용 요청 헤더 |
| `Access-Control-Max-Age` | Preflight 캐시 시간 (초) |
| `Access-Control-Allow-Credentials` | 쿠키/인증 헤더 전송 허용 여부 |

---

## 2. @CrossOrigin — 컨트롤러/메서드 레벨

```java
// 클래스 레벨 — 모든 메서드에 적용
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/members")
public class MemberController { ... }

// 메서드 레벨 — 특정 엔드포인트만 허용
@CrossOrigin(origins = {"http://localhost:3000", "https://myapp.com"})
@GetMapping("/{id}")
public ResponseEntity<EntityModel<MemberResponse>> getById(@PathVariable String id) { ... }
```

---

## 3. WebMvcConfigurer.addCorsMappings() — 전역 설정 (권장)

```java
@Configuration(proxyBeanMethods = false)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")                    // CORS 적용 URL 패턴
                .allowedOrigins(
                        "http://localhost:3000",          // 프론트엔드 개발 서버
                        "http://localhost:8080"           // API Gateway
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")                      // 모든 요청 헤더 허용
                .allowCredentials(true)                   // 쿠키/인증 헤더 전송 허용
                .maxAge(3600);                            // Preflight 캐시: 1시간
    }
}
```

---

## 4. @CrossOrigin vs 전역 설정 우선순위

우선순위: **메서드 레벨 > 클래스 레벨 > 전역 설정 (addCorsMappings)**

두 설정이 겹치면 **병합(merge)**된다. 예:
- 전역: `allowedOrigins("http://localhost:3000")`
- 메서드: `@CrossOrigin(origins = "https://myapp.com")`
- 결과: 두 출처 모두 허용

---

## 5. 이 프로젝트 적용: WebMvcConfig.java

`global/config/WebMvcConfig.java` → `addCorsMappings()` 참조

**설계 결정 사항:**
- `allowCredentials(true)` → `allowedOrigins("*")` 불가 (브라우저 보안 정책)
- 개발 환경만 허용: `localhost:3000`(프론트), `localhost:8080`(게이트웨이)
- 운영 환경에서는 `@Value("${cors.allowed-origins}")` 방식으로 외부화 권장

---

## 6. 전문가의 노트

> ⚠️ **allowCredentials(true) + allowedOrigins("*") 조합 불가**
> `withCredentials: true`로 보내는 요청은 서버가 와일드카드가 아닌 구체적인 출처를 명시해야 한다.
> 이것은 브라우저 CORS spec의 보안 제약이다.

> 💡 **개발 vs 운영 CORS 분리 전략**
> ```yaml
> # application-dev.yml
> cors.allowed-origins: http://localhost:3000,http://localhost:8080
> # application-prod.yml
> cors.allowed-origins: https://myapp.com
> ```
> `@Value("${cors.allowed-origins}")` + `String[] split(",")` 패턴으로 주입.

> 🔍 **Spring Security 사용 시 주의**
> Spring Security가 있으면 `HttpSecurity.cors()`를 별도로 설정해야 한다.
> `WebMvcConfigurer.addCorsMappings()`만으로는 Security Filter 앞에서 차단될 수 있다.
> `CorsConfigurationSource` 빈을 등록하고 `HttpSecurity.cors(c -> c.configurationSource(bean))`를 사용한다.
> (이 프로젝트는 현재 Spring Security 미사용)
