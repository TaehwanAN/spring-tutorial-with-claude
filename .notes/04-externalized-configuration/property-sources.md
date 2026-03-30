`@PropertySource` 어노테이션을 사용할 때 가장 많이 겪는 실수 중 하나가 바로 **"분명히 설정했는데 왜 적용이 안 되지?"** 하는 상황입니다.

Spring Boot의 구동 프로세스 단계와 함께 구체적인 예시를 통해 설명해 드릴게요.

---

## 1. 실패하는 예시: 로그 레벨 설정

상황: `custom-logging.properties`라는 별도의 파일을 만들고, 여기서 전체 로그 레벨을 `DEBUG`로 설정하려고 합니다.

### 설정 파일 (`src/main/resources/custom-logging.properties`)
```properties
logging.level.root=DEBUG
spring.main.banner-mode=off
```

### Java 설정 클래스
```java
@Configuration
@PropertySource("classpath:custom-logging.properties")
public class MyLoggingConfig {
    // 이 시점은 너무 늦습니다!
}
```

### 결과
애플리케이션을 실행해도 **로그는 여전히 `INFO` 레벨**로 출력되고, **배너(Banner)도 그대로 화면에 나타납니다.**

---

## 2. 왜 안되는 걸까? (원인 분석)

이유는 **Spring Boot의 시작 순서** 때문입니다. Spring Boot가 실행될 때 내부적으로 발생하는 순서를 단순화하면 다음과 같습니다.



1.  **애플리케이션 시작 (Bootstrap):** `SpringApplication.run()` 호출.
2.  **환경 준비 (Environment Prepared):** 이 단계에서 `application.properties`나 YAML 파일, 환경 변수 등을 읽어들입니다.
3.  **로깅 시스템 초기화:** 바로 이 시점에 `logging.*` 설정을 읽어 로그 시스템을 가동합니다. **(아직 `@Configuration` 클래스는 읽지도 않은 상태입니다!)**
4.  **컨텍스트 리프레시 (Context Refresh):** 여기서 비로소 `@Configuration` 클래스를 스캔하고 `@PropertySource`를 확인하여 `Environment`에 추가합니다.
5.  **완료:** 모든 빈(Bean)이 생성됩니다.

> **결론:** 로깅 시스템은 이미 **3번 단계**에서 초기화가 끝났는데, 우리가 추가한 설정값은 **4번 단계**에 이르러서야 나타나기 때문에 로깅 시스템이 그 값을 알 턱이 없는 것입니다. `spring.main.*` 설정(배너 표시 여부 등)도 마찬가지로 아주 극초반에 결정되는 값들이라 무시됩니다.

---

## 3. 어떻게 해결해야 할까?

이러한 "매우 이른(Early)" 설정들을 적용하려면 `@PropertySource` 대신 다음 방법 중 하나를 사용해야 합니다.

### 방법 1: 표준 설정 파일 사용 (권장)
`application.properties` 혹은 `application-{profile}.properties`에 넣으면 됩니다. 이 파일들은 2번 단계(Environment Prepared)에서 읽히기 때문에 로깅 설정이 정상 적용됩니다.

### 방법 2: 실행 시 파라미터 전달
```bash
java -jar app.jar --logging.level.root=DEBUG
```

### 방법 3: 프로그래밍 방식 (꼭 외부 파일을 써야 한다면)
`main` 메소드에서 직접 기본 속성을 설정해 줄 수 있습니다.

```java
public static void main(String[] args) {
    SpringApplication app = new SpringApplication(MyApplication.class);
    
    // 프로그래밍 방식으로 기본값 지정 (가장 빠른 단계에서 적용)
    Properties properties = new Properties();
    properties.put("logging.level.root", "DEBUG");
    app.setDefaultProperties(properties);
    
    app.run(args);
}
```

---

### 요약
* `@PropertySource`는 **Spring Bean이 생성될 준비를 할 때** 읽힙니다.
* `logging.*`, `spring.main.*`, `spring.profiles.*` 등은 **Spring Bean이 준비되기 훨씬 전**에 필요합니다.
* 따라서 초기 인프라 설정은 반드시 **`application.properties`**나 **환경 변수**를 사용해야 합니다.

**다음 단계로 무엇을 도와드릴까요?**
* "실무에서 `application.properties`를 환경별로 분리하는 구체적인 방법이 궁금해"
* "YAML 파일에서 계층 구조를 사용해 로그 설정을 하는 법 알려줘"