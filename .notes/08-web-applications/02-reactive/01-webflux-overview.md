# Spring WebFlux 개요 (Reactive Web Applications)

> 참고: https://docs.spring.io/spring-boot/reference/web/reactive.html

---

## 목차
1. [WebFlux vs Spring MVC — 선택 기준](#1-webflux-vs-spring-mvc--선택-기준)
2. [Reactor 핵심 타입: Mono와 Flux](#2-reactor-핵심-타입-mono와-flux)
3. [WebFlux 자동 설정](#3-webflux-자동-설정)
4. [WebFluxConfigurer — 자동 설정 확장](#4-webfluxconfigurer--자동-설정-확장)
5. [함수형 라우팅 (WebFlux.fn)](#5-함수형-라우팅-webfluxfn)
6. [내장 리액티브 서버](#6-내장-리액티브-서버)
7. [이 프로젝트와 WebFlux](#7-이-프로젝트와-webflux)
8. [전문가의 노트](#8-전문가의-노트)

---

## 1. WebFlux vs Spring MVC — 선택 기준

| | Spring MVC (Servlet) | Spring WebFlux (Reactive) |
|--|---------------------|--------------------------|
| 스레드 모델 | 블로킹 I/O, 스레드 풀 | 논블로킹 I/O, 이벤트 루프 |
| 동시성 처리 | 스레드 수 = 동시 요청 수 | 적은 스레드로 많은 요청 |
| 프로그래밍 모델 | 동기 (직관적) | 리액티브 (복잡) |
| 학습 난이도 | 낮음 | 높음 |
| 외부 I/O 라이브러리 | JDBC, JPA 가능 | R2DBC, 리액티브 클라이언트 필요 |
| Spring Boot 스타터 | `spring-boot-starter-web` | `spring-boot-starter-webflux` |
| 기본 서버 | Tomcat | Reactor Netty |

### 언제 WebFlux를 선택하나?

✅ 많은 동시 연결이 필요한 스트리밍, 채팅, 실시간 API
✅ 마이크로서비스 간 비동기 호출이 많은 게이트웨이
✅ 이미 리액티브 라이브러리(R2DBC, WebClient)를 사용 중인 프로젝트

❌ 블로킹 I/O가 많은 경우 (JDBC, 파일 I/O) → 오히려 성능 저하
❌ 팀이 리액티브 프로그래밍에 익숙하지 않은 경우

> 💡 **Java 21 가상 스레드 + Spring MVC** (이 프로젝트 방식)
> 가상 스레드는 블로킹 코드를 작성하면서도 WebFlux 수준의 동시성을 얻는 대안이다.
> 학습 곡선이 낮고 기존 생태계(JDBC, JPA)와 완벽히 호환된다.

---

## 2. Reactor 핵심 타입: Mono와 Flux

```java
// Mono<T>: 0 또는 1개의 비동기 값
Mono<MemberResponse> findById(String id);

// Flux<T>: 0 ~ N개의 비동기 스트림
Flux<MemberResponse> findAll();
```

### 주요 연산자

```java
// 변환
Mono.just(member)
    .map(m -> MemberResponse.from(m))        // 동기 변환
    .flatMap(m -> saveToDB(m))               // 비동기 변환 (Mono 반환)

// 에러 처리
Mono.error(new RuntimeException())
    .onErrorReturn(defaultValue)             // 에러 시 기본값
    .onErrorResume(e -> fallback())          // 에러 시 대체 Mono

// 조합
Mono.zip(mono1, mono2, (a, b) -> combine(a, b))  // 두 Mono 병렬 실행
```

---

## 3. WebFlux 자동 설정

`WebFluxAutoConfiguration`이 제공하는 것:

| 기능 | 담당 |
|------|------|
| JSON 직렬화 | Jackson `HttpMessageReader`/`HttpMessageWriter` |
| 정적 리소스 | `ResourceWebHandler` (`spring.webflux.static-path-pattern`) |
| 에러 처리 | `DefaultErrorWebExceptionHandler` |
| 타입 변환 | `WebFluxConfigurer.addFormatters()` |
| 로케일 | `AcceptHeaderLocaleContextResolver` |

활성화 조건: `@ConditionalOnMissingBean(WebFluxConfigurationSupport.class)`
→ `@EnableWebFlux`를 사용하면 자동 설정이 OFF된다 (Spring MVC의 `@EnableWebMvc`와 동일한 패턴).

---

## 4. WebFluxConfigurer — 자동 설정 확장

Spring MVC의 `WebMvcConfigurer`에 대응하는 WebFlux 버전.

```java
@Configuration(proxyBeanMethods = false)
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000");
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // Codec 커스터마이징 (HttpMessageConverter의 WebFlux 버전)
    }
}
```

---

## 5. 함수형 라우팅 (WebFlux.fn)

WebMvc.fn과 API가 거의 동일하지만, 반환 타입이 `Mono<ServerResponse>`다.

```java
@Bean
RouterFunction<ServerResponse> route() {
    return RouterFunctions.route()
            .GET("/api/members/{id}", req -> {
                String id = req.pathVariable("id");
                return memberService.findById(id)           // Mono<MemberInfo>
                        .map(MemberResponse::from)           // Mono<MemberResponse>
                        .flatMap(resp -> ServerResponse.ok()
                                .bodyValue(resp));           // Mono<ServerResponse>
            })
            .build();
}
```

---

## 6. 내장 리액티브 서버

| 서버 | 기본값 | 특징 |
|------|--------|------|
| Reactor Netty | ✅ (`spring-boot-starter-webflux` 기본) | 논블로킹 NIO, 고성능 |
| Tomcat | (`spring-boot-starter-web`에 포함) | Servlet 호환 |
| Jetty | 수동 교체 가능 | Servlet + Reactive |

서버 교체:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <exclusions>
        <exclusion>
            <groupId>io.projectreactor.netty</groupId>
            <artifactId>reactor-netty-http</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

---

## 7. 이 프로젝트와 WebFlux

이 프로젝트는 **Servlet 스택** + **Java 21 가상 스레드** 방식을 선택했다.

```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true  # Tomcat이 가상 스레드로 요청 처리
  main:
    web-application-type: SERVLET  # MyApplication.java에서 명시
```

WebFlux를 추가하려면 별도 프로젝트로 분리하거나
`spring.main.web-application-type: reactive`로 전환해야 한다.
Servlet과 WebFlux는 같은 앱에서 동시에 사용할 수 없다.

---

## 8. 전문가의 노트

> 💡 **가상 스레드 vs WebFlux 성능**
> Java 21 가상 스레드 + Spring MVC는 I/O 바운드 워크로드에서 WebFlux와 유사한 처리량을 달성한다.
> 다만 블로킹 라이브러리(JDBC 등)는 WebFlux의 Carrier Thread를 점유하므로
> WebFlux + 블로킹 라이브러리 조합은 오히려 성능이 나빠진다.

> 🔍 **WebMvc.fn ↔ WebFlux.fn 마이그레이션**
> 두 API는 의도적으로 동일하게 설계되었다.
> `ServerRequest` / `ServerResponse` 타입 이름이 같고,
> `RouterFunctions.route()` DSL도 동일하다.
> Servlet → Reactive 전환 시 핸들러 코드의 반환 타입을 `Mono<ServerResponse>`로 바꾸고
> 블로킹 코드를 리액티브 연산자로 교체하면 된다.
