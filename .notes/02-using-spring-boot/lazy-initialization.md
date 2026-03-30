🔹 Lazy Initialization

SpringApplication은 애플리케이션을 지연 초기화(lazy initialization) 방식으로 실행할 수 있게 해준다.

지연 초기화가 활성화되면,
Bean들은 애플리케이션 시작 시점이 아니라 실제로 필요한 시점에 생성된다.

그 결과,
지연 초기화를 사용하면 애플리케이션 시작 시간을 줄일 수 있다.

웹 애플리케이션의 경우,
지연 초기화를 활성화하면 많은 웹 관련 Bean들이
HTTP 요청이 들어오기 전까지 초기화되지 않는다.

⚠️ 단점

지연 초기화의 단점은 다음과 같다.

문제 발견 지연
설정이 잘못된 Bean이 있어도
애플리케이션 시작 시점에는 에러가 발생하지 않음
실제로 해당 Bean이 생성되는 시점에서 에러 발생
메모리 관리 주의 필요
JVM은 초기 Bean뿐 아니라 전체 Bean을 수용할 수 있어야 함
결국 모든 Bean은 언젠가 생성됨
그래서 기본적으로는 비활성화(default = false) 상태
사용 전에
👉 JVM Heap Size 튜닝이 필요함

🔹 설정 방법
1. 코드 기반 설정
new SpringApplicationBuilder(App.class)
    .lazyInitialization(true)
    .run(args);

또는

SpringApplication app = new SpringApplication(App.class);
app.setLazyInitialization(true);
2. 설정 파일 (가장 일반적)
spring:
  main:
    lazy-initialization: true
✅ 📌 핵심 개념 정리
🔥 Lazy vs Eager 초기화
구분	Eager (기본)	Lazy
Bean 생성 시점	서버 시작 시	필요할 때
장점	안정성, 빠른 오류 발견	빠른 startup
단점	느린 startup	런타임 에러 가능
✅ 📌 동작 흐름 (중요)
🔹 기본 (Eager)
서버 시작 →
모든 Bean 생성 →
문제 있으면 바로 실패
🔹 Lazy
서버 시작 →
Bean 생성 안함 →
요청 발생 →
필요한 Bean 생성 →
문제 있으면 그때 실패
✅ 📌 언제 사용해야 하나?
✔ 추천 상황
개발 환경 (빠른 startup 필요)
마이크로서비스 (가벼운 시작)
Bean 수가 매우 많은 경우
❌ 비추천 상황
운영 환경 (특히 안정성이 중요한 경우)
초기 실행 시 문제를 반드시 검증해야 하는 시스템
금융/결제 시스템
✅ 💡 실무 팁 (중요)
1. 전체 Lazy는 위험
spring.main.lazy-initialization=true

👉 전체 Bean 적용 → 예상 못한 장애 발생 가능

2. 부분 Lazy 권장
@Lazy
@Service
public class HeavyService { }

👉 특정 Bean만 Lazy 처리

3. 운영 환경 전략
기본: eager
일부 무거운 Bean만 lazy