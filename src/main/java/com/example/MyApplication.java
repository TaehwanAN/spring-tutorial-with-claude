package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*@SpringBootApplication = meta-annotation
1. @SpringBootConfiguration: Spring 설정 클래스
2. @EnableAutoConfiguration: Spring Boot의 핵심 - classpath에 있는 라이브러리 기반으로 자동 설정
3. @ComponentScan: Bean 자동 등록 - 현재 패키지 기준으로 하위 패키지 스캔 */

@RestController
@SpringBootApplication
public class MyApplication {

	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	public static void main(String[] args) {
        /*Java의 표준 entry point, SpringApplication.run() 호출
        1. Spring ApplicationContext 생성 - IoC Container 생성
        2. Component Scan 수행 - Bean 등록
        3. Auto Configuration 실행 - Tomcat / MVC 설정
        4. 내장 톰캣 실행 - Embedded Tomcat start
        5. HTTP 서버 오픈 - localhost:8080
        */
		SpringApplication.run(MyApplication.class, args);
	}

}