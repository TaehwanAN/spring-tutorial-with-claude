# WebFlux 웹 필터 (WebFilter)

> 참고: https://docs.spring.io/spring-boot/reference/web/reactive.html#web.reactive.webflux.web-filters

---

## 목차
1. [WebFilter 개요](#1-webfilter-개요)
2. [WebFilter 구현 패턴](#2-webfilter-구현-패턴)
3. [필터 순서 제어](#3-필터-순서-제어)
4. [Servlet Filter vs WebFilter 비교](#4-servlet-filter-vs-webfilter-비교)
5. [전문가의 노트](#5-전문가의-노트)

---

## 1. WebFilter 개요

`WebFilter`는 WebFlux의 논블로킹 필터 인터페이스다.
Servlet의 `javax.servlet.Filter`에 대응하지만, **리액티브 체인** 기반으로 동작한다.

```java
public interface WebFilter {
    Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain);
}
```

`ServerWebExchange`: 요청(`ServerHttpRequest`) + 응답(`ServerHttpResponse`) 컨텍스트.

---

## 2. WebFilter 구현 패턴

### 요청/응답 로깅 필터

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ReactiveLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();
        String method = exchange.getRequest().getMethodValue();
        String uri = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)
                .doFinally(signal -> {
                    // doFinally: 완료/에러/취소 모두에서 실행 (Servlet의 finally와 유사)
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value() : 0;
                    log.info("[{}] {} {} - {}ms",
                            status, method, uri,
                            System.currentTimeMillis() - start);
                });
    }
}
```

### 요청 헤더 검증 필터

```java
@Component
public class ApiKeyFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");

        if (apiKey == null || !isValid(apiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();  // 즉시 응답 완료
        }

        return chain.filter(exchange);
    }
}
```

---

## 3. 필터 순서 제어

### @Order 어노테이션

```java
@Component
@Order(1)   // 낮을수록 먼저 실행
public class FirstFilter implements WebFilter { ... }

@Component
@Order(2)
public class SecondFilter implements WebFilter { ... }
```

### Ordered 인터페이스 구현

```java
@Component
public class MyFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) { ... }
}
```

---

## 4. Servlet Filter vs WebFilter 비교

| | Servlet `Filter` | WebFlux `WebFilter` |
|--|-----------------|---------------------|
| 패키지 | `jakarta.servlet` | `org.springframework.web.server` |
| 체인 호출 | `chain.doFilter(req, res)` | `chain.filter(exchange)` |
| 반환 타입 | `void` (블로킹) | `Mono<Void>` (논블로킹) |
| 요청/응답 | `HttpServletRequest/Response` | `ServerWebExchange` |
| 등록 방식 | `FilterRegistrationBean` 또는 `@Component` | `@Component` 또는 `@Bean` |
| 순서 제어 | `FilterRegistrationBean.setOrder()` 또는 `@Order` | `@Order` 또는 `Ordered` 구현 |

---

## 5. 전문가의 노트

> 💡 **doFinally() vs doOnTerminate()**
> - `doFinally(SignalType)`: 완료, 에러, 취소 **모두**에서 실행 → Servlet의 `finally`와 동일
> - `doOnTerminate()`: 완료 또는 에러에서만 실행 (취소 제외)
> 로깅, 리소스 정리에는 `doFinally()`를 사용한다.

> 🔍 **Coroutine(코루틴) WebFilter**
> Spring WebFlux + Kotlin 코루틴 환경에서는 `CoWebFilter`를 상속하면
> `suspend fun filter()` 형태로 더 직관적으로 작성할 수 있다:
> ```kotlin
> class LoggingFilter : CoWebFilter() {
>     override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
>         val start = System.currentTimeMillis()
>         chain.filter(exchange)
>         log.info("Elapsed: ${System.currentTimeMillis() - start}ms")
>     }
> }
> ```

> ⚠️ **컨텍스트 전파 (Context Propagation)**
> MDC (로깅 컨텍스트)는 ThreadLocal 기반이라 WebFlux의 비동기 스레드 전환 시 사라진다.
> Reactor `Context`와 `MDC.put()`을 연동하려면 별도 설정이 필요하다.
> Spring Boot 3.x + Micrometer Tracing을 사용하면 `traceId`/`spanId`가 자동으로 전파된다.
