# HTTP 메시지 컨버터 (HttpMessageConverters)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.message-converters

---

## 목차
1. [HttpMessageConverter 역할](#1-httpmessageconverter-역할)
2. [기본 등록 컨버터 목록](#2-기본-등록-컨버터-목록)
3. [Jackson 자동 설정](#3-jackson-자동-설정)
4. [컨버터 커스터마이징 패턴](#4-컨버터-커스터마이징-패턴)
5. [전문가의 노트](#5-전문가의-노트)

---

## 1. HttpMessageConverter 역할

HTTP 요청/응답 바디와 Java 객체 사이의 **직렬화/역직렬화**를 담당한다.

```
HTTP Request Body (JSON/XML/text)
        ↓  역직렬화 (읽기)
  HttpMessageConverter
        ↓
    Java Object (@RequestBody)

    Java Object (@ResponseBody / ResponseEntity)
        ↓  직렬화 (쓰기)
  HttpMessageConverter
        ↓
HTTP Response Body
```

컨버터 선택 기준: **Content-Type** (요청 읽기) / **Accept 헤더** (응답 쓰기)

---

## 2. 기본 등록 컨버터 목록

`HttpMessageConvertersAutoConfiguration`이 자동 등록하는 컨버터들:

| 컨버터 | 처리 타입 | 기본 미디어 타입 |
|--------|---------|----------------|
| `ByteArrayHttpMessageConverter` | `byte[]` | `application/octet-stream`, `*/*` |
| `StringHttpMessageConverter` | `String` | `text/plain`, `*/*` |
| `ResourceHttpMessageConverter` | `Resource` | `*/*` |
| `FormHttpMessageConverter` | `MultiValueMap` | `application/x-www-form-urlencoded` |
| `MappingJackson2HttpMessageConverter` | `Object` | `application/json` |
| `MappingJackson2XmlHttpMessageConverter` | `Object` | `application/xml` (jackson-dataformat-xml 있을 때) |

---

## 3. Jackson 자동 설정

`spring-boot-starter-web`에는 `spring-boot-starter-json`이 포함되어 있어
`ObjectMapper` 빈이 자동으로 구성된다.

### spring.jackson.* 프로퍼티로 전역 조정

```yaml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false   # LocalDateTime → ISO-8601 문자열
      indent-output: true                # 개발 환경 가독성 향상
    deserialization:
      fail-on-unknown-properties: false  # 알 수 없는 필드 무시
    default-property-inclusion: non_null # null 필드 직렬화 생략
    date-format: "yyyy-MM-dd HH:mm:ss"  # 날짜 포맷 전역 설정
```

### Jackson2ObjectMapperBuilderCustomizer — 세밀한 커스터마이징

자동 설정을 유지하면서 `ObjectMapper`를 추가 설정할 때 사용한다.

```java
@Bean
Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
    return builder -> builder
        .modules(new JavaTimeModule())              // Java 8 시간 타입 지원 모듈
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
}
```

---

## 4. 컨버터 커스터마이징 패턴

### 패턴 1: extendMessageConverters (기존 유지 + 추가/정렬) ✅ 권장

```java
@Override
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // 기존 컨버터 목록을 유지하면서 앞에 커스텀 컨버터를 삽입
    converters.add(0, new MyCustomConverter());
}
```

### 패턴 2: configureMessageConverters (완전 교체) ⚠️ 주의

```java
@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // 이 메서드를 구현하면 기본 컨버터가 모두 사라지고 여기서 지정한 것만 남는다
    converters.add(new MappingJackson2HttpMessageConverter());
}
```

### 패턴 3: HttpMessageConverters 빈 교체 (Spring Boot 방식)

```java
@Bean
HttpMessageConverters customConverters() {
    return new HttpMessageConverters(new MyCustomConverter());
    // 기본 컨버터 목록에 추가됨 (완전 교체 아님)
}
```

### 컨버터 선택 흐름 (응답 시)

```
클라이언트 Accept: application/json
        ↓
ContentNegotiationManager
        ↓
등록된 컨버터 순서대로 canWrite() 확인
        ↓
MappingJackson2HttpMessageConverter.write() 호출
```

---

## 5. 전문가의 노트

> 💡 **@JsonComponent vs HttpMessageConverter**
> - `@JsonComponent`: Jackson ObjectMapper 수준 — 특정 타입의 직렬화 방식만 변경
> - `HttpMessageConverter`: Spring MVC 수준 — 미디어 타입 협상 + 변환 전체 레이어
> 단순히 JSON 형식을 바꾸고 싶으면 `@JsonComponent`, 새로운 미디어 타입을 지원하려면 `HttpMessageConverter`를 커스터마이징한다.
> (`@JsonComponent`는 `.notes/06-json/json.md` 참조)

> ⚠️ **ClientHttpMessageConvertersCustomizer (Spring Boot 3.4+)**
> 서버측 컨버터(`MappingJackson2HttpMessageConverter`)와 별개로,
> `RestClient` / `RestTemplate`의 **클라이언트측 컨버터**를 독립적으로 커스터마이징할 수 있다.
> 서버/클라이언트 직렬화 규칙을 다르게 가져가야 할 때 유용하다.

> 🔍 **컨버터 등록 순서와 성능**
> 컨버터 목록은 순서대로 `canWrite()`를 확인하므로,
> 가장 많이 사용하는 컨버터(`MappingJackson2HttpMessageConverter`)를 앞에 두는 것이 유리하다.
