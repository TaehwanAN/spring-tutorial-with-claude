# Graceful Shutdown

> 참고: https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html

---

## 목차
1. [Graceful Shutdown이란?](#1-graceful-shutdown이란)
2. [설정 방법](#2-설정-방법)
3. [동작 흐름](#3-동작-흐름)
4. [지원 웹 서버](#4-지원-웹-서버)
5. [이 프로젝트 적용](#5-이-프로젝트-적용)
6. [전문가의 노트](#6-전문가의-노트)

---

## 1. Graceful Shutdown이란?

애플리케이션 종료 신호(SIGTERM, `Ctrl+C`)를 받았을 때,
**진행 중인 요청을 완료할 때까지 기다린 후** 종료하는 방식.

```
SIGTERM 수신
    ├─ 즉시 종료 (server.shutdown=immediate, 기본값)
    │   → 처리 중이던 요청이 갑자기 끊김 → 클라이언트 오류
    │
    └─ Graceful Shutdown (server.shutdown=graceful)
        → 새 요청 수락 중지 (포트는 열려 있지만 503 반환 또는 연결 거부)
        → 진행 중인 요청 완료까지 대기
        → timeout-per-shutdown-phase 초과 시 강제 종료
        → 완전 종료
```

---

## 2. 설정 방법

```yaml
# application.yml
server:
  shutdown: graceful   # 기본값: immediate

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # 유예 기간 (기본값: 30s)
```

| 설정값 | 설명 |
|--------|------|
| `server.shutdown=immediate` | 즉시 종료 (기본값) |
| `server.shutdown=graceful` | 진행 중 요청 완료 후 종료 |
| `spring.lifecycle.timeout-per-shutdown-phase` | 각 종료 단계별 최대 대기 시간 |

---

## 3. 동작 흐름

```
1. SIGTERM 수신
2. Spring Application Context 종료 시작 (SmartLifecycle 역순 실행)
3. 웹 서버: 새 연결 수락 중지
4. 진행 중인 요청 완료 대기 (최대 timeout-per-shutdown-phase)
5. 타임아웃 도달 → 나머지 요청 강제 중단
6. 빈 소멸 (DisposableBean, @PreDestroy) 실행
7. JVM 종료
```

Kubernetes 환경에서의 권장 설정:
```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # K8s terminationGracePeriodSeconds보다 작게
```

---

## 4. 지원 웹 서버

| 서버 | Servlet 스택 | Reactive 스택 |
|------|------------|-------------|
| Tomcat | ✅ | — |
| Jetty | ✅ | ✅ |
| Reactor Netty | — | ✅ |
| Undertow | ✅ | ✅ |

---

## 5. 이 프로젝트 적용

```yaml
# application.yml (공통 설정 블록)
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

기존 `ApplicationShutdownHandler.java` (`.notes/03-spring-application/application-exit.md` 참조)와의 관계:
- `@PreDestroy` 메서드: 빈 단위 정리 작업 (DB 연결 해제, 파일 플러시 등)
- `Graceful Shutdown`: 서버 수준에서 HTTP 요청 완료를 보장
- 두 메커니즘은 독립적으로 동작하며 함께 사용 가능하다.

---

## 6. 전문가의 노트

> 💡 **IDE에서의 Graceful Shutdown**
> IntelliJ 등 IDE에서 앱을 중지할 때 SIGKILL이 전송되면 Graceful Shutdown이 동작하지 않는다.
> SIGTERM을 보내도록 IDE 종료 설정을 확인해야 한다.
> (IntelliJ: Run Configuration → "Exit gracefully" 또는 `kill -SIGTERM <pid>` 사용)

> ⚠️ **timeout-per-shutdown-phase 단계별 적용**
> 이 타임아웃은 각 `SmartLifecycle` **단계(phase)별**로 적용된다.
> 여러 단계가 있으면 각 단계마다 최대 30초를 기다릴 수 있다.
> 총 종료 시간 = 단계 수 × timeout-per-shutdown-phase (최악의 경우).

> 🔍 **Kubernetes 연동**
> K8s `terminationGracePeriodSeconds` > Spring `timeout-per-shutdown-phase` 로 설정해야 한다.
> 예: K8s 60초, Spring 30초. Spring이 먼저 정상 종료되고 K8s가 나머지를 정리한다.
> Readiness Probe가 SIGTERM 직후 `OUT_OF_SERVICE`로 전환되어 트래픽이 끊기는지 확인 필요.
> (`application-availability.md` 참조)
