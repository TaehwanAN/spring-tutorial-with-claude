# 015. Logging with Logback

## 1. Spring Boot 기본 로깅 스택

```
애플리케이션 코드
      │ LoggerFactory.getLogger() 호출 (org.slf4j)
      ▼
   SLF4J API          ← 추상화 레이어 (인터페이스)
      │ 런타임 바인딩
      ▼
   Logback            ← 실제 구현체 (엔진)
      │
      ├── ConsoleAppender    → 터미널 출력
      └── RollingFileAppender → 파일 출력 (롤링/압축)
```

`spring-boot-starter-web` 의존성에 SLF4J + Logback 이 기본 포함됨.
별도 의존성 추가 없이 바로 사용 가능.

---

## 2. logback.xml vs logback-spring.xml

| 구분 | logback.xml | logback-spring.xml |
|------|------------|-------------------|
| 로드 주체 | JVM (Logback 자체) | Spring Boot |
| 로드 시점 | 매우 이른 시점 (Spring 이전) | ApplicationEnvironmentPreparedEvent 이후 |
| `<springProfile>` | **사용 불가** | **사용 가능** |
| `<springProperty>` | **사용 불가** | **사용 가능** |
| 권장 여부 | Spring Boot 환경에선 비권장 | **권장** |

→ Spring Boot 프로젝트에서는 항상 `logback-spring.xml` 을 사용한다.

---

## 3. Log Level 계층

```
TRACE < DEBUG < INFO < WARN < ERROR
```

- 설정한 레벨 **이상**의 로그만 출력됨.
- 예: `level=INFO` → INFO, WARN, ERROR 출력 / TRACE, DEBUG 차단.
- Logger 계층 구조: `com.demo.myapplication.global` 은 `com.demo.myapplication` 의 자식.
  부모에서 설정한 레벨을 자식이 상속받음 (additivity 개념).

### 패키지별 레벨 설정 (application.yml)

```yaml
logging:
  level:
    root: INFO                          # 모든 logger의 기본 레벨
    "[com.demo.myapplication]": DEBUG   # 우리 패키지만 DEBUG
    "[org.springframework.web]": WARN   # Spring Web은 WARN만
```

---

## 4. logback-spring.xml 핵심 구성요소

### 4-1. springProperty — Spring 값 가져오기

```xml
<springProperty scope="context" name="APP_NAME"
                source="spring.application.name"
                defaultValue="my-app"/>
```

- `source`: `application.yml` 의 키 (kebab-case 호환)
- `scope="context"`: 전체 Logback 설정에서 `${APP_NAME}` 으로 참조 가능

### 4-2. springProfile — 프로파일 분기

```xml
<springProfile name="dev">
  <!-- dev 프로파일에서만 이 블록 적용 -->
</springProfile>

<springProfile name="prod">
  <!-- prod 프로파일에서만 이 블록 적용 -->
</springProfile>

<!-- OR 조건 -->
<springProfile name="dev | test">
  ...
</springProfile>

<!-- NOT 조건 -->
<springProfile name="!prod">
  ...
</springProfile>
```

### 4-3. ConsoleAppender

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <pattern>%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{36}) - %msg%n</pattern>
    <charset>UTF-8</charset>
  </encoder>
</appender>
```

**주요 패턴 토큰:**

| 토큰 | 의미 |
|------|------|
| `%d{HH:mm:ss.SSS}` | 시간 (포맷 지정 가능) |
| `%thread` | 스레드 이름 |
| `%-5level` | 로그 레벨 (5자리 왼쪽 정렬) |
| `%logger{36}` | Logger 이름 (최대 36자, 패키지 축약) |
| `%msg` | 로그 메시지 |
| `%n` | 줄바꿈 |
| `%highlight(...)` | ANSI 색상 (레벨별 자동 적용) |
| `%cyan(...)` | cyan 색상 |
| `%X{key:-기본값}` | MDC 값 |

### 4-4. RollingFileAppender — TimeBasedRollingPolicy

날짜(시간) 단위로 파일을 롤오버.

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>logs/app.log</file>  <!-- 현재 활성 파일 -->
  <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <!-- %d 토큰 단위로 롤오버. 매일 자정에 새 파일 생성 -->
    <fileNamePattern>logs/app.%d{yyyy-MM-dd}.log</fileNamePattern>
    <!-- 보관할 파일 최대 개수 (개수 초과 시 오래된 파일 자동 삭제) -->
    <maxHistory>7</maxHistory>
  </rollingPolicy>
  <encoder>...</encoder>
</appender>
```

### 4-5. RollingFileAppender — SizeAndTimeBasedRollingPolicy

날짜 + 파일 크기 복합 롤오버. 운영 환경에 적합.

```xml
<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
  <!-- %i : 동일 날짜 내 인덱스 (0, 1, 2 ...) -->
  <!-- .gz 확장자 → Logback이 자동으로 gzip 압축 -->
  <fileNamePattern>logs/prod/app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
  <maxFileSize>50MB</maxFileSize>    <!-- 단일 파일 크기 상한 -->
  <maxHistory>30</maxHistory>        <!-- 보관 파일 최대 개수 -->
  <totalSizeCap>5GB</totalSizeCap>   <!-- 전체 아카이브 크기 상한 -->
</rollingPolicy>
```

### 4-6. Logger와 Root

```xml
<!-- 특정 패키지의 레벨을 개별 지정 -->
<!-- additivity="false": 이 logger 의 이벤트를 root 로 전파하지 않음 -->
<!-- (true 이면 root 의 appender 에도 중복 출력됨) -->
<logger name="com.demo.myapplication" level="DEBUG" additivity="false">
  <appender-ref ref="CONSOLE"/>
</logger>

<!-- 모든 logger 의 최상위 부모 -->
<root level="INFO">
  <appender-ref ref="CONSOLE"/>
</root>
```

---

## 5. MDC (Mapped Diagnostic Context)

현재 **스레드에 컨텍스트 정보를 저장**하는 Map. 로그 패턴의 `%X{key}` 로 자동 출력됨.

```java
import org.slf4j.MDC;

MDC.put("traceId", "abc-123");  // 현재 스레드에 저장
try {
    log.info("요청 처리 시작");   // → 로그에 [traceId=abc-123] 포함
    log.debug("중간 처리");
} finally {
    MDC.clear();  // 반드시 정리! 스레드 풀 재사용 시 이전 값 오염 방지
}
```

**패턴 적용:**
```
%X{traceId:-}     → MDC traceId 값. 없으면 빈 문자열
%X{traceId:-N/A}  → MDC traceId 값. 없으면 "N/A"
```

**ECS 에서는:** MDC 값이 자동으로 `labels` 필드에 포함됨.

```json
{
  "@timestamp": "2026-03-28T12:00:00.000Z",
  "log.level": "INFO",
  "message": "요청 처리 시작",
  "labels": { "traceId": "abc-123" }
}
```

---

## 6. ECS (Elastic Common Schema) Structured Logging

### ECS란?

Elastic이 정의한 **표준 JSON 로그 스키마**. 로그를 구조화된 JSON으로 출력해
Elasticsearch → Kibana 파이프라인에서 별도 파싱 없이 바로 분석 가능.

### Spring Boot 버전별 ECS 지원

| 버전 | 방법 |
|------|------|
| 3.4+ | `logging.structured.format.file=ecs` (native 지원) |
| 3.2.x (이 프로젝트) | `co.elastic.logging:logback-ecs-encoder` 라이브러리 직접 추가 |

### pom.xml 의존성

```xml
<dependency>
    <groupId>co.elastic.logging</groupId>
    <artifactId>logback-ecs-encoder</artifactId>
    <version>1.6.0</version>
</dependency>
```

### logback-spring.xml 설정

```xml
<appender name="ECS_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>logs/prod/app-ecs.log</file>
  <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>logs/prod/app-ecs.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
    <maxFileSize>50MB</maxFileSize>
    <maxHistory>30</maxHistory>
    <totalSizeCap>5GB</totalSizeCap>
  </rollingPolicy>
  <encoder class="co.elastic.logging.logback.EcsEncoder">
    <serviceName>${APP_NAME}</serviceName>   <!-- ECS service.name 필드 -->
    <serviceVersion>0.0.9</serviceVersion>   <!-- ECS service.version 필드 -->
  </encoder>
</appender>
```

### ECS 출력 예시

```json
{
  "@timestamp": "2026-03-28T12:00:00.000Z",
  "log.level": "INFO",
  "message": "===== First Command Line Runner =====",
  "ecs.version": "1.2.0",
  "service.name": "spring-tutorial",
  "service.version": "0.0.9",
  "process.thread.name": "main",
  "log.logger": "com.demo.myapplication.global.runner.commandline.FirstCommandLineRunnerConfig",
  "labels": {
    "traceId": "runner-001"
  }
}
```

---

## 7. 로깅 초기화 타이밍

Spring Boot 애플리케이션 시작 순서와 로깅 시스템의 관계:

```
[1] ApplicationStartingEvent
    → Logback 기본 설정으로 동작 (logback-spring.xml 미적용)
    → <springProfile> 분기 비활성

[2] ApplicationEnvironmentPreparedEvent
    → 이 이벤트 처리 중에 Spring Boot 로깅 시스템 초기화
    → logback-spring.xml 로드
    → <springProperty> 바인딩 완성
    → <springProfile> 분기 활성화 시작

[3] ApplicationContextInitializedEvent
    → 로깅 시스템 완전 초기화 완료

[4] ApplicationPreparedEvent ~ 이후
    → <springProfile> 설정 완전 활성
    → 프로파일별 Appender, Level 정상 동작
```

**실무 시사점:**
- `ApplicationStartingEvent` 리스너에서 출력한 로그는 프로파일 설정과 무관하게 기본 Logback 설정으로 출력됨.
- `@PropertySource` 로 읽은 값은 Context Refresh 이후에 로드되므로 logging 설정에 사용 불가.
- logging 설정은 반드시 `application.yml` 또는 `logback-spring.xml` 에 작성해야 함.

---

## 8. 성능 팁: 파라미터화된 로깅

```java
// ❌ 나쁜 예: level 이 비활성화되어도 문자열 연결(+) 연산이 항상 실행됨
log.debug("user=" + user.toString() + ", order=" + order.toString());

// ✅ 좋은 예: {} 플레이스홀더 → level 이 활성화될 때만 toString() 호출
log.debug("user={}, order={}", user, order);

// 조건 검사 방식 (비용이 큰 연산에 사용)
if (log.isDebugEnabled()) {
    log.debug("result={}", expensiveOperation());
}
```

---

## 9. 프로젝트 로깅 구성 요약

```
dev 프로파일:
  콘솔 (색상) ──┐
  파일 (단순 롤링, logs/dev/) ──┤ com.demo.myapplication: DEBUG
                               └ root: INFO

prod 프로파일:
  파일 (SizeAndTime 롤링, .log.gz, logs/prod/app.log) ──┐
  ECS JSON 파일 (logs/prod/app-ecs.log) ────────────────┤ com.demo.myapplication: INFO
                                                         └ root: WARN
```
