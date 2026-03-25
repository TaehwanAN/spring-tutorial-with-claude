package com.demo.myapplication; // 만약 패키지 선언 없는 경우, 디폴트 패키지에 속함. 그러나 명시적으로 패키지를 선언해주는 것이 일반관행. 

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
	public static void main(String[] args) {
        /*Java의 표준 entry point, SpringApplication.run() 호출
        1. Spring ApplicationContext 생성 - IoC Container 생성
        2. Component Scan 수행 - Bean 등록
        3. Auto Configuration 실행 - Tomcat / MVC 설정
        4. 내장 톰캣 실행 - Embedded Tomcat start
        5. HTTP 서버 오픈 - localhost:8080
        */
		SpringApplication.run(MyApplication.class, args);

        // Customizing Spring App In a Code (Also, possible with application.properties and application.yml)
        /* SpringApplication application = new SpringApplication(MyApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args); */
	}
}