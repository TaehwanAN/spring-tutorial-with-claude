# Spring MVC / WebFlux 보안 설정

> 참고: https://docs.spring.io/spring-boot/reference/web/spring-security.html#web.security.spring-mvc

---

## 목차
1. [MVC Security — HttpSecurity 커스터마이징](#1-mvc-security--httpsecurity-커스터마이징)
2. [접근 규칙 설정 패턴](#2-접근-규칙-설정-패턴)
3. [@EnableMethodSecurity — 메서드 레벨 보안](#3-enablemethodsecurity--메서드-레벨-보안)
4. [WebFlux Security — ServerHttpSecurity](#4-webflux-security--serverhttpsecurity)
5. [MVC vs WebFlux Security 비교](#5-mvc-vs-webflux-security-비교)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. MVC Security — HttpSecurity 커스터마이징

```java
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // URL 기반 접근 제어
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/members/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            // 인증 방식
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
            )
            .httpBasic(Customizer.withDefaults())  // HTTP Basic 인증
            // JWT 사용 시 세션 비활성화
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // REST API: CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
```

---

## 2. 접근 규칙 설정 패턴

### PathRequest — Spring Boot 내장 경로

```java
.authorizeHttpRequests(auth -> auth
    // Spring Boot 정적 리소스 경로 자동 허용
    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
    // Actuator 헬스 체크 허용
    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
    .anyRequest().authenticated()
)
```

### SpEL 표현식 기반 접근 제어

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").access(
        new WebExpressionAuthorizationManager("hasRole('ADMIN') and hasIpAddress('10.0.0.0/8')")
    )
)
```

---

## 3. @EnableMethodSecurity — 메서드 레벨 보안

```java
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity  // prePostEnabled=true 기본값
public class MethodSecurityConfig { }
```

```java
@Service
public class MemberService {

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public MemberInfo getById(String userId) { ... }

    @PostAuthorize("returnObject.email == authentication.principal.username")
    public MemberInfo getProfile() { ... }

    @Secured("ROLE_ADMIN")  // 단순 역할 체크
    public void deleteAll() { ... }
}
```

---

## 4. WebFlux Security — ServerHttpSecurity

WebFlux에서는 `HttpSecurity` 대신 `ServerHttpSecurity`를 사용한다.

```java
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
public class ReactiveSecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    ReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user").password("password").roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
}
```

---

## 5. MVC vs WebFlux Security 비교

| | Spring MVC Security | WebFlux Security |
|--|--------------------|--------------------|
| 어노테이션 | `@EnableWebSecurity` | `@EnableWebFluxSecurity` |
| 설정 클래스 | `HttpSecurity` | `ServerHttpSecurity` |
| 필터 체인 | `SecurityFilterChain` | `SecurityWebFilterChain` |
| 사용자 서비스 | `UserDetailsService` | `ReactiveUserDetailsService` |
| 인증 관리자 | `AuthenticationManager` | `ReactiveAuthenticationManager` |
| 메서드 보안 | `@EnableMethodSecurity` | `@EnableReactiveMethodSecurity` |

---

## 6. 전문가의 노트

> 💡 **SecurityFilterChain vs WebSecurityConfigurerAdapter**
> Spring Security 5.7+ / Spring Boot 3.x에서 `WebSecurityConfigurerAdapter`는 deprecated됐다.
> `@Configuration` + `SecurityFilterChain @Bean` 패턴을 사용한다.

> 🔍 **자동 설정 백오프 조건**
> Spring Boot는 커스텀 `SecurityFilterChain` 빈이 등록되면 자동 설정을 백오프한다.
> `SpringBootWebSecurityConfiguration`의 조건:
> `@ConditionalOnDefaultWebSecurity` + `@ConditionalOnMissingBean(SecurityFilterChain.class)`

> ⚠️ **Actuator + Security**
> `management.endpoints.web.exposure.include=*` 설정 시 Security가 없으면
> 모든 Actuator 엔드포인트가 공개된다.
> Security 추가 시 Actuator 엔드포인트를 명시적으로 허용해야 한다.
