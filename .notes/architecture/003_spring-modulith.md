좋은 질문이다.
클린 아키텍처는 “지키자”로는 절대 유지되지 않고, **구조적으로 강제해야만 유지된다.**

아래는 **Spring 프로젝트에서 실제로 사용하는 강제 방법 4가지 (강도 순)**이다.

---

# 📌 1. 패키지 구조로 1차 강제 (기본)

## ✅ 구조 자체를 명확히 나눈다

```text
com.example
 ├─ user
 │   ├─ domain
 │   ├─ application
 │   ├─ infrastructure
 │   └─ interface
```

👉 규칙:

* `domain` → 아무것도 의존 ❌
* `application` → domain만 의존
* `infrastructure` → domain 의존 가능
* `interface` → application 의존

---

## ❗ 문제

👉 개발자가 마음만 먹으면 깨짐

```java
// domain에서 spring import 가능 (막을 방법 없음)
import org.springframework.stereotype.Service;
```

---

# 📌 2. Gradle/Maven 멀티 모듈 (강력 추천)

👉 가장 효과적인 방법

---

## ✅ 모듈 구조

```text
project
 ├─ user-domain
 ├─ user-application
 ├─ user-infrastructure
 ├─ user-interface
 └─ common
```

---

## ✅ 의존성 강제

### domain (최상위)

```gradle
dependencies {
    // 아무것도 없음 (중요)
}
```

---

### application

```gradle
dependencies {
    implementation project(":user-domain")
}
```

---

### infrastructure

```gradle
dependencies {
    implementation project(":user-domain")
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
}
```

---

### interface

```gradle
dependencies {
    implementation project(":user-application")
}
```

---

## 🔥 효과

👉 물리적으로 차단됨

* domain → spring import ❌ (dependency 없음)
* application → JPA 접근 ❌
* controller → domain 직접 접근 제한 가능

---

## 📌 핵심

> **컴파일 타임에 구조를 강제**

---

# 📌 3. ArchUnit으로 룰 검사 (자동 테스트)

👉 구조 위반을 테스트로 막음

---

## ✅ 의존성 추가

```gradle
testImplementation 'com.tngtech.archunit:archunit'
```

---

## ✅ 룰 정의

```java
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    @Test
    void domain_should_not_depend_on_spring() {
        var classes = new ClassFileImporter().importPackages("com.example");

        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .check(classes);
    }
}
```

---

## 🔥 효과

* domain에서 spring 쓰면 → 테스트 실패
* PR 단계에서 자동 검증 가능

---

# 📌 4. 코드 리뷰 + 컨벤션 (현실 필수)

👉 기술보다 더 중요함

---

## ✅ 예시 룰

* domain에는 @Entity 금지
* repository interface는 domain에만
* usecase는 하나의 기능만

---

## ❗ 현실

👉 ArchUnit만으로 부족

* “개념 위반”은 못 잡음
* (ex: 도메인 로직이 service로 빠지는 문제)

---

# 📌 5. Spring Modulith (추가 옵션)

👉 Spring 공식에서 미는 방식

---

## ✅ 특징

* 모듈 단위 구조 강제
* 내부 접근 제한 가능

---

## 예:

```java
@ApplicationModule
package com.example.user;
```

👉 모듈 간 의존성 관리 가능

---

# 📌 6. 실무 추천 조합 (중요)

👉 가장 많이 쓰는 현실적인 조합:

---

## ✅ Level 1 (기본)

* 도메인 패키지 분리
* 코드 리뷰

---

## ✅ Level 2 (추천)

* * ArchUnit

---

## ✅ Level 3 (강력 추천)

* * 멀티 모듈

---

## ✅ Level 4 (대규모)

* * Modulith or MSA

---

# 📌 7. 절대 규칙 (핵심 3개)

이 3개만 강제해도 성공이다:

---

## ✅ 1. domain은 framework 몰라야 한다

```text
org.springframework ❌
jakarta.persistence ❌
```

---

## ✅ 2. repository interface는 domain에

---

## ✅ 3. UseCase 중심 설계

---

# 📌 한 줄 결론

> 👉 **클린 아키텍처는 “설계”가 아니라 “의존성을 컴파일 단계에서 차단”해야 유지된다.**

---

원하면 다음 단계로:

👉 "멀티 모듈 Gradle 실제 설정 파일 전체 예제"
👉 "ArchUnit 실무 룰 세트 (복붙용)"
👉 "Spring Modulith vs 멀티모듈 차이"

이건 실무 적용 단계라서 이어서 자세히 보여줄게.
---
Sprint Modulith

Spring Modulith allows developers to build well-structured Spring Boot applications and guides developers in finding and working with application modules driven by the domain. It supports the verification of such modular arrangements, integration testing individual modules, observing the application’s behavior on the module level and creating documentation snippets based on the arrangement created.

Quickstart
Create a Spring Boot application on https://start.spring.io

Create a Java package arrangement that puts business modules as direct sub-packages of the application’s main package.

□ Example
└─ □ src/main/java
   ├─ □ example           <1>
   │  └─ Application.java
   ├─ □ example.inventory <2>
   │  └─ …
   └─ □ example.order     <2>
      └─ …
The application root package

Application module packages

Create an ApplicationModules model, run verifications and create documentation snippets.

class ApplicationTests {

  @Test
  void writeDocumentationSnippets() {

    var modules = ApplicationModules.of(Application.class).verify(); (1)

    new Documenter(modules) (2)
      .writeModulesAsPlantUml()
      .writeIndividualModulesAsPlantUml();
  }
}
Creates application module model and verifies its structure.

Renders Asciidoctor snippets (component diagrams, application module canvas) to target/modulith-docs.

Run integration tests for individual application modules.

□ Example
└─ □ src/test/java
   └─ □ example.order
      └─ OrderModuleIntegrationTests.java

@ApplicationModuleTests
class OrderModuleIntegrationTests {

  @Test
  void someTestMethod() { … }
}
Spring Initializr

