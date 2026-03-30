안녕하세요! Spring Boot 전문가로서 요청하신 내용을 정확하게 번역해 드리고, 실무에서 도움이 될 상세 설명을 덧붙여 드리겠습니다.

---

## 1. 본문 번역 (Translation)

### 태스크 스케줄링 (Task Scheduling)

Spring Boot는 스케줄된 태스크 실행을 위한 `TaskScheduler`도 자동 설정합니다.

- **가상 스레드 활성화 시** (`spring.threads.virtual.enabled=true`, Java 21+):
  가상 스레드를 사용하는 `SimpleAsyncTaskScheduler`가 자동 설정되며, 풀링 관련 프로퍼티는 무시됩니다.
- **가상 스레드 비활성화 시**:
  적절한 기본값을 가진 `ThreadPoolTaskScheduler`가 자동 설정됩니다.

`ThreadPoolTaskScheduler`는 `spring.task.scheduling` 네임스페이스로 설정할 수 있습니다.

```yaml
spring:
  task:
    scheduling:
      thread-name-prefix: "scheduling-"
      pool:
        size: 2
```

빌더 클래스(`ThreadPoolTaskSchedulerBuilder`, `SimpleAsyncTaskSchedulerBuilder`)도 자동 설정되며,
커스텀 `TaskScheduler`를 쉽게 만들 수 있습니다.

---

## 2. 전문가의 상세 설명 (Deep Dive)

### 2.1 `@Scheduled` 트리거 3가지

Spring Boot에서 스케줄링을 사용하려면 `@EnableScheduling`을 활성화하고,
실행할 메서드에 `@Scheduled`를 붙입니다.

#### fixedRate — 시작 시점 기준 반복

이전 실행의 **시작** 시점으로부터 일정 간격 후 다음 실행을 시작합니다.

```java
@Scheduled(fixedRate = 30_000, initialDelay = 10_000)
public void fixedRateTask() {
    // 애플리케이션 기동 10초 후 첫 실행, 이후 30초마다
}
```

> **주의:** 작업이 30초보다 오래 걸리면 이전 작업이 끝나기 전에 다음 실행이 예약됩니다.
> 기본적으로 단일 스레드에서는 직전 완료를 기다리지만, 멀티 스레드 스케줄러 환경에서는 겹칠 수 있습니다.

#### fixedDelay — 완료 시점 기준 대기

이전 실행의 **완료** 시점으로부터 일정 시간 대기 후 다음 실행을 시작합니다.

```java
@Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
public void fixedDelayTask() {
    // 이전 실행 완료 후 60초 대기
}
```

외부 API 폴링처럼 **직전 작업 완료를 보장**해야 하는 경우에 적합합니다.

#### cron — 정확한 시각 지정

Unix cron 표현식으로 실행 시각을 정밀하게 지정합니다.

```java
@Scheduled(cron = "0 * * * * *")         // 매분 0초 (1분마다)
@Scheduled(cron = "0 0 9 * * MON-FRI")   // 평일 오전 9시
@Scheduled(cron = "0 0/30 8-18 * * *")   // 오전 8시~오후 6시, 30분마다
```

> **Spring vs Unix cron 차이:** Spring은 **6자리** cron 표현식을 사용합니다 (초 필드 추가).
> Unix cron의 5자리와 다르므로 주의하세요.

```
초(0-59)  분(0-59)  시(0-23)  일(1-31)  월(1-12)  요일(0-7, MON-SUN)
  0          *          *         *          *           *
```

| 표현식 | 설명 |
| :--- | :--- |
| `"0 * * * * *"` | 매분 0초 (1분마다) |
| `"0 0 * * * *"` | 매시 정각 |
| `"0 0 0 * * *"` | 매일 자정 |
| `"0 0 9 * * MON-FRI"` | 평일 오전 9시 |
| `"0 0/15 9-18 * * *"` | 오전 9시~오후 6시, 15분마다 |

### 2.2 fixedRate vs fixedDelay 선택 기준

| 기준 | fixedRate | fixedDelay |
| :--- | :--- | :--- |
| **간격 기준** | 시작 → 시작 | 완료 → 시작 |
| **빠른 반복 보장** | ✅ 일정한 시간 간격 | ❌ 작업 시간만큼 늦어짐 |
| **직전 완료 보장** | ❌ 겹침 가능 | ✅ 항상 완료 후 시작 |
| **적합한 용도** | 상태 모니터링, 하트비트 | 외부 API 폴링, 순서 의존 배치 |

### 2.3 ThreadPoolTaskScheduler 설정

| 프로퍼티 | 기본값 | 설명 |
| :--- | :--- | :--- |
| `pool.size` | 1 | 스케줄러 스레드 수 |
| `thread-name-prefix` | `"scheduling-"` | 스레드 이름 접두사 |

> **주의:** 기본 `pool.size`는 **1**입니다. `@Scheduled` 메서드가 여러 개이고 동시에 실행될 수 있다면,
> 작업 수에 맞게 `pool.size`를 늘려야 합니다. 크기가 1이면 한 번에 하나씩만 실행됩니다.

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 4   # cron/fixedRate 태스크가 4개라면 4로 설정
      thread-name-prefix: "scheduling-"
```

### 2.4 가상 스레드와 스케줄링 — 중요한 함정

> 📌 관련 노트: [virtual-threads.md](../03-spring-application/virtual-threads.md)

가상 스레드를 활성화하면 스케줄러 스레드도 가상 스레드(= **데몬 스레드**)가 됩니다.
JVM은 실행 중인 모든 스레드가 데몬 스레드이면 종료됩니다.

**문제 상황:**
스케줄링 작업만 수행하는 애플리케이션(HTTP 서버 없음)에서 가상 스레드를 활성화하면,
`@Scheduled` 메서드가 등록되어 있어도 JVM이 즉시 종료될 수 있습니다.

**해결책:**

```yaml
spring:
  main:
    keep-alive: true  # 모든 스레드가 데몬이어도 JVM을 유지
  threads:
    virtual:
      enabled: true
```

### 2.5 `@Scheduled` + `@Async` 조합 — 비동기 스케줄 실행

기본적으로 `@Scheduled`는 단일 스케줄러 스레드에서 순차 실행됩니다.
`@Async`를 함께 붙이면 `@Scheduled`가 트리거하고, 실제 실행은 `AsyncTaskExecutor`에 위임됩니다.

```java
@Scheduled(fixedRate = 5_000)
@Async  // 실제 실행은 applicationTaskExecutor 스레드 풀에서
public CompletableFuture<Void> asyncScheduledTask() {
    // 오래 걸리는 작업
    return CompletableFuture.completedFuture(null);
}
```

> **주의:** `@Async`를 붙이면 `@EnableAsync`가 반드시 활성화되어 있어야 합니다.

### 2.6 스케줄링 빌더 빈

Spring Boot는 다음 4가지 빌더를 자동 설정합니다:

| 빌더 | 생성 대상 |
| :--- | :--- |
| `ThreadPoolTaskExecutorBuilder` | `ThreadPoolTaskExecutor` |
| `SimpleAsyncTaskExecutorBuilder` | `SimpleAsyncTaskExecutor` |
| `ThreadPoolTaskSchedulerBuilder` | `ThreadPoolTaskScheduler` |
| `SimpleAsyncTaskSchedulerBuilder` | `SimpleAsyncTaskScheduler` |

`SimpleAsyncTaskExecutorBuilder`와 `SimpleAsyncTaskSchedulerBuilder`는
가상 스레드가 활성화되면 자동으로 가상 스레드를 사용하도록 설정됩니다.

```java
// 빌더를 주입받으면 spring.task.scheduling.* 프로퍼티가 자동 반영됩니다
@Bean
ThreadPoolTaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder builder) {
    return builder.build();
}
```
