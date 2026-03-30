# 필터, 서블릿, 리스너 (Filters, Servlets, Listeners)

> 참고: https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.embedded-container.servlets-filters-listeners

---

## 목차
1. [Servlet 컴포넌트 등록 방법 비교](#1-servlet-컴포넌트-등록-방법-비교)
2. [FilterRegistrationBean — 권장 등록 방식](#2-filterregistrationbean--권장-등록-방식)
3. [DispatcherType — 필터 적용 시점](#3-dispatchertype--필터-적용-시점)
4. [OncePerRequestFilter — 요청당 1회 보장](#4-onceperequestrequestfilter--요청당-1회-보장)
5. [@WebFilter 방식 (대안)](#5-webfilter-방식-대안)
6. [이 프로젝트 적용: RequestLoggingFilter](#6-이-프로젝트-적용-requestloggingfilter)
7. [전문가의 노트](#7-전문가의-노트)

---

## 1. Servlet 컴포넌트 등록 방법 비교

| 방법 | 순서 제어 | URL 패턴 제한 | Spring 통합 | 권장 여부 |
|------|---------|------------|-----------|---------|
| `FilterRegistrationBean` | ✅ `setOrder()` | ✅ `addUrlPatterns()` | ✅ 최적 | ✅ **권장** |
| `@WebFilter + @ServletComponentScan` | ❌ `Ordered` 미지원 | ✅ `urlPatterns` | ⚠️ 부분적 | 레거시 |
| `@Component` | ⚠️ `@Order`로만 | ❌ 모든 경로 | ✅ | 간단한 경우 |

---

## 2. FilterRegistrationBean — 권장 등록 방식

```java
// global/config/FilterConfig.java
@Configuration(proxyBeanMethods = false)
public class FilterConfig {

    @Bean
    FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new RequestLoggingFilter()); // new: @Component 미사용으로 이중 등록 방지
        registration.addUrlPatterns("/api/*");              // /api/ 하위 경로만 적용
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // 필터 체인 최우선
        registration.setName("requestLoggingFilter");

        return registration;
    }
}
```

### FilterRegistrationBean 주요 설정

| 메서드 | 설명 |
|--------|------|
| `setFilter()` | 등록할 Filter 객체 |
| `addUrlPatterns()` | 적용 URL 패턴 (Servlet 방식: `/api/*`) |
| `setOrder()` | 실행 순서 (낮을수록 먼저) |
| `setDispatcherTypes()` | 적용할 DispatcherType 지정 |
| `setEnabled()` | 필터 활성화/비활성화 |
| `setAsyncSupported()` | 비동기 지원 여부 |

---

## 3. DispatcherType — 필터 적용 시점

| DispatcherType | 설명 | 예시 |
|---------------|------|------|
| `REQUEST` | 일반 요청 (기본값) | 브라우저 → 서버 |
| `FORWARD` | RequestDispatcher.forward() | Spring MVC `/error` 처리 |
| `INCLUDE` | RequestDispatcher.include() | JSP include |
| `ERROR` | 에러 페이지 처리 | 오류 → `/error` 포워드 |
| `ASYNC` | 비동기 처리 | `DeferredResult`, `@Async` |

```java
// 특정 DispatcherType에만 필터 적용
registration.setDispatcherTypes(
    EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC)
);
```

---

## 4. OncePerRequestFilter — 요청당 1회 보장

일반 `javax.servlet.Filter`는 `RequestDispatcher.forward()`가 호출될 때마다 다시 실행될 수 있다.
`OncePerRequestFilter`는 **같은 요청에서 한 번만 실행**됨을 보장한다.

```
일반 Filter 위험:
클라이언트 요청 → Filter → Controller → 예외 → /error FORWARD → Filter (다시 실행!) ← 문제

OncePerRequestFilter:
클라이언트 요청 → Filter → Controller → 예외 → /error FORWARD → Filter (건너뜀) ← 안전
```

```java
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            // finally: 체인에서 예외 발생해도 반드시 로깅
            log.info("[{}] {} {} - {}ms",
                    response.getStatus(),
                    request.getMethod(),
                    request.getRequestURI(),
                    System.currentTimeMillis() - start);
        }
    }
}
```

---

## 5. @WebFilter 방식 (대안)

```java
@WebFilter(urlPatterns = "/api/*")
public class MyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(req, res);
    }
}
```

`@ServletComponentScan`을 메인 클래스 또는 `@Configuration`에 추가해야 한다:

```java
@SpringBootApplication
@ServletComponentScan(basePackages = "com.demo.myapplication")
public class MyApplication { ... }
```

> ⚠️ `@WebFilter`는 `Ordered`를 구현해도 필터 순서가 보장되지 않는다.
> 순서 제어가 필요하면 반드시 `FilterRegistrationBean`을 사용한다.

---

## 6. 이 프로젝트 적용: RequestLoggingFilter

```
global/
├── filter/
│   └── RequestLoggingFilter.java   ← OncePerRequestFilter 상속
└── config/
    └── FilterConfig.java           ← FilterRegistrationBean 등록
```

**필터 실행 순서 (이 프로젝트):**
```
ORDER = HIGHEST_PRECEDENCE + 1: RequestLoggingFilter
    ↓ 모든 /api/* 요청
    Spring Security Filters (추후 추가 시)
    ↓
DispatcherServlet → HandlerMapping → Controller
```

---

## 7. 전문가의 노트

> 💡 **필터 순서 설계 전략**
> - `HIGHEST_PRECEDENCE` (= `Integer.MIN_VALUE`): 스프링 시큐리티 등 인프라 필터용
> - `HIGHEST_PRECEDENCE + 1`: 로깅/추적 필터 (가장 먼저 → 전체 시간 측정)
> - `0`: 일반 커스텀 필터
> - `LOWEST_PRECEDENCE`: 마지막에 실행하고 싶은 필터

> ⚠️ **OncePerRequestFilter와 비동기 디스패치**
> `DeferredResult`, `Callable` 등 비동기 처리 시, 비동기 디스패치에서도 필터가 실행된다.
> 비동기 디스패치를 제외하려면 `shouldNotFilterAsyncDispatch()`를 오버라이드해 `true`를 반환한다.

> 🔍 **ServletRegistrationBean / ServletListenerRegistrationBean**
> 커스텀 `HttpServlet`이나 `ServletContextListener`를 등록할 때도 같은 패턴을 사용한다:
> ```java
> @Bean
> ServletRegistrationBean<MyServlet> myServlet() {
>     return new ServletRegistrationBean<>(new MyServlet(), "/custom/*");
> }
> ```
