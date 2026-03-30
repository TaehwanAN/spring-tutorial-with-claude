# Spring MVC 자동 설정 (Spring MVC Auto-Configuration)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc

---

## 목차
1. [WebMvcAutoConfiguration이 제공하는 것](#1-webmvcautoconfiguration이-제공하는-것)
2. [WebMvcConfigurer — 자동 설정 확장 패턴 (권장)](#2-webmvcconfigurer--자동-설정-확장-패턴-권장)
3. [@EnableWebMvc — 자동 설정 비활성화 (주의)](#3-enablewebmvc--자동-설정-비활성화-주의)
4. [이 프로젝트 적용: WebMvcConfig.java](#4-이-프로젝트-적용-webmvcconfigjava)
5. [전문가의 노트](#5-전문가의-노트)

---

## 1. WebMvcAutoConfiguration이 제공하는 것

`spring-boot-starter-web` 추가 시 `WebMvcAutoConfiguration`이 활성화되어 아래를 자동 구성한다.

| 기능 | 담당 클래스/빈 |
|------|--------------|
| JSON 직렬화/역직렬화 | `MappingJackson2HttpMessageConverter` |
| 뷰 리졸버 | `ContentNegotiatingViewResolver`, `BeanNameViewResolver` |
| 정적 리소스 서빙 | `ResourceHttpRequestHandler` (기본 4경로) |
| 로케일 처리 | `AcceptHeaderLocaleResolver` |
| 타입 변환 | `ConfigurableWebBindingInitializer` + `ConversionService` |
| 에러 메시지 코드 생성 | `MessageCodesResolver` |
| WebJars 지원 | `/webjars/**` 자동 매핑 |

활성화 조건:
```java
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
// → WebMvcConfigurationSupport 빈이 없을 때만 자동 설정 ON
// → @EnableWebMvc를 붙이면 이 빈이 등록되어 자동 설정 OFF
```

---

## 2. WebMvcConfigurer — 자동 설정 확장 패턴 (권장)

### 동작 원리

```
WebMvcAutoConfiguration
  └─ WebMvcAutoConfigurationAdapter (WebMvcConfigurer 구현)
       └─ DelegatingWebMvcConfiguration
            └─ 등록된 모든 WebMvcConfigurer 구현체에 위임 호출
                 └─ [우리의 WebMvcConfig.java] ← 이 지점에서 커스터마이징
```

`WebMvcConfigurer`를 구현하면 **자동 설정은 유지**하면서 원하는 부분만 추가/변경할 수 있다.

### 주요 확장 포인트

| 메서드 | 용도 |
|--------|------|
| `addCorsMappings(CorsRegistry)` | 전역 CORS 설정 |
| `addInterceptors(InterceptorRegistry)` | HandlerInterceptor 등록 |
| `addFormatters(FormatterRegistry)` | 커스텀 타입 변환기 등록 |
| `configureMessageConverters(List)` | HttpMessageConverter 완전 교체 |
| `extendMessageConverters(List)` | 기존 컨버터 유지하며 추가/정렬 |
| `configureViewResolvers(ViewResolverRegistry)` | 뷰 리졸버 설정 |
| `addResourceHandlers(ResourceHandlerRegistry)` | 정적 리소스 핸들러 추가 |

---

## 3. @EnableWebMvc — 자동 설정 비활성화 (주의)

```java
@Configuration
@EnableWebMvc  // ⚠️ 이것 하나로 WebMvcAutoConfiguration 전체가 꺼진다
public class MyMvcConfig implements WebMvcConfigurer { ... }
```

> ⚠️ **경고**: `@EnableWebMvc`를 붙이면 `WebMvcAutoConfiguration`이 완전히 비활성화된다.
> Jackson ObjectMapper 자동 등록, `AcceptHeaderLocaleResolver`, `ContentNegotiation` 등
> 자동 설정이 사라져 모두 수동으로 등록해야 한다.

### 비교 테이블

| | `WebMvcConfigurer` 구현 | `@EnableWebMvc` 사용 |
|--|------------------------|---------------------|
| 자동 설정 | 유지 ✅ | 비활성화 ❌ |
| Jackson 자동 구성 | 유지 ✅ | 수동 등록 필요 |
| 커스터마이징 범위 | 확장/재정의 | 완전한 제어 |
| 권장 상황 | 대부분의 Spring Boot 앱 | Spring Boot 없는 순수 MVC |

---

## 4. 이 프로젝트 적용: WebMvcConfig.java

```java
// global/config/WebMvcConfig.java
@Configuration(proxyBeanMethods = false)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",   // 프론트엔드 개발 서버
                        "http://localhost:8080"    // 동일 서버 또는 API Gateway
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Preflight 응답 캐시: 1시간
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 커스텀 Formatter 등록 포인트
        // 예: registry.addFormatter(new MemberIdFormatter());
    }
}
```

💡 `I18nConfig`에서 이미 `AcceptHeaderLocaleResolver`를 등록하므로,
`WebMvcConfig`에서는 LocaleResolver를 **중복 등록하지 않는다**.

---

## 5. 전문가의 노트

> 🔍 **ConfigurableWebBindingInitializer와 addFormatters()**
> `addFormatters()`로 등록한 Formatter/Converter는
> `ConfigurableWebBindingInitializer`를 통해 모든 요청 바인딩에 자동 반영된다.
> 예: `@PathVariable String id` → `MemberId id`로 자동 변환 가능.

> 💡 **`proxyBeanMethods = false`**
> 설정 클래스 간 빈 메서드 호출이 없을 때 사용하면,
> CGLIB 프록시 생성을 생략하여 스타트업이 약간 빨라진다.
> 이 프로젝트의 모든 `@Configuration` 클래스에 일관되게 적용한다.

> 🌳 **가상 스레드(Virtual Threads)와 MVC**
> 이 프로젝트는 `spring.threads.virtual.enabled=true`로 가상 스레드를 활성화했다.
> Tomcat이 가상 스레드로 요청을 처리하므로, `WebMvcConfigurer`로 추가되는
> Interceptor, Filter 등도 모두 가상 스레드 위에서 실행된다.
