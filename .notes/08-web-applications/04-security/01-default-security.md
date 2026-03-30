# Spring Security 기본 보안 설정

> 참고: https://docs.spring.io/spring-boot/reference/web/spring-security.html

---

## 목차
1. [의존성 추가 시 자동 적용되는 것](#1-의존성-추가-시-자동-적용되는-것)
2. [기본 인증 설정](#2-기본-인증-설정)
3. [SecurityFilterChain 자동 설정 구조](#3-securityfilterchain-자동-설정-구조)
4. [자동 설정 비활성화 패턴](#4-자동-설정-비활성화-패턴)
5. [전문가의 노트](#5-전문가의-노트)

---

## 1. 의존성 추가 시 자동 적용되는 것

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

`spring-boot-starter-security`를 추가하는 순간 발생하는 일:

| 자동 설정 | 결과 |
|----------|------|
| 모든 HTTP 엔드포인트에 인증 요구 | `/api/members` 등 → 401 Unauthorized |
| HTTP Basic 인증 활성화 | Authorization: Basic ... 헤더 필요 |
| Form 로그인 활성화 | `/login` 페이지 자동 생성 |
| CSRF 보호 활성화 | POST/PUT/DELETE에 CSRF 토큰 필요 |
| 보안 헤더 추가 | X-Frame-Options, X-XSS-Protection 등 |
| 인메모리 사용자 자동 생성 | 기본 사용자 `user`, 비밀번호는 시작 시 로그에 출력 |

> ⚠️ **이 프로젝트는 현재 Spring Security 미사용**
> 기존 엔드포인트가 모두 공개 상태다. Security 추가 시 별도 `SecurityFilterChain` 설정이 필요하다.

---

## 2. 기본 인증 설정

```yaml
spring:
  security:
    user:
      name: admin           # 기본 사용자 이름 (기본값: user)
      password: secret123   # 기본 비밀번호 (기본값: 랜덤 UUID, 시작 시 로그 출력)
      roles:                # 기본 역할
        - ADMIN
        - USER
```

> 💡 이 설정은 개발/테스트용이다. 운영에서는 `UserDetailsService`를 직접 구현해야 한다.

---

## 3. SecurityFilterChain 자동 설정 구조

```
HTTP 요청
    ↓
SecurityFilterChain (필터 체인)
    ├─ SecurityContextPersistenceFilter
    ├─ UsernamePasswordAuthenticationFilter  ← Form 로그인
    ├─ BasicAuthenticationFilter             ← HTTP Basic
    ├─ ExceptionTranslationFilter            ← 인증/인가 예외 처리
    └─ FilterSecurityInterceptor / AuthorizationFilter ← 접근 제어
    ↓
DispatcherServlet → Controller
```

`SecurityAutoConfiguration`이 기본 `SecurityFilterChain` 빈을 등록한다.
커스텀 `SecurityFilterChain` 빈을 등록하면 자동 설정이 백오프(backoff)된다.

---

## 4. 자동 설정 비활성화 패턴

### 패턴 1: 커스텀 SecurityFilterChain 빈 등록 (권장)

```java
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())   // REST API에서 CSRF 비활성화
                .build();
    }
}
```

### 패턴 2: @EnableWebSecurity(debug = true)

```java
@EnableWebSecurity(debug = true)  // 각 요청의 필터 체인 처리 과정을 로그에 출력 (개발용)
```

### Spring Boot Actuator와 Security

```java
// Actuator 엔드포인트는 EndpointRequest.toAnyEndpoint()로 접근 제어
http.authorizeHttpRequests(auth -> auth
    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR_ADMIN")
    ...
);
```

---

## 5. 전문가의 노트

> 💡 **AuthenticationEventPublisher**
> Spring Boot는 `DefaultAuthenticationEventPublisher`를 자동 등록한다.
> 로그인 성공/실패 이벤트를 `ApplicationEventPublisher`로 수신하여
> 감사 로그, 알림 등에 활용할 수 있다:
> ```java
> @EventListener
> void onSuccess(AuthenticationSuccessEvent event) {
>     log.info("로그인 성공: {}", event.getAuthentication().getName());
> }
> ```

> 🔍 **Spring Security + CORS**
> Spring Security가 활성화되면 Security Filter가 `WebMvcConfigurer.addCorsMappings()`보다
> 먼저 실행된다. CORS Preflight(`OPTIONS`)가 Security에서 차단될 수 있으므로
> `HttpSecurity.cors(c -> c.configurationSource(source))`를 설정해야 한다.

> ⚠️ **CSRF와 REST API**
> Stateless REST API(JWT 사용)에서는 CSRF 공격이 성립하지 않으므로 `csrf().disable()`이 적합하다.
> Cookie 기반 세션 인증을 사용하는 경우에는 CSRF 보호를 반드시 유지한다.
