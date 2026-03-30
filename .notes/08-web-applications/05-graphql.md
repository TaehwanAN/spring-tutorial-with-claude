# Spring for GraphQL

> 참고: https://docs.spring.io/spring-boot/reference/web/spring-graphql.html

---

## 목차
1. [GraphQL 개요](#1-graphql-개요)
2. [의존성 및 스타터](#2-의존성-및-스타터)
3. [스키마 설정](#3-스키마-설정)
4. [Annotated Controllers (권장)](#4-annotated-controllers-권장)
5. [트랜스포트 설정](#5-트랜스포트-설정)
6. [예외 처리](#6-예외-처리)
7. [개발 도구 (GraphiQL)](#7-개발-도구-graphiql)
8. [전문가의 노트](#8-전문가의-노트)

---

## 1. GraphQL 개요

REST API와 달리, 클라이언트가 **필요한 데이터만 지정**하여 요청하는 쿼리 언어.

```
REST:
  GET /api/members/1         → { id, name, email, createdAt, ... } (서버가 모든 필드 반환)
  GET /api/members/1/orders  → 별도 요청

GraphQL:
  query {
    member(id: "1") {
      name
      orders {
        total
      }
    }
  }
  → { member: { name: "홍길동", orders: [{ total: 50000 }] } } (요청한 필드만 반환, 1번 요청)
```

---

## 2. 의존성 및 스타터

```xml
<!-- GraphQL 코어 + HTTP 트랜스포트 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>

<!-- HTTP 트랜스포트 (Spring MVC) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 또는 WebFlux 트랜스포트 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 3. 스키마 설정

```yaml
spring:
  graphql:
    schema:
      locations: classpath:graphql/**/   # 스키마 파일 위치 (기본값)
      file-extensions: graphqls, gqls   # 스키마 파일 확장자
      introspection:
        enabled: true                   # 인트로스펙션 쿼리 허용 (기본: true, 운영에서는 false 권장)
```

스키마 파일 예시 (`src/main/resources/graphql/schema.graphqls`):

```graphql
type Query {
    member(id: ID!): Member
    members: [Member!]!
}

type Mutation {
    registerMember(name: String!, email: String!): Member!
}

type Member {
    id: ID!
    name: String!
    email: String!
    createdAt: String
}
```

---

## 4. Annotated Controllers (권장)

Spring MVC의 `@Controller`와 유사한 방식으로 GraphQL 핸들러를 작성한다.

```java
@Controller
public class MemberGraphQlController {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;

    // Query 처리
    @QueryMapping  // Query.member(id: ID!)에 매핑
    public MemberInfo member(@Argument String id) {
        return getMemberUseCase.getById(id);
    }

    // Mutation 처리
    @MutationMapping
    public MemberInfo registerMember(@Argument String name, @Argument String email) {
        return registerMemberUseCase.register(new RegisterMemberCommand(name, email));
    }

    // Subscription 처리 (WebFlux 필요)
    @SubscriptionMapping
    public Flux<MemberInfo> memberCreated() {
        return memberEventPublisher.memberCreatedEvents();
    }
}
```

---

## 5. 트랜스포트 설정

### HTTP 트랜스포트

```yaml
spring:
  graphql:
    http:
      path: /graphql          # 기본값
    websocket:
      path: /graphql-ws       # WebSocket 구독용
    cors:
      allowed-origins: "http://localhost:3000"
```

### CORS 설정

```java
@Configuration
public class GraphQlConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/graphql")
                .allowedOrigins("http://localhost:3000");
    }
}
```

### 인터셉터 (WebGraphQlInterceptor)

```java
@Component
public class LoggingInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        log.info("GraphQL 요청: {}", request.getDocument());
        return chain.next(request)
                .doOnNext(response -> log.info("GraphQL 응답: {}", response.getData()));
    }
}
```

---

## 6. 예외 처리

```java
@Component
public class BusinessExceptionResolver implements DataFetcherExceptionResolver {

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable ex,
                                                      DataFetchingEnvironment env) {
        if (ex instanceof BusinessException bex) {
            GraphQLError error = GraphQLError.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(bex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            return Mono.just(List.of(error));
        }
        return Mono.empty();  // 처리하지 않음 → 다음 Resolver에 위임
    }
}
```

---

## 7. 개발 도구 (GraphiQL)

```yaml
spring:
  graphql:
    graphiql:
      enabled: true   # 기본값: false (개발 환경에서만 활성화 권장)
      path: /graphiql  # 기본값
    schema:
      printer:
        enabled: true  # /graphql/schema 엔드포인트에서 스키마 출력
```

활성화 후 브라우저에서 `http://localhost:9999/graphiql`로 접근.

---

## 8. 전문가의 노트

> 💡 **GraphQL vs REST 선택 기준**
> - **REST 적합**: 단순 CRUD, 공개 API, HTTP 캐싱이 중요한 경우
> - **GraphQL 적합**: 다양한 클라이언트(모바일/웹)가 다른 데이터 형태를 요구, 중첩 데이터 쿼리가 많은 경우

> 🔍 **N+1 문제와 DataLoader**
> GraphQL에서 연관 데이터를 개별 쿼리로 해결하면 N+1 쿼리가 발생한다.
> Spring for GraphQL은 `BatchLoaderRegistry`를 통해 `DataLoader` 패턴을 지원한다.

> ⚠️ **운영 환경 인트로스펙션 비활성화**
> `spring.graphql.schema.introspection.enabled=false` — 스키마 정보 노출 방지.
> 공격자가 스키마를 탐색하여 취약점을 찾는 것을 방지한다.
