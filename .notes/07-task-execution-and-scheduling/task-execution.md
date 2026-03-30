안녕하세요! Spring Boot 전문가로서 요청하신 내용을 정확하게 번역해 드리고, 실무에서 도움이 될 상세 설명을 덧붙여 드리겠습니다.

---

## 1. 본문 번역 (Translation)

### 태스크 실행 (Task Execution)

Spring Boot는 `Executor` 빈이 없는 경우 `AsyncTaskExecutor`를 자동 설정합니다.

- **가상 스레드 활성화 시** (`spring.threads.virtual.enabled=true`, Java 21+):
  가상 스레드를 사용하는 `SimpleAsyncTaskExecutor`가 자동 설정됩니다.
- **가상 스레드 비활성화 시**:
  적절한 기본값을 가진 `ThreadPoolTaskExecutor`가 자동 설정됩니다.

자동 설정된 `AsyncTaskExecutor`는 다음 통합 기능에서 사용됩니다:

1. `@EnableAsync`를 사용한 비동기 태스크 실행
2. Spring for GraphQL에서 `Callable` 반환값의 비동기 처리
3. Spring MVC에서의 비동기 요청 처리
4. Spring WebFlux에서의 블로킹 실행 지원
5. Spring WebSocket 인바운드/아웃바운드 메시지 채널
6. JPA 부트스트랩 실행기
7. `ApplicationContext`에서의 백그라운드 빈 초기화

여러 통합 기능에서 사용할 커스텀 `AsyncTaskExecutor`를 등록하려면, `applicationTaskExecutor`라는 이름으로 빈을 등록하세요.

```java
@Configuration(proxyBeanMethods = false)
public class MyTaskExecutorConfiguration {

    @Bean("applicationTaskExecutor")
    SimpleAsyncTaskExecutor applicationTaskExecutor() {
        return new SimpleAsyncTaskExecutor("app-");
    }
}
```

빌더 클래스를 사용하면 더 쉽게 기본 동작을 갖춘 인스턴스를 만들 수 있습니다.

```java
@Configuration(proxyBeanMethods = false)
public class MyTaskExecutorConfiguration {

    @Bean
    SimpleAsyncTaskExecutor taskExecutor(SimpleAsyncTaskExecutorBuilder builder) {
        return builder.build();
    }
}
```

`@EnableAsync`에 사용할 executor를 지정하려면 `AsyncConfigurer` 빈을 정의하세요.

```java
@Configuration(proxyBeanMethods = false)
public class MyTaskExecutorConfiguration {

    @Bean
    AsyncConfigurer asyncConfigurer(ExecutorService executorService) {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                return executorService;
            }
        };
    }

    @Bean
    ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }
}
```

자동 설정된 `AsyncTaskExecutor`를 유지하면서 별도 executor도 등록하려면, `defaultCandidate = false`로 설정하세요.

```java
@Bean(defaultCandidate = false)
@Qualifier("scheduledExecutorService")
ScheduledExecutorService scheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
}
```

Spring Boot가 항상 `AsyncTaskExecutor`를 자동 설정하도록 강제하려면 force 모드를 사용하세요.

```yaml
spring:
  task:
    execution:
      mode: force
```

기본 `ThreadPoolTaskExecutor`는 8개의 코어 스레드로 부하에 따라 늘고 줄어듭니다.
`spring.task.execution` 네임스페이스로 세부 설정이 가능합니다.

```yaml
spring:
  task:
    execution:
      pool:
        max-size: 16
        queue-capacity: 100
        keep-alive: "10s"
```

---

## 2. 전문가의 상세 설명 (Deep Dive)

### 2.1 ThreadPoolTaskExecutor vs SimpleAsyncTaskExecutor

| 구분 | ThreadPoolTaskExecutor | SimpleAsyncTaskExecutor |
| :--- | :--- | :--- |
| **스레드 관리** | 스레드 풀을 생성해 재사용 | 매 요청마다 새 스레드 생성 |
| **설정** | `core-size`, `max-size`, `queue-capacity` | 가상 스레드 활성화 여부 |
| **적합 상황** | 플랫폼 스레드, 스레드 수 제한 필요 | 가상 스레드 환경 (Java 21+) |
| **가상 스레드** | 자동 설정 시 비활성화 상태에서 사용 | 가상 스레드 활성화 시 자동 선택됨 |

> **Tip:** 가상 스레드 활성화 시 `spring.task.execution.pool.*` 설정은 무시됩니다.
> 가상 스레드는 풀링 없이 JVM이 직접 스케줄링하기 때문입니다.

### 2.2 커스텀 Executor 등록 패턴 4가지

#### 패턴 1: `applicationTaskExecutor` 이름으로 등록 (권장)
MVC async, JPA bootstrap, WebSocket 등 Spring 인프라 전반에서 공유됩니다.

```java
@Bean("applicationTaskExecutor")
ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
    return builder.build(); // spring.task.execution.* 프로퍼티 자동 반영
}
```

#### 패턴 2: `AsyncConfigurer`로 `@Async` 전용 executor 지정

```java
@Bean
AsyncConfigurer asyncConfigurer() {
    return new AsyncConfigurer() {
        @Override
        public Executor getAsyncExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setThreadNamePrefix("my-async-");
            executor.initialize();
            return executor;
        }
    };
}
```

#### 패턴 3: `defaultCandidate = false`로 자동 설정 유지
자동 설정된 executor는 그대로 두고, 특정 용도의 executor를 별도로 등록할 때 사용합니다.

```java
@Bean(defaultCandidate = false)
@Qualifier("heavyTaskExecutor")
ThreadPoolTaskExecutor heavyTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.initialize();
    return executor;
}
```

#### 패턴 4: force 모드로 자동 설정 강제

```yaml
spring:
  task:
    execution:
      mode: force
```

`Executor` 빈이 등록되어 있어도 Spring Boot가 `applicationTaskExecutor`를 강제로 자동 설정합니다.
`AsyncConfigurer`가 있으면 `@Async`에는 `AsyncConfigurer`가 우선합니다.

### 2.3 빈 해석 우선순위 (Bean Resolution Priority)

Spring이 각 통합 기능에서 executor를 선택하는 우선순위입니다.

| 통합 기능 | 우선순위 |
| :--- | :--- |
| **`@EnableAsync`** | `AsyncConfigurer` → `@Primary` 빈 또는 `taskExecutor` 이름 → `applicationTaskExecutor` → 자동 설정 |
| **Spring MVC / WebFlux / GraphQL** | `applicationTaskExecutor` 빈 → 자동 설정 |
| **WebSocket / JPA** | 단일 `AsyncTaskExecutor` 빈 → `applicationTaskExecutor` → 자동 설정 |
| **Bootstrap (백그라운드 초기화)** | `bootstrapExecutor` 이름 → `applicationTaskExecutor` → 자동 설정 |

### 2.4 `ThreadPoolTaskExecutorBuilder` 사용의 장점

빌더를 주입받아 사용하면 `application.yml`의 `spring.task.execution.*` 값이 자동으로 적용됩니다.
환경별 프로파일(dev/prod)에 따라 설정을 달리할 수 있어 코드 변경 없이 스레드 풀을 튜닝할 수 있습니다.

```java
// 빌더가 spring.task.execution.* 프로퍼티를 자동으로 읽어서 설정해줍니다.
@Bean("applicationTaskExecutor")
ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
    return builder.build();
}
```

### 2.5 `spring.task.execution.pool.*` 동작 원리

```
요청 증가 →
  ① core-size 이하: core 스레드로 즉시 처리
  ② core-size 초과: queue-capacity 크기 대기열에 쌓임
  ③ queue-capacity 초과: max-size까지 스레드 추가 생성
  ④ max-size 초과: RejectedExecutionException 발생
```

| 프로퍼티 | 기본값 | 설명 |
| :--- | :--- | :--- |
| `pool.core-size` | 8 | 기본 유지 스레드 수 |
| `pool.max-size` | `Integer.MAX_VALUE` | 최대 스레드 수 |
| `pool.queue-capacity` | `Integer.MAX_VALUE` | 대기열 크기 |
| `pool.keep-alive` | `60s` | 유휴 스레드 유지 시간 |
| `thread-name-prefix` | `"task-"` | 스레드 이름 접두사 |

> **실무 팁:** `queue-capacity`를 무한대로 두면 `max-size`까지 스레드가 늘어나지 않습니다.
> 큐가 먼저 채워져야 max 스레드가 생성되므로, 빠른 확장이 필요하면 `queue-capacity`를 작게 설정하세요.
