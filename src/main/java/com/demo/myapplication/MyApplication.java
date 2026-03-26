package com.demo.myapplication; // 만약 패키지 선언 없는 경우, 디폴트 패키지에 속함. 그러나 명시적으로 패키지를 선언해주는 것이 일반관행. 

import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.demo.myapplication.global.configuration.listener.application.BeforeStartApplicationListeners;

/*@SpringBootApplication = meta-annotation
1. @SpringBootConfiguration: Spring 설정 클래스
2. @EnableAutoConfiguration: Spring Boot의 핵심 - classpath에 있는 라이브러리 기반으로 자동 설정
3. @ComponentScan: Bean 자동 등록 - 현재 패키지 기준으로 하위 패키지 스캔 */

/*
특정 configuration은 배제하는 방법
@SpringBootApplication(
    exclude = { DataSourceAutoConfiguration.class }
)
 */

@SpringBootApplication // 루트 패키지(<groupId>com.demo.myapplication</groupId>)에 속해주어야, @ComponentScan 과 EnableAutoConfiguration의 기본 스캔 범위가 그 하위 패키지들로 정의됨. 스캔 대상 com.demo.myapplication.*
public class MyApplication {

    // 종료 코드로 42를 반환하는 예시
    /*
    보통 웹 서버보다는 배치(Batch) 애플리케이션이나 CLI 도구에서 주로 사용합니다.
    이렇게 숫자를 지정해두면, 쉘 스크립트나 CI/CD 파이프라인(Jenkins, GitHub Actions 등)에서 $? 변수를 통해 이전 단계가 왜 실패했는지 판단하고 후속 처리를 할 수 있습니다
    단순히 빈으로 등록하는 것 외에, 커스텀 예외에 이 인터페이스를 구현하면 매우 깔끔한 에러 처리가 가능합니다.
    public class DataNotFoundException extends RuntimeException implements ExitCodeGenerator {
    @Override
    public int getExitCode() {
            return 80; // 데이터가 없을 때의 특정 종료 코드
        }
    }
    */
    @Bean
    public ExitCodeGenerator exitCodeGenerator(){
        return () -> 42;
    }

	public static void main(String[] args) {
        /*Java의 표준 entry point, SpringApplication.run() 호출
        1. Spring ApplicationContext 생성 - IoC Container 생성
        2. Component Scan 수행 - Bean 등록
        3. Auto Configuration 실행 - Tomcat / MVC 설정
        4. 내장 톰캣 실행 - Embedded Tomcat start
        5. HTTP 서버 오픈 - localhost:8080
        */
		// SpringApplication.run(MyApplication.class, args);

        // Customizing Spring App In a Code (Also, possible with application.properties and application.yml)
        /* SpringApplication application = new SpringApplication(MyApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args); */

        SpringApplication app = new SpringApplication(MyApplication.class);

        // Application Event Listener 사용을 위한 SpringBoot 로딩
        app.addListeners(
            new BeforeStartApplicationListeners.StartingEventListener()
            , new BeforeStartApplicationListeners.EnvironmentPreparedEventListener()
            , new BeforeStartApplicationListeners.ContextInitializedEventListener()
            , new BeforeStartApplicationListeners.ApplicationPreparedEventListener()
        );

        // Web Environment Type Setting
        app.setWebApplicationType(WebApplicationType.SERVLET);
        /* Spring MVC 발견시 Default. WebFlux와 MVC 동시발견시 Default. 
        작동 방식: Thread-per-request 모델. 요청마다 스레드를 할당하며, DB 작업 등이 끝날 때까지 스레드가 기다립니다(Blocking).
        Use Case (언제 쓰나요?)
        전형적인 기업용 시스템 (ERP, 그룹웨어): 트래픽이 예측 가능하고 안정적인 처리가 중요할 때.
        표준 웹 애플리케이션: 쇼핑몰, 게시판, 관리자 페이지 등 대부분의 일반적인 웹 서비스.
        풍부한 라이브러리 지원이 필요할 때: JPA(Hibernate) 등 기존의 수많은 블로킹 방식 라이브러리를 그대로 사용하고 싶을 때.
        */
        
        // app.setWebApplicationType(WebApplicationType.REACTIVE);
        /* Spring WebFlux 발견시 Default. 
        작동 방식: Event-loop 모델. 적은 수의 스레드로 수만 개의 동시 접속을 효율적으로 처리합니다(Non-blocking).
        Use Case (언제 쓰나요?)
        고동시성(High Concurrency) 환경: 실시간 채팅, 주식 시황 중계, 실시간 알림 시스템.
        API 게이트웨이(Spring Cloud Gateway): 수많은 마이크로서비스로 요청을 전달만 하고 응답을 기다려야 하는 중계 역할.
        스트리밍 서비스: 대용량 데이터를 조금씩 계속 내려보내야 하는 넷플릭스 같은 스트리밍 API.
        */

        // app.setWebApplicationType(WebApplicationType.NONE);
        /* 
        작동 방식: 서버(Tomcat/Netty)를 실행하지 않고, 컨텍스트 내부의 빈(Bean)들만 초기화하여 로직을 실행합니다.
        Use Case (언제 쓰나요?)
        배치(Batch) 애플리케이션: 매일 밤 12시에 전날 매출 데이터를 정산하여 DB에 저장하는 작업.
        CLI(Command Line Interface) 도구: 터미널에서 명령어를 입력하면 특정 작업을 수행하고 종료되는 프로그램.
        단순 스케줄러: 주기적으로 특정 파일을 삭제하거나 서버 상태를 체크하는 백그라운드 프로세스.
         */

        app.run(args);
	}
}