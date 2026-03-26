아래는 제공해주신 **Spring Boot Application Availability** 문서의 **한글 번역 + 핵심 설명**입니다.

---

# 📌 Application Availability (애플리케이션 가용성)

플랫폼(예: Kubernetes)에 배포된 애플리케이션은
자신의 상태를 **가용성 정보(availability)** 형태로 제공할 수 있습니다.

Spring Boot는 기본적으로 다음 두 가지 상태를 지원합니다:

* **Liveness (생존 상태)**
* **Readiness (준비 상태)**

👉 만약 Spring Boot Actuator 를 사용하면
이 상태들은 **health endpoint 그룹**으로 자동 노출됩니다.

또한, `ApplicationAvailability` 인터페이스를 주입받아
애플리케이션 내부에서도 상태를 조회할 수 있습니다.

---

# 📌 Liveness State (생존 상태)

### ✔ 개념 (번역)

“Liveness” 상태는
애플리케이션이 **정상적으로 동작 가능한 내부 상태인지**,
또는 **현재 실패 상태에서 스스로 복구 가능한지**를 나타냅니다.

* ❌ Liveness가 깨진 경우
  → 애플리케이션이 **복구 불가능한 상태**
  → 인프라(Kubernetes 등)가 **재시작해야 함**

---

### ✔ 중요한 설계 원칙

> Liveness는 **외부 시스템(DB, API 등)에 의존하면 안 된다**

이유:

* DB 장애 → Liveness 실패
* → 컨테이너 재시작
* → 전체 시스템에서 **연쇄 재시작 (cascading failure)** 발생

👉 즉, Liveness는 **오직 내부 상태 기준**

---

### ✔ Spring Boot 기준

* Spring의 핵심 상태 = **ApplicationContext**
* 이 컨텍스트가 정상적으로 올라오면 → **LIVE 상태**

👉 기준 시점:

* `ApplicationContext refresh 완료 시점`

---

# 📌 Readiness State (준비 상태)

### ✔ 개념 (번역)

“Readiness” 상태는
애플리케이션이 **트래픽을 받을 준비가 되었는지**를 나타냅니다.

* ❌ Readiness 실패
  → 플랫폼은 해당 인스턴스로 트래픽을 보내지 않음

---

### ✔ 언제 발생하는가?

* 애플리케이션 시작 중
* `CommandLineRunner`, `ApplicationRunner` 실행 중
* 서버가 과부하 상태일 때
* 특정 리소스 준비 안 된 경우

---

### ✔ Spring Boot 기준

👉 다음 조건 만족 시 Ready:

* Application 시작 완료
* CommandLineRunner / ApplicationRunner 실행 완료

---

### ✔ 중요한 권장사항

> 초기화 작업은 `@PostConstruct`가 아니라
> `CommandLineRunner` 또는 `ApplicationRunner`에서 수행해야 함

이유:

* Readiness 상태와 lifecycle이 정확히 맞춰짐

---

# 📌 상태 관리 (Managing Availability)

## 1️⃣ 상태 조회

```java
@Autowired
ApplicationAvailability availability;
```

→ 현재 상태 확인 가능

---

## 2️⃣ 상태 변경 이벤트 리스닝

### ✔ 예제: Readiness 상태를 파일로 export

```java
@Component
public class MyReadinessStateExporter {

    @EventListener
    public void onStateChange(AvailabilityChangeEvent<ReadinessState> event) {
        switch (event.getState()) {
            case ACCEPTING_TRAFFIC -> {
                // /tmp/healthy 파일 생성
            }
            case REFUSING_TRAFFIC -> {
                // 파일 삭제
            }
        }
    }
}
```

### ✔ 설명

* 상태 변화 이벤트 수신
* Kubernetes의 `exec probe`에서 파일 존재 여부로 체크 가능

---

## 3️⃣ 상태 직접 변경 (Liveness)

### ✔ 예제: 치명적 장애 발생 시

```java
@Component
public class MyLocalCacheVerifier {

    private final ApplicationEventPublisher eventPublisher;

    public void checkLocalCache() {
        try {
            // 캐시 검증
        } catch (CacheCompletelyBrokenException ex) {
            AvailabilityChangeEvent.publish(
                this.eventPublisher,
                ex,
                LivenessState.BROKEN
            );
        }
    }
}
```

### ✔ 설명

* 복구 불가능한 상태 발생 시
* Liveness를 `BROKEN`으로 변경
* → Kubernetes가 컨테이너 재시작

---

# 📌 Kubernetes와의 연동

Spring Boot는 기본적으로 다음을 제공:

* Liveness Probe (HTTP)
* Readiness Probe (HTTP)

👉 Actuator 사용 시:

```
/actuator/health/liveness
/actuator/health/readiness
```

---

# 📌 핵심 정리 (실무 관점)

| 구분    | Liveness         | Readiness      |
| ----- | ---------------- | -------------- |
| 의미    | 살아있는가            | 트래픽 받을 준비 되었는가 |
| 실패 시  | 재시작              | 트래픽 차단         |
| 기준    | 내부 상태            | 외부 포함 가능       |
| 대표 사용 | JVM 상태, deadlock | DB 연결, 외부 API  |

---

# 📌 실무에서 자주 하는 실수

❌ Liveness에 DB 체크 넣기
→ 장애 시 전체 서비스 재시작 폭발

❌ 초기화 로직을 @PostConstruct에 작성
→ Readiness 제어 불가

---

# 📌 한 줄 요약

> Liveness는 "죽었냐?", Readiness는 "일할 준비 됐냐?"
> → Kubernetes가 각각 **재시작 vs 트래픽 차단**을 결정한다.

---

필요하면
👉 "Actuator + Kubernetes probe 실제 설정 예시"
👉 "헬스 체크 커스터마이징 (@ReadinessState, HealthIndicator)"
도 이어서 깊게 설명해줄게.
