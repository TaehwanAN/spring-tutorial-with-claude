# WebFlux HTTP Codec (HttpMessageReader / HttpMessageWriter)

> 참고: https://docs.spring.io/spring-boot/reference/web/reactive.html#web.reactive.webflux.httpcodecs

---

## 목차
1. [Codec이란?](#1-codec이란)
2. [기본 등록 Codec 목록](#2-기본-등록-codec-목록)
3. [CodecCustomizer — Codec 커스터마이징](#3-codeccustomizer--codec-커스터마이징)
4. [spring.http.codecs.* 설정](#4-springhttpcodecs-설정)
5. [Servlet HttpMessageConverter vs WebFlux Codec](#5-servlet-httpmessageconverter-vs-webflux-codec)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. Codec이란?

WebFlux에서 HTTP 요청/응답 바디를 직렬화/역직렬화하는 컴포넌트.
Spring MVC의 `HttpMessageConverter`에 대응하지만, **리액티브 스트림** 기반이다.

```
HTTP Request Body (JSON bytes 스트림)
        ↓  역직렬화 (읽기)
  HttpMessageReader<T>
        ↓
    Mono<T> / Flux<T>

    Mono<T> / Flux<T>
        ↓  직렬화 (쓰기)
  HttpMessageWriter<T>
        ↓
HTTP Response Body (JSON bytes 스트림)
```

---

## 2. 기본 등록 Codec 목록

`CodecConfigurer`가 자동 등록하는 기본 Codec:

| Codec | 처리 타입 | 미디어 타입 |
|-------|---------|-----------|
| `ByteArrayDecoder` / `ByteArrayEncoder` | `byte[]` | `application/octet-stream` |
| `StringDecoder` / `StringEncoder` | `String` | `text/plain` |
| `ResourceDecoder` / `ResourceEncoder` | `Resource` | `*/*` |
| `Jackson2JsonDecoder` / `Jackson2JsonEncoder` | `Object` | `application/json` |
| `FormHttpMessageReader` | `MultiValueMap` | `application/x-www-form-urlencoded` |
| `ServerSentEventHttpMessageWriter` | `ServerSentEvent` | `text/event-stream` |

---

## 3. CodecCustomizer — Codec 커스터마이징

```java
@Bean
CodecCustomizer myCodecCustomizer() {
    return configurer -> {
        // 기존 Codec 유지하면서 추가 설정
        configurer.defaultCodecs()
                .maxInMemorySize(1024 * 1024);  // 버퍼 크기: 1MB (기본 256KB)

        // 커스텀 Jackson ObjectMapper 적용
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        configurer.defaultCodecs().jackson2JsonEncoder(
                new Jackson2JsonEncoder(mapper));
        configurer.defaultCodecs().jackson2JsonDecoder(
                new Jackson2JsonDecoder(mapper));
    };
}
```

### WebFluxConfigurer.configureHttpMessageCodecs()

```java
@Override
public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    configurer.customCodecs()
            .register(new MyCustomEncoder());  // 커스텀 Codec 추가
}
```

---

## 4. spring.http.codecs.* 설정

```yaml
spring:
  http:
    codecs:
      max-in-memory-size: 1MB   # 메모리 내 버퍼 최대 크기 (기본: 256KB)
                                 # 큰 파일 업로드/다운로드 시 조정 필요
```

> ⚠️ `max-in-memory-size`가 너무 작으면 큰 요청 바디에서
> `DataBufferLimitException`이 발생한다. 파일 업로드가 많다면 늘려야 한다.

---

## 5. Servlet HttpMessageConverter vs WebFlux Codec

| | Spring MVC | Spring WebFlux |
|--|-----------|---------------|
| 타입 | `HttpMessageConverter<T>` | `HttpMessageReader<T>` / `HttpMessageWriter<T>` |
| 반환 타입 | `T` (블로킹) | `Mono<T>` / `Flux<T>` (논블로킹) |
| 등록 방식 | `WebMvcConfigurer.extendMessageConverters()` | `CodecCustomizer` / `WebFluxConfigurer.configureHttpMessageCodecs()` |
| 버퍼 전략 | 전체 바디를 메모리에 로드 | 스트리밍 (일부씩 처리 가능) |

---

## 6. 전문가의 노트

> 💡 **SSE (Server-Sent Events)와 Codec**
> WebFlux에서 `Flux<T>`를 반환하면 자동으로 스트리밍 응답이 된다.
> `text/event-stream` 미디어 타입과 `ServerSentEventHttpMessageWriter`가
> 이를 SSE 형식으로 직렬화한다.
> ```java
> @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
> Flux<String> stream() {
>     return Flux.interval(Duration.ofSeconds(1)).map(i -> "event-" + i);
> }
> ```

> 🔍 **ClientHttpMessageConvertersCustomizer (Spring Boot 3.4+)**
> `RestClient`/`RestTemplate`의 클라이언트측 컨버터는
> `ClientHttpMessageConvertersCustomizer` 빈으로 독립적으로 커스터마이징한다.
> 서버측 `CodecCustomizer`와 분리되어 있으므로 서버/클라이언트 설정을 다르게 가져갈 수 있다.
