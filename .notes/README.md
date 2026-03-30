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

---

> 향후 추가 예정: `07-web-applications/`, `08-data-access/`, `09-security/` ...
