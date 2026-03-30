설정값 주입 방법
  1. @Value: Bean에 직접 주입
  2. Environment 추상화: Spring의 환경 추상화를 통해 접근.
  3. @ConfigurationProperties: 구조화된 객체에 바인딩.

```💡 전문가의 상세 설명: @Value vs @ConfigurationProperties
@Value: 단순한 단일 값을 가져올 때 유용하지만, 설정이 많아지면 관리가 어렵고 계층 구조를 표현하기 힘듭니다.
@ConfigurationProperties: 객체 지향적으로 설정을 관리할 수 있게 해줍니다. **유연한 바인딩(Relaxed Binding)**과 **유효성 검증(Validation)**을 지원하므로, 실무에서는 이 방식을 더 권장합니다.```

우선순위 규칙
**기본값 SpringApplication.setDefaultProperties **
  < @PropertySource("") 
    **< 설정값 application.yml / application.properties**
      < RandomValuePropertySource
        **< OS Environment Variables**(히로쿠같은 플랫폼 사용시 환경변수에 해당)
          **< System.getProperties()**
            < java:comp/env, ServletContext, ServletConfig
              < SPRING_APPLICATION_JSON 같은 환경변수(하나의 환경변수로 제이슨 전달해버리는거: SPRING_APPLICATION_JSON={"server":{"port":9000},"my":{"name":"spring"}})
                **< Command Line Arguments 커맨드라인 인자**
                  **< 테스트용 properties, @SpringBootTest, @DynamicPropertySource, @TestPropertySource**
                    < $HOME/.config/spring-boot

설정값 데이터 파일 순서: Spring Boot는 설정을 읽을 때 "구체적일수록, 그리고 실행 환경에 가까울수록" 우선순위를 높게 둡니다.
Jar 내부에 패키징된 application.properties (및 YAML)
  < Jar 내부에 패키징된 프로파일별 설정 (application-{profile}.properties)
    < Jar 외부의 application.properties (및 YAML)
      < Jar 외부의 프로파일별 설정 (application-{profile}.properties)
