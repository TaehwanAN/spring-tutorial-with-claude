# Kotlin Support

> 참고: https://docs.spring.io/spring-boot/reference/features/kotlin.html
>
> **현재 프로젝트에는 미적용** — 추후 Kotlin 전환 시 참고

---

## 요구사항

- 최소 Kotlin 버전: **2.2.x**
- 필수 의존성: `kotlin-stdlib`, `kotlin-reflect`
- 필수 컴파일러 플래그: `-Xannotation-default-target=param-property`
  - Kotlin 2.2.x 경고 방지, 미래 기본 동작과 일치시키기 위해 필요

---

## 필수 플러그인

| 플러그인 | 역할 |
|--------|------|
| `kotlin-spring` | Spring 어노테이션 클래스를 자동으로 `open` 처리 (Kotlin 클래스는 기본 `final` → 프록시 불가 문제 해결) |
| Jackson Kotlin module | 클래스패스에 존재 시 자동 등록, JSON 직렬화/역직렬화 지원 |

> `start.spring.io`에서 Kotlin 프로젝트 생성 시 기본 포함됨.

---

## 핵심 기능

### Null-Safety

- Kotlin 컴파일 타임 null 체크로 NPE 제거
- `Optional` 래퍼 불필요
- Spring은 [JSpecify](https://jspecify.dev/) 어노테이션으로 null-safety 제공
- Kotlin 2.1+: `org.jspecify.annotations` 패키지의 nullable 어노테이션을 엄격 적용

### `runApplication` 관용구

```kotlin
@SpringBootApplication
class MyApplication

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}
```

커스터마이징:
```kotlin
runApplication<MyApplication>(*args) {
    setBannerMode(OFF)
}
```

`SpringApplication.run(MyApplication::class.java, *args)`의 Kotlin 관용적 대체.

---

## 의존성 관리

- Spring Boot가 **Kotlin BOM** 자동 import
- **Maven**: `kotlin.version` 프로퍼티로 버전 커스터마이징
- **Gradle**: Spring Boot 플러그인이 Kotlin 버전 자동 정렬
- **코루틴**: Kotlin Coroutines BOM으로 관리, `kotlin-coroutines.version`으로 커스터마이징
- `kotlinx-coroutines-reactor`: Reactor 리액티브 프로젝트에 기본 제공

---

## `@ConfigurationProperties` with Data Class

```kotlin
@ConfigurationProperties("example.kotlin")
data class KotlinExampleProperties(
    val name: String,
    val description: String,
    val myService: MyService
) {
    data class MyService(
        val apiToken: String,
        val uri: URI
    )
}
```

- `val` (불변 프로퍼티)로 생성자 바인딩 지원
- **주의**: Value Class는 Java 상호운용성 제약으로 지원 제한, 기본값 바인딩 불가 → data class 사용 권장
- 커스텀 메타데이터 생성: `kapt` + `spring-boot-configuration-processor` 사용

---

## 테스트

- **JUnit 5** 기본 제공 및 권장
- `@BeforeAll`, `@AfterAll`을 non-static 메서드에 사용 가능 → Kotlin에 이상적
- **목킹**: [MockK](https://mockk.io/) 권장 (Mockito 대신)
- [SpringMockK](https://github.com/Ninja-Squad/springmockk): `@MockkBean`, `@SpykBean` 제공
