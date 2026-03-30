# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Run the application (dev profile active by default)
mvn spring-boot:run

# Run with a specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Package as executable JAR
mvn package

# Run tests
mvn test

# Clean build
mvn clean package
```

The packaged JAR is output to `target/my-spring-tutorial-{version}.jar`.

## Architecture

**Spring Boot 3.5.13 / Java 21 / Maven / Spring Modulith 1.4.10**

### Design Principles
- **DDD Package Structure**: 최상위는 바운디드 컨텍스트(도메인 모듈) 단위로 구성
- **Clean Architecture per Domain**: 각 도메인 내부는 `domain` → `application` → `adapter` 레이어
- **Spring Modulith**: 모듈 경계 강제, 테스트 시점에 의존성 규칙 검증
- **MSA Ready**: Modulith-first, 필요 시 각 모듈을 마이크로서비스로 추출 가능

### Package Structure

```
com.demo.myapplication/
├── MyApplication.java              # Entry point; lifecycle listeners, startup tracking
├── global/                          # [OPEN module] App-wide infrastructure
│   ├── config/                      # @ConfigurationProperties, @Value bindings
│   ├── lifecycle/                   # Application event listeners, shutdown handler, args loader
│   ├── monitoring/health/           # Custom health indicators and Kubernetes probes
│   └── runner/                      # CommandLineRunner beans
├── shared/                          # [OPEN module] Cross-cutting: API response, error handling
│   ├── api/                         # ApiResponse, ErrorCode
│   └── exception/                   # BusinessException, GlobalExceptionHandler
└── {domain}/                        # [CLOSED module] Bounded context (e.g., member)
    ├── domain/                      # Entities, Value Objects, Repository ports (순수 Java, Spring 의존성 0)
    ├── application/
    │   ├── port/in/                 # Input ports (UseCase interfaces, Commands)
    │   ├── port/out/                # Output ports (persistence/external interfaces)
    │   └── service/                 # @Service — UseCase 구현체
    ├── adapter/
    │   ├── in/web/                  # @RestController, Request/Response DTOs
    │   └── out/persistence/         # Repository 구현체
    └── internal/                    # Module-private classes (hidden from other modules)
```

### Clean Architecture Rules
1. `domain/` 패키지는 Spring/Jakarta 의존성 금지 (순수 Java)
2. `application/service/`는 port 인터페이스에만 의존 (구현체 모름)
3. `adapter/in/`과 `adapter/out/`은 서로 의존 금지
4. 모듈 간 통신은 최상위 패키지의 public API만 사용 (Modulith 강제)

### Key Choices in `MyApplication.java`
- `BufferingApplicationStartup(2048)` — startup metrics at `/actuator/startup`
- Exit code bean returns `42` for CI/CD
- `WebApplicationType.SERVLET` (not reactive)
- Lifecycle listeners registered programmatically (fires before context refresh)

## Configuration

Default profile is `dev` (port **9999**). Production profile uses port **9090** with WARN-level logging and 200 Tomcat threads.

Spring Boot's externalized config priority (highest → lowest):
1. `./config/application-{profile}.yml`
2. `./config/application.yml`
3. `src/main/resources/application-{profile}.yml`
4. `src/main/resources/application.yml`

The main `application.yml` dynamically imports three modular configs:
- `config/db-config.yml` — MySQL + HikariCP (credentials via `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` env vars)
- `config/api-config.yml`
- `config/batch-config.yml`

Actuator exposes `health`, `info`, and `startup` endpoints. Kubernetes liveness/readiness probes are enabled.

## Notes

`.notes/` contains learning materials organized by Spring Boot Reference doc chapters:
- `architecture/` — DDD, Clean Architecture, Modulith, MSA 의사결정
- `02-using-spring-boot/` — Auto-config, lazy init, runtime handling
- `03-spring-application/` — Lifecycle events, runners, availability, virtual threads
- `04-externalized-configuration/` — Properties, YAML, imports, random values
- `05-logging/` — Logback config, structured logging, OpenTelemetry

See `.notes/README.md` for full index.
