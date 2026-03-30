이 패턴은 **Spring MVC의 디스패처 내부 자료구조를 런타임에 직접 조작**하는 방식이다.
핵심은 `RequestMappingHandlerMapping`이 들고 있는 **매핑 레지스트리**에 `RequestMappingInfo → HandlerMethod`를 동적으로 추가/삭제하는 것.

아래를 순서대로 보면 전체 동작이 선명해진다.

---

# 1. 내부 동작 원리 (DispatcherServlet 관점)

Spring MVC 요청 흐름을 축약하면:

```text
DispatcherServlet
 → HandlerMapping (여러 개)
   → RequestMappingHandlerMapping
      → MappingRegistry 조회
 → HandlerAdapter
 → Controller 실행
```

여기서 우리가 건드리는 지점:

```text
RequestMappingHandlerMapping.MappingRegistry
```

---

## 1.1 MappingRegistry 구조

내부적으로 이런 맵을 유지한다:

```text
Map<RequestMappingInfo, HandlerMethod>
```

그리고 추가로:

```text
path → mapping index
method → mapping index
```

👉 그래서 빠르게 매칭됨

---

# 2. registerMapping()의 실제 의미

```java
handlerMapping.registerMapping(mappingInfo, handler, method);
```

이 한 줄이 하는 일:

```text
1. RequestMappingInfo 생성 (path, method, params 등)
2. HandlerMethod 생성 (bean + method)
3. MappingRegistry에 등록
4. URL lookup index 업데이트
```

👉 즉:

```text
Spring이 부팅 시 하던 작업을 런타임에 수행
```

---

# 3. 제대로 구현하려면 필요한 구성

단순 예제는 위험하다. 실무에서는 최소 이 구조 필요:

---

## 3.1 Controller는 반드시 Bean이어야 함 (중요)

❌ 잘못된 코드:

```java
new DynamicController()
```

👉 문제:

* DI 안 됨
* AOP 안 먹음
* @Transactional 깨짐

---

✅ 올바른 방식:

```java
@Component
public class DynamicController {
    public ResponseEntity<?> handle(HttpServletRequest request) {
        return ResponseEntity.ok("dynamic");
    }
}
```

```java
@Component
@RequiredArgsConstructor
public class DynamicHandlerMapping {

    private final RequestMappingHandlerMapping handlerMapping;
    private final DynamicController controller;

    public void register(String path) throws NoSuchMethodException {

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(path)
                .methods(RequestMethod.GET)
                .build();

        Method method = DynamicController.class.getMethod("handle", HttpServletRequest.class);

        handlerMapping.registerMapping(mappingInfo, controller, method);
    }
}
```

---

# 4. unregisterMapping (삭제도 가능)

```java
handlerMapping.unregisterMapping(mappingInfo);
```

👉 주의:

* 동일한 `RequestMappingInfo` 객체 필요
* 보통 Map으로 관리해야 함

---

## 추천 구조

```java
Map<String, RequestMappingInfo> registry = new ConcurrentHashMap<>();
```

---

# 5. 충돌 처리 (매우 중요)

Spring은 기본적으로:

```text
같은 path + method → 충돌 시 Exception
```

---

## 해결 전략

### 방법 1. 덮어쓰기

```java
try {
    handlerMapping.registerMapping(mappingInfo, controller, method);
} catch (IllegalStateException e) {
    handlerMapping.unregisterMapping(mappingInfo);
    handlerMapping.registerMapping(mappingInfo, controller, method);
}
```

---

### 방법 2. 버전 관리

```text
/api/v1/users
/api/v2/users
```

---

# 6. Path 패턴 (고급)

```java
.paths("/dynamic/{tenant}/users/{id}")
```

Spring 내부에서는:

```text
AntPathMatcher 또는 PathPatternParser
```

사용

---

## PathVariable 처리

```java
public ResponseEntity<?> handle(
    @PathVariable String tenant,
    @PathVariable String id
)
```

👉 동적 매핑에서도 정상 동작

---

# 7. HTTP Method / 조건 다양화

```java
RequestMappingInfo mappingInfo = RequestMappingInfo
        .paths(path)
        .methods(RequestMethod.GET, RequestMethod.POST)
        .params("version=1")
        .headers("X-Tenant=abc")
        .consumes(MediaType.APPLICATION_JSON_VALUE)
        .produces(MediaType.APPLICATION_JSON_VALUE)
        .build();
```

👉 거의 모든 조건 동적 설정 가능

---

# 8. 성능 특성

## 장점

* Lookup은 여전히 O(1) 수준
* DispatcherServlet 변경 없음

## 단점

* 등록/삭제는 thread-safe lock 사용 → 비용 있음

---

# 9. 동적 로딩 시점 전략

---

## 9.1 애플리케이션 시작 시 DB 로딩

```java
@PostConstruct
public void init() {
    routes.forEach(route -> register(route.getPath()));
}
```

---

## 9.2 실시간 반영 (Admin API)

```java
@PostMapping("/admin/routes")
public void addRoute(@RequestBody RouteDto dto) {
    dynamicHandlerMapping.register(dto.getPath());
}
```

---

## 9.3 캐싱

```text
DB → 메모리 Map → HandlerMapping
```

---

# 10. 실무에서 반드시 고려해야 할 것

---

## 10.1 메모리 누수

* unregister 안 하면 계속 쌓임

---

## 10.2 멀티 인스턴스 문제

```text
서버 2대 이상이면 mapping 동기화 필요
```

👉 해결:

* Redis Pub/Sub
* Kafka event

---

## 10.3 보안

```text
임의 path 등록 → 공격 surface 증가
```

---

## 10.4 OpenAPI 불일치

```text
Swagger에는 안 보임
```

---

# 11. 언제 이 패턴을 써야 하나

---

## 적합

* SaaS tenant별 API
* 플러그인 시스템
* Feature toggle 기반 endpoint

---

## 부적합

* 고성능 gateway (→ Kong)
* 단순 API 서버

---

# 12. 핵심 요약

---

## 이 패턴의 본질

```text
Spring의 "정적 라우팅 테이블"을
런타임에 직접 수정하는 것
```

---

## 가장 중요한 3가지

```text
1. Controller는 반드시 Bean으로 사용
2. mappingInfo를 반드시 따로 관리 (삭제 위해)
3. 멀티 인스턴스 동기화 고려
```

---

# 13. 한 단계 더 깊은 인사이트

이 방식은 사실:

```text
Spring을 "미니 API Gateway"로 만드는 것
```

이다.

---

원하면 다음 단계로:

* Spring Cloud Gateway에서 동일 개념 구현 비교
* DB + HandlerMapping + 캐시 구조 설계 (실무 아키텍처)
* Path trie 직접 구현해서 초고속 라우팅 만드는 방법

까지 이어서 설명해줄게.
