# 015-2. 현대적인 로깅 시스템

## 들어가며: 로깅의 진화

| 시대 | 방식 | 한계 |
|------|------|------|
| 모놀리식 | 서버 직접 접속 → `tail -f app.log` | 서버가 늘어나면 불가능 |
| 초기 클라우드 | 중앙 로그 서버에 파일 전송 (텍스트) | 검색은 되지만 구조화가 없어 분석이 어려움 |
| 현재 (MSA) | 구조화 JSON + 분산 추적 ID + 중앙 플랫폼 | 표준화가 필요 (벤더 종속 문제 등장) |
| 방향 | **OpenTelemetry** 표준으로 수렴 | 아직 로그 spec 은 성숙 중 |

---

## 1. 중앙 집중형 로깅 (Centralized Logging)

MSA/컨테이너 환경에서 서비스 인스턴스가 수백 개로 흩어지면
개별 서버에 접속해 로그를 확인하는 것은 불가능하다.

```
[서비스 A Pod 1] ─┐
[서비스 A Pod 2] ─┤  로그 수집 에이전트   중앙 저장소    시각화
[서비스 B Pod 1] ─┼─ (Filebeat/Fluentd) → (ES / Loki) → (Kibana / Grafana)
[서비스 C Pod 1] ─┘
```

**핵심 파이프라인 구성요소:**

| 역할 | 도구 예시 | 설명 |
|------|----------|------|
| 수집 (Ship) | Filebeat, Fluentd, Promtail | 파일/stdout 로그를 읽어 전달 |
| 저장·인덱싱 (Store) | Elasticsearch, Loki | 빠른 검색을 위한 인덱싱 |
| 시각화 (Visualize) | Kibana, Grafana | 대시보드, 쿼리, 알람 |

---

## 2. 구조화 로깅 (Structured Logging)

### 왜 JSON인가?

```
# 텍스트 로그 (사람이 읽기 좋지만 파싱이 어려움)
2026-03-28 12:00:00 ERROR 사용자 42번의 결제 실패 - 잔액 부족

# JSON 로그 (컴퓨터가 필드 단위로 바로 집계·필터 가능)
{"@timestamp":"2026-03-28T12:00:00Z","level":"ERROR","userId":42,"event":"payment_failed","reason":"insufficient_balance","traceId":"4bf92f35..."}
```

JSON 이면 `userId=42 AND level=ERROR` 같은 조건으로 즉시 필터링·집계 가능하고,
알람 규칙도 코드 없이 대시보드에서 설정할 수 있다.

### 이 프로젝트의 구조화 로깅: logstash-logback-encoder

`LogstashEncoder` 가 각 로그 이벤트를 JSON 한 줄(NDJSON)로 출력한다.
MDC 에 있는 모든 키-값을 최상위 JSON 필드로 자동 포함한다.

```json
{
  "@timestamp": "2026-03-28T18:00:00.000+00:00",
  "@version": "1",
  "message": "요청 처리 완료",
  "logger_name": "com.demo.myapplication.web.SomeController",
  "thread_name": "http-nio-9090-exec-1",
  "level": "INFO",
  "service.name": "spring-tutorial",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "spanId": "00f067aa0ba902b7"
}
```

---

## 3. 분산 추적과 Correlation ID

### 문제: 요청이 여러 서비스를 거칠 때

```
사용자 요청
    │
    ▼
[API Gateway] ─ traceId: 4bf92f35...
    │
    ▼
[Order Service] ─ traceId: 4bf92f35... (동일 trace)
    │                spanId: abc001 (새 span)
    ▼
[Payment Service] ─ traceId: 4bf92f35... (동일 trace)
    │                 spanId: abc002 (새 span)
    ▼
[DB]
```

모든 서비스의 로그가 동일한 `traceId` 를 갖는다.
→ Kibana/Grafana 에서 `traceId = "4bf92f35..."` 하나로 전체 요청 흐름을 한 화면에 볼 수 있다.

### Spring Boot 에서의 자동 주입 흐름

```
HTTP 요청 수신
    │
    ▼
Spring MVC Observation 생성
    │
    ▼
micrometer-tracing-bridge-otel
    → OTel SDK 로 Span 시작
    → TraceId(128-bit) / SpanId(64-bit) 생성
    │
    ▼
MDCScopeDecorator
    → MDC.put("traceId", "4bf92f35...")
    → MDC.put("spanId",  "00f067aa...")
    │
    ▼
로그 기록 시 LogstashEncoder 가 MDC 자동 포함
```

---

## 4. 대표 기술 스택 비교

### ELK / EFK 스택

```
앱 → Filebeat → Logstash(변환) → Elasticsearch(저장) → Kibana(시각화)
앱 → Fluentd  → Elasticsearch → Kibana
```

| 항목 | 내용 |
|------|------|
| 강점 | 강력한 전문 검색, 풍부한 시각화, 성숙한 생태계 |
| 약점 | 리소스(메모리/CPU) 소비가 크고 운영 복잡도 높음 |
| 로그 포맷 | ECS (Elastic Common Schema) JSON 권장 |
| 적합한 경우 | 로그 검색·분석이 핵심인 대규모 시스템 |

### PLG 스택 (Grafana 생태계)

```
앱 → Promtail(수집) → Loki(저장, 인덱싱 최소화) → Grafana(시각화)
```

| 항목 | 내용 |
|------|------|
| 강점 | 가볍고 저렴, Prometheus(메트릭)·Tempo(추적)와 동일 대시보드에 통합 |
| 약점 | 전문 검색 기능이 ES 대비 약함 (레이블 기반 쿼리) |
| 로그 포맷 | 구조화 JSON 권장 (별도 스키마 표준 없음) |
| 적합한 경우 | 메트릭·추적·로그를 Grafana 한 곳에서 보고 싶은 경우 |

---

## 5. OpenTelemetry (OTel): 2026년의 표준

### 핵심 개념

OpenTelemetry 는 **Observability 데이터의 생성·수집·내보내기를 표준화**하는 CNCF 프로젝트다.
특정 벤더에 종속되지 않고, 하나의 계측 코드로 어떤 백엔드에든 데이터를 보낼 수 있다.

```
애플리케이션 (OTel SDK)
    │
    │  OTLP (OpenTelemetry Protocol)
    ▼
OTel Collector
    ├── Logs    → Elasticsearch / Loki
    ├── Metrics → Prometheus / InfluxDB
    └── Traces  → Jaeger / Grafana Tempo / Zipkin
```

### OTel의 3가지 신호 (Signals)

| 신호 | 질문 | 도구 예시 |
|------|------|----------|
| **Logs** | "무슨 일이 있었나?" | Loki, Elasticsearch |
| **Metrics** | "현재 상태가 어떤가?" | Prometheus, InfluxDB |
| **Traces** | "어디서 얼마나 걸렸나?" | Jaeger, Grafana Tempo |

이 세 가지를 통합해서 보는 것이 **Observability (관측 가능성)**이다.

### ECS vs OTel 비교

| 항목 | ECS (Elastic) | OpenTelemetry |
|------|--------------|---------------|
| 주도 | Elastic 사 | CNCF (벤더 중립) |
| 목적 | Elastic Stack 최적화 | 모든 백엔드 연동 |
| 필드명 | `log.level`, `service.name` (dot notation) | `severity`, `resource.service.name` |
| 벤더 종속 | Elastic 스택에 최적화 | 어느 백엔드나 가능 |
| Spring Boot 지원 | 3.4+에서 native 지원 | 3.x 에서 Micrometer Tracing 으로 통합 |

### 왜 이 프로젝트는 OTel 방식을 선택했나?

1. **벤더 중립**: logstash-logback-encoder 로 만든 JSON 은 ELK, PLG, Splunk 어디든 연결 가능.
2. **표준 Trace ID**: Micrometer Tracing + OTel bridge 가 생성하는 traceId 는 W3C TraceContext 표준 (128-bit hex) 을 따름.
3. **Spring Boot 네이티브**: Spring Boot 3.x 의 Observation API 와 완전히 통합되어 별도 에이전트 없이 동작.

---

## 6. 이 프로젝트의 OTel 구성 요약

### 의존성

```xml
<!-- OTel SDK 위에서 동작하는 Micrometer Tracing 브리지 -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
    <!-- Spring Boot BOM 버전 관리 -->
</dependency>

<!-- MDC 값(traceId/spanId 포함)을 JSON 으로 직렬화 -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### application.yml

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 모든 요청 추적 (운영에서는 0.1~0.2 권장)
```

### logback-spring.xml (prod)

```xml
<appender name="OTEL_JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <customFields>{"service.name":"${APP_NAME}"}</customFields>
  </encoder>
</appender>
```

### 실제 HTTP 요청 로그 (자동 주입)

```json
{
  "@timestamp": "2026-03-28T18:00:00.000+00:00",
  "message": "GET /actuator/health 처리 완료",
  "level": "INFO",
  "service.name": "spring-tutorial",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "spanId": "00f067aa0ba902b7"
}
```

---

## 7. 향후 확장: 완전한 OTel 파이프라인

현재 구성은 로그에 OTel Trace ID 를 포함시키는 수준이다.
완전한 OTel 파이프라인은 추가 의존성이 필요하다.

```
현재:
앱 (Logstash JSON) ──파일──▶ Filebeat ──▶ ELK / Loki

완전한 OTel:
앱 (OTel SDK) ──OTLP──▶ OTel Collector ──▶ Loki (로그)
                                         ──▶ Tempo (추적)
                                         ──▶ Prometheus (메트릭)
```

추가 필요 의존성:
```xml
<!-- OTLP 로 OTel Collector 에 직접 전송 -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

추가 필요 설정:
```yaml
management:
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces
```

Spring Boot 3.4+ 에서는 로그 자체도 OTLP 로 전송하는 native 지원이 추가됐다.
(`logging.structured.format.console=otlp` 또는 `ecs`)
