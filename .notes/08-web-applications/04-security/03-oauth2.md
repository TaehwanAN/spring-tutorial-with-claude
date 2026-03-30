# Spring Security OAuth2

> 참고: https://docs.spring.io/spring-boot/reference/web/spring-security.html#web.security.oauth2

---

## 목차
1. [OAuth2 개요 및 의존성](#1-oauth2-개요-및-의존성)
2. [OAuth2 Client — 소셜 로그인](#2-oauth2-client--소셜-로그인)
3. [OAuth2 Resource Server — API 보호](#3-oauth2-resource-server--api-보호)
4. [OAuth2 Authorization Server](#4-oauth2-authorization-server)
5. [SAML 2.0](#5-saml-20)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. OAuth2 개요 및 의존성

OAuth2 3가지 역할:

| 역할 | 설명 | Spring Boot 스타터 |
|------|------|------------------|
| **Client** | 외부 인가 서버(Google, GitHub)로 로그인 | `spring-boot-starter-oauth2-client` |
| **Resource Server** | JWT/토큰으로 API 접근 제어 | `spring-boot-starter-oauth2-resource-server` |
| **Authorization Server** | 직접 인가 서버 운영 | `spring-boot-starter-oauth2-authorization-server` |

---

## 2. OAuth2 Client — 소셜 로그인

### 의존성 및 설정

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
        provider:
          # Google, GitHub, Facebook, Okta는 내장 프로바이더 → provider 섹션 불필요
          my-idp:  # 커스텀 프로바이더
            issuer-uri: https://my-idp.example.com
```

### SecurityFilterChain 설정

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2Login(Customizer.withDefaults())  // OAuth2 로그인 활성화
            .build();
}
```

### 인가된 클라이언트 서비스 (토큰 저장)

```java
// 인메모리 저장 (기본값, 개발용)
@Bean
OAuth2AuthorizedClientService authorizedClientService(
        ClientRegistrationRepository registrationRepository) {
    return new InMemoryOAuth2AuthorizedClientService(registrationRepository);
}

// DB 저장 (운영용)
@Bean
OAuth2AuthorizedClientService jdbcAuthorizedClientService(
        JdbcOperations jdbcOperations,
        ClientRegistrationRepository registrationRepository) {
    return new JdbcOAuth2AuthorizedClientService(jdbcOperations, registrationRepository);
}
```

---

## 3. OAuth2 Resource Server — API 보호

클라이언트에서 발급된 JWT를 검증하여 API 접근을 제어한다.

### JWT 방식

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com          # JWK Set URI 자동 발견
          # 또는
          jwk-set-uri: https://auth.example.com/.well-known/jwks.json
          audiences: my-api                             # aud 클레임 검증
```

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults())  // JWT 검증 활성화
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable())
            .build();
}
```

### Opaque Token 방식 (토큰 인트로스펙션)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        opaquetoken:
          introspection-uri: https://auth.example.com/introspect
          client-id: ${CLIENT_ID}
          client-secret: ${CLIENT_SECRET}
```

---

## 4. OAuth2 Authorization Server

Spring Authorization Server를 Spring Boot와 통합하여 인가 서버를 직접 운영한다.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
</dependency>
```

```yaml
spring:
  security:
    oauth2:
      authorizationserver:
        client:
          my-client:
            registration:
              client-id: my-client-id
              client-secret: "{noop}my-secret"
              authorization-grant-types:
                - authorization_code
                - client_credentials
                - refresh_token
              redirect-uris:
                - https://myapp.com/callback
              scopes:
                - openid
                - profile
                - read
                - write
```

---

## 5. SAML 2.0

기업 환경(SSO)에서 사용하는 인증 프로토콜.

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-saml2-service-provider</artifactId>
</dependency>
```

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          my-idp:
            entity-id: https://myapp.com/saml/metadata
            signing:
              credentials:
                - private-key-location: classpath:saml/private.key
                  certificate-location: classpath:saml/cert.pem
            assertingparty:
              metadata-uri: https://idp.example.com/saml/metadata
```

---

## 6. 전문가의 노트

> 💡 **내장 OAuth2 프로바이더 목록**
> Spring Security에서 자동으로 엔드포인트를 알고 있는 프로바이더:
> `google`, `github`, `facebook`, `okta`, `ping-identity`, `azure`, `keycloak`
> 이들은 `provider` 섹션 없이 `registration`만 설정하면 된다.

> 🔍 **JWT vs Opaque Token 선택 기준**
> - **JWT**: 토큰 자체에 클레임 포함 → 인가 서버 호출 없이 검증, 성능 좋음
>   단점: 토큰 즉시 무효화 불가 (만료까지 유효)
> - **Opaque Token**: 인가 서버에 인트로스펙션 요청 → 즉시 무효화 가능
>   단점: 매 요청마다 인가 서버 호출 필요

> ⚠️ **Authorization Server 프로덕션 고려사항**
> `{noop}` 인코딩은 테스트용이다.
> 운영에서는 `PasswordEncoderFactories.createDelegatingPasswordEncoder()`로 bcrypt 인코딩을 사용한다.
> 클라이언트 정보는 `InMemoryRegisteredClientRepository` 대신 `JdbcRegisteredClientRepository`를 사용한다.
