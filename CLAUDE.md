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

**Spring Boot 3.2.4 / Java 21 / Maven**

The project uses a **domain-driven package structure** (not the traditional layered approach):

```
com.demo.myapplication/
├── MyApplication.java          # Entry point; registers lifecycle listeners, startup tracking
├── common/                     # Shared utilities
├── domain/
│   └── example/                # Business capability modules go here
└── global/                     # App-wide infrastructure
    ├── listener/application/   # Spring Boot lifecycle event listeners (very early startup hooks)
    ├── monitoring/health/       # Custom health indicators and Kubernetes probes
    ├── runner/commandline/      # CommandLineRunner / ApplicationRunner beans
    ├── ApplicationArgumentsLoader.java
    ├── ApplicationShutdownDisposableBean.java
    ├── ExternalConfigurationPropertiesConfig.java  # @ConfigurationProperties binding
    └── ExternalValueConfig.java                    # @Value binding
```

Key architectural choices in `MyApplication.java`:
- `BufferingApplicationStartup(2048)` — startup metrics available at `/actuator/startup`
- Exit code bean returns `42` for CI/CD pipeline status checks
- Web type explicitly set to `SERVLET` (not reactive)
- Lifecycle listeners registered programmatically (not via `@Component`) so they fire before context refresh

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

`.notes/` contains ~19 markdown files documenting architectural patterns and Spring Boot concepts covered by this tutorial (lifecycle, health monitoring, runners, config properties, virtual threads, etc.). These are learning materials, not operational docs.
