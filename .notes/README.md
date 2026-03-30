# 학습 노트 인덱스

Spring Boot Reference 공식 문서 구조에 맞춰 정리한 학습 노트입니다.
> 참고: https://docs.spring.io/spring-boot/reference/

## architecture/ — 아키텍처 의사결정
- [001_domain-driven-vs-layered.md](architecture/001_domain-driven-vs-layered.md) — Layered vs Domain-Driven 구조 비교
- [002_clean-architecture.md](architecture/002_clean-architecture.md) — Clean Architecture + DDD 구조
- [003_spring-modulith.md](architecture/003_spring-modulith.md) — Spring Modulith로 모듈 경계 강제
- [004_microservice-multi-module.md](architecture/004_microservice-multi-module.md) — Modulith vs Multi-Module vs MSA

## 02-using-spring-boot/ — Spring Boot 사용하기
- [dynamic-runtime-handling.md](02-using-spring-boot/dynamic-runtime-handling.md) — 런타임 동적 핸들러 매핑
- [auto-configuration-evaluation.md](02-using-spring-boot/auto-configuration-evaluation.md) — Auto Configuration 조건 평가 리포트
- [lazy-initialization.md](02-using-spring-boot/lazy-initialization.md) — Lazy Initialization

## 03-spring-application/ — Spring Application
- [application-availability.md](03-spring-application/application-availability.md) — Liveness & Readiness 상태
- [monitoring-availability.md](03-spring-application/monitoring-availability.md) — 가용성 모니터링 방식 비교
- [application-events-and-listeners.md](03-spring-application/application-events-and-listeners.md) — 8개 라이프사이클 이벤트
- [runner.md](03-spring-application/runner.md) — CommandLineRunner & ApplicationRunner
- [application-exit.md](03-spring-application/application-exit.md) — Graceful Shutdown & Exit Code
- [virtual-threads.md](03-spring-application/virtual-threads.md) — Java 21 Virtual Threads

## 04-externalized-configuration/ — 외부 설정
- [overview.md](04-externalized-configuration/overview.md) — 설정 우선순위 개요
- [property-sources.md](04-externalized-configuration/property-sources.md) — @PropertySource 사용법과 주의점
- [application-properties-yml.md](04-externalized-configuration/application-properties-yml.md) — application.properties/yml 우선순위
- [random-value-property.md](04-externalized-configuration/random-value-property.md) — RandomValuePropertySource
- [external-application-properties.md](04-externalized-configuration/external-application-properties.md) — 외부 설정 파일 로딩
- [importing-other-properties.md](04-externalized-configuration/importing-other-properties.md) — spring.config.import 패턴

## 05-logging/ — 로깅
- [logging.md](05-logging/logging.md) — Logback 설정 가이드
- [modern-logging-system.md](05-logging/modern-logging-system.md) — 분산 로깅 + OpenTelemetry

## 06-json/ — JSON
- [json.md](06-json/json.md) — Jackson / Gson / JSON-B 자동 설정 및 커스터마이징

## 07-task-execution-and-scheduling/ — 태스크 실행 및 스케줄링
- [task-execution.md](07-task-execution-and-scheduling/task-execution.md) — AsyncTaskExecutor 자동 설정, 커스텀 executor 패턴 4종, 빈 해석 우선순위
- [task-scheduling.md](07-task-execution-and-scheduling/task-scheduling.md) — @Scheduled 트리거 3종, ThreadPoolTaskScheduler 설정, 가상 스레드 주의사항

## 08-web-applications/ — 웹 애플리케이션

### 01-servlet/ — Servlet Web Applications
- [01-mvc-auto-configuration.md](08-web-applications/01-servlet/01-mvc-auto-configuration.md) — WebMvcAutoConfiguration, WebMvcConfigurer vs @EnableWebMvc
- [02-http-message-converters.md](08-web-applications/01-servlet/02-http-message-converters.md) — HttpMessageConverter 체인, Jackson 통합 및 커스터마이징
- [03-static-content.md](08-web-applications/01-servlet/03-static-content.md) — 정적 리소스 서빙, Cache Busting, WebJars
- [04-error-handling.md](08-web-applications/01-servlet/04-error-handling.md) — BasicErrorController 흐름, @ExceptionHandler, RFC 9457 ProblemDetail
- [05-cors.md](08-web-applications/01-servlet/05-cors.md) — Preflight 흐름, @CrossOrigin vs addCorsMappings() 전역 설정
- [06-filters-servlets-listeners.md](08-web-applications/01-servlet/06-filters-servlets-listeners.md) — FilterRegistrationBean, OncePerRequestFilter, DispatcherType
- [07-path-matching.md](08-web-applications/01-servlet/07-path-matching.md) — PathPatternParser vs AntPathMatcher, ContentNegotiationManager
- [08-functional-routing.md](08-web-applications/01-servlet/08-functional-routing.md) — RouterFunction DSL, WebMvc.fn, @Controller와 공존

### 02-reactive/ — Reactive Web Applications
- [01-webflux-overview.md](08-web-applications/02-reactive/01-webflux-overview.md) — Mono/Flux, WebFluxConfigurer, 가상 스레드 vs WebFlux 비교
- [02-codecs.md](08-web-applications/02-reactive/02-codecs.md) — HttpMessageReader/Writer, CodecCustomizer, 버퍼 크기 설정
- [03-error-handling.md](08-web-applications/02-reactive/03-error-handling.md) — ErrorWebExceptionHandler, AbstractErrorWebExceptionHandler
- [04-web-filters.md](08-web-applications/02-reactive/04-web-filters.md) — WebFilter 구현, @Order 순서 제어, doFinally() 패턴

### 기타
- [03-graceful-shutdown.md](08-web-applications/03-graceful-shutdown.md) — server.shutdown=graceful, timeout-per-shutdown-phase, K8s 연동
- [04-security/01-default-security.md](08-web-applications/04-security/01-default-security.md) — 자동 설정, SecurityFilterChain, UserDetailsService
- [04-security/02-mvc-webflux-security.md](08-web-applications/04-security/02-mvc-webflux-security.md) — HttpSecurity, @EnableMethodSecurity, 접근 규칙
- [04-security/03-oauth2.md](08-web-applications/04-security/03-oauth2.md) — OAuth2 Client/Resource Server/Authorization Server, SAML 2.0
- [05-graphql.md](08-web-applications/05-graphql.md) — 스키마 설정, @QueryMapping, DataFetcherExceptionResolver, GraphiQL
- [06-hateoas.md](08-web-applications/06-hateoas.md) — EntityModel, CollectionModel, WebMvcLinkBuilder, HAL 형식

## features/ — 개별 기능 (미적용, 추후 참고용)
- [kotlin.md](features/kotlin.md) — Kotlin 지원: runApplication, null-safety, data class 바인딩, MockK 테스트
- [ssl.md](features/ssl.md) — SSL 번들: JKS/PEM 방식, Hot Reload (Tomcat/Netty), SslBundles 빈

---

> 향후 추가 예정: `09-data-access/`, `10-security/` ...
