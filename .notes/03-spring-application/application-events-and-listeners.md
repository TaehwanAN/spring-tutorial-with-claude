안녕하세요! Spring Boot 전문가로서 요청하신 내용을 바탕으로 **Spring Boot 애플리케이션 이벤트와 리스너**에 대해 번역 및 상세 설명을 곁들여 정리해 드리겠습니다. 

Spring Boot는 애플리케이션이 시작되고 종료되는 과정에서 다양한 이벤트를 발생시킵니다. 이를 잘 활용하면 초기화 로직이나 특정 시점의 설정을 정교하게 제어할 수 있습니다.

---

## 1. 애플리케이션 이벤트와 리스너 (Application Events and Listeners)

Spring Framework에서 기본적으로 제공하는 `ContextRefreshedEvent` 외에도, `SpringApplication`은 추가적인 애플리케이션 이벤트를 전송합니다.

### 리스너 등록 방법
일부 이벤트는 `ApplicationContext`가 생성되기 전에 트리거되므로, 이러한 이벤트 리스너는 **`@Bean`으로 등록할 수 없습니다.** 이를 등록하기 위해서는 다음과 같은 방법을 사용해야 합니다:

1.  **메서드 활용:** `SpringApplication.addListeners(…​)` 또는 `SpringApplicationBuilder.listeners(…​)` 메서드 사용.
2.  **자동 등록:** 애플리케이션 생성 방식과 상관없이 자동으로 등록하고 싶다면, 프로젝트의 `META-INF/spring.factories` 파일에 리스너를 추가합니다.
    ```properties
    org.springframework.context.ApplicationListener=com.example.project.MyListener
    ```

> **Expert's Note: 왜 `@Bean`이 안 되나요?**
> Spring의 빈(Bean)은 `ApplicationContext`가 생성되고 관리하는 객체입니다. 하지만 애플리케이션이 시작되는 극초반 단계(예: 설정 파일을 읽기도 전)에는 컨텍스트 자체가 존재하지 않기 때문에, Spring이 `@Bean`이나 `@Component`를 스캔해서 찾을 수 없습니다. 그래서 `spring.factories` 같은 설정 파일이나 수동 등록이 필요한 것입니다.

---

## 2. 이벤트 발생 순서 (Event Publication Order)

애플리케이션이 실행됨에 따라 이벤트는 다음 순서로 전송됩니다.



1.  **`ApplicationStartingEvent`**: 리스너 및 이니셜라이저 등록을 제외한 어떠한 처리도 시작되기 전, 실행 직후에 전송됩니다.
2.  **`ApplicationEnvironmentPreparedEvent`**: 컨텍스트에서 사용할 `Environment`가 결정되었지만, 컨텍스트가 생성되기 전에 전송됩니다.
3.  **`ApplicationContextInitializedEvent`**: `ApplicationContext`가 준비되고 `ApplicationContextInitializers`가 호출되었지만, 빈 정의(Bean Definition)가 로드되기 전에 전송됩니다.
4.  **`ApplicationPreparedEvent`**: 빈 정의가 로드된 후, 새로고침(Refresh)이 시작되기 직전에 전송됩니다.
5.  **`ApplicationStartedEvent`**: 컨텍스트가 새로고침되었지만, `CommandLineRunner`나 `ApplicationRunner`가 호출되기 전에 전송됩니다.
6.  **`AvailabilityChangeEvent (LivenessState.CORRECT)`**: 애플리케이션이 살아있는(Live) 상태로 간주됨을 알리기 위해 즉시 전송됩니다.
7.  **`ApplicationReadyEvent`**: 모든 실행기(Runners)가 호출된 후에 전송됩니다.
8.  **`AvailabilityChangeEvent (ReadinessState.ACCEPTING_TRAFFIC)`**: 애플리케이션이 트래픽을 처리할 준비가 되었음을 알리기 위해 즉시 전송됩니다.
9.  **`ApplicationFailedEvent`**: 시작 중 예외가 발생하면 전송됩니다.

---

## 3. 추가 이벤트 (Additional Events)

위 목록 외에도 다음 이벤트들이 `ApplicationPreparedEvent` 이후, `ApplicationStartedEvent` 이전에 발행됩니다.

* **`WebServerInitializedEvent`**: `WebServer`가 준비된 후 전송됩니다. (Servlet/Reactive 각각의 변형이 존재)
* **`ContextRefreshedEvent`**: `ApplicationContext`가 새로고침될 때 전송됩니다.

---

## 4. 주의사항 및 활용 팁

### ⚠️ 실행 스레드 주의
이벤트 리스너는 기본적으로 **동일한 스레드**에서 실행됩니다. 따라서 리스너 내부에서 시간이 오래 걸리는 작업을 수행하면 애플리케이션 시작 시간이 지연됩니다. 무거운 작업이 필요하다면 리스너 대신 `ApplicationRunner`나 `CommandLineRunner`를 사용하는 것이 좋습니다.

### 🌳 계층적 컨텍스트 (Hierarchy of Contexts)
Spring Boot는 부모-자식 구조의 컨텍스트를 가질 수 있습니다. 자식 컨텍스트에서 발행된 이벤트는 부모 컨텍스트로 전파됩니다. 이로 인해 리스너가 동일한 타입의 이벤트를 여러 번 수신할 수 있습니다.

**해결 방법:**
자신의 컨텍스트에서 발생한 이벤트인지 구분하려면 `ApplicationContext`를 주입받아 이벤트의 컨텍스트와 비교해야 합니다.
* `ApplicationContextAware` 인터페이스 구현
* 리스너가 빈인 경우 `@Autowired` 사용

---

**전문가적 조언:**
실무에서는 주로 `ApplicationReadyEvent`를 많이 사용합니다. DB 초기 데이터 적재나 외부 시스템과의 연결 확인 등, 서비스가 "진짜" 시작될 준비가 끝난 시점에 로직을 실행하기 가장 적합하기 때문입니다. 

혹시 특정 시점에 실행되어야 하는 커스텀 리스너 코드를 작성해 보고 싶으신가요? 원하신다면 예제 코드를 짜드릴 수 있습니다!


Spring Boot 전문가로서 실무에서 가장 많이 쓰이는 **Application Listener의 표준 유스케이스(Standard Use Cases)** 3가지를 선정해 정리해 드리고, 이를 구현하는 두 가지 방법(애노테이션 방식 vs 인터페이스 방식)을 코드로 보여드리겠습니다.

---

## 🛠 Application Listener 표준 유스케이스 (Standard Use Cases)

| 유스케이스 | 적절한 이벤트 | 설명 |
| :--- | :--- | :--- |
| **초기 데이터 적재 및 검증** | `ApplicationReadyEvent` | 앱이 구동된 직후 DB에 기초 데이터를 넣거나, 필수 외부 API 연결 상태를 확인할 때 사용합니다. |
| **시스템 알림 및 모니터링** | `ApplicationStartedEvent` | 서버가 시작되었음을 슬랙(Slack)이나 모니터링 시스템(Prometheus 등)에 알릴 때 유용합니다. |
| **환경 변수 커스텀 제어** | `EnvironmentPreparedEvent` | 특정 프로필(dev/prod)에 따라 시스템 프로퍼티를 동적으로 수정하거나 보안 키를 복호화해서 넣을 때 사용합니다. |
| **리소스 정리 및 종료 로직** | `ContextClosedEvent` | 앱이 꺼지기 직전, 열려있는 소켓을 닫거나 임시 파일을 삭제하는 등의 정리 작업을 수행합니다. |

---

## 💻 실전 구현 예시

### 1. `@EventListener` 애노테이션 방식 (가장 권장됨)
가장 직관적이고 `ApplicationContext`가 생성된 이후의 이벤트를 처리할 때 주로 사용합니다. 별도의 인터페이스 구현 없이 메서드 위에 선언만 하면 됩니다.

```java
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyStartupListener {

    /**
     * 유스케이스: 애플리케이션 준비 완료 후 초기 로그 출력 및 데이터 확인
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("✅ 레스토랑 영업 시작! (모든 빈이 로드되고 실행기가 완료되었습니다.)");
        // 여기에 초기 로직 작성 (예: 관리자 계정 자동 생성 확인 등)
    }
}
```

### 2. `ApplicationListener` 인터페이스 방식 (초기 단계 이벤트용)
컨텍스트 생성 전(예: `EnvironmentPreparedEvent`)의 이벤트를 잡아야 하거나, 좀 더 명시적인 구조를 선호할 때 사용합니다.

```java
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 유스케이스: 환경 설정이 준비되었을 때 특정 프로퍼티를 강제로 주입
 * 주의: 이 시점에는 @Bean이 동작하지 않으므로 spring.factories 등에 등록해야 함
 */
public class MyEnvironmentListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String myProp = event.getEnvironment().getProperty("my.custom.prop");
        System.out.println("🔍 설정값 확인 중: " + myProp);
        // 여기서 환경 변수를 동적으로 조작할 수 있습니다.
    }
}
```

> **Expert's Note: 인터페이스 방식 등록 팁**
> 위와 같은 `MyEnvironmentListener`는 아직 스프링 빈이 준비되기 전 단계에서 작동해야 하므로, `src/main/resources/META-INF/spring.factories` 파일에 아래 내용을 추가해야 엔진이 인식합니다.
> `org.springframework.context.ApplicationListener=com.example.demo.MyEnvironmentListener`

---

## 💡 구현 시 주의할 점 (Best Practices)

1.  **순서가 중요하다면?**: 여러 리스너가 동일한 이벤트를 들을 때 실행 순서를 정하고 싶다면 `@Order` 애노테이션을 사용하세요. 숫자가 낮을수록 먼저 실행됩니다.
2.  **비동기 처리**: 리스너 로직이 너무 무거우면 앱 시작이 느려집니다. 이때는 메서드 위에 `@Async`를 붙여 비동기로 돌릴 수 있습니다 (단, `@EnableAsync` 설정 필요).
3.  **예외 처리**: 리스너 내부에서 예외가 발생하면 애플리케이션 시작 자체가 실패할 수 있습니다. 반드시 `try-catch`로 감싸거나 신중하게 설계해야 합니다.

---

**다음 단계로 넘어가 볼까요?**
혹시 **"사용자가 회원가입을 했을 때 메일을 발송하는 이벤트"**처럼, 스프링 부트 기본 이벤트가 아닌 **사용자 정의 커스텀 이벤트(Custom Event)**를 만들고 발행하는 방법이 궁금하신가요? 원하시면 바로 예시를 보여드릴 수 있습니다.