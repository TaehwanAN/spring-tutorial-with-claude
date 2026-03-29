package com.demo.myapplication.global.listener.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

// 주의: 이 클래스는 @Component를 붙이지 않습니다. main 메서드에서 수동 등록합니다.
public class BeforeStartApplicationListeners {

  /*
    ─────────────────────────────────────────────────────────────────────
    [로깅 초기화 타이밍 - 중요 학습 포인트]
    ─────────────────────────────────────────────────────────────────────
    Spring Boot 로깅 시스템은 ApplicationEnvironmentPreparedEvent 시점에 초기화된다.
    logback-spring.xml 의 <springProfile> 태그는 Spring Environment 가 존재해야 동작하므로
    역시 ApplicationEnvironmentPreparedEvent 이후에만 프로파일 분기가 활성화된다.

    따라서 이 클래스의 리스너들은:
      [1] ApplicationStartingEvent          → Logback 기본 설정으로 출력 (프로파일 분기 없음)
      [2] ApplicationEnvironmentPreparedEvent → 이 이벤트가 처리되는 중에 로깅 시스템 초기화
      [3] ApplicationContextInitializedEvent → 로깅 시스템 완전 초기화 완료
      [4] ApplicationPreparedEvent           → <springProfile> 분기 완전 활성화

    SLF4J + Logback 자체는 [1] 시점부터 동작하지만,
    logback-spring.xml 의 프로파일별 Appender/Level 설정은 [2] 이후부터 적용됨.
    ─────────────────────────────────────────────────────────────────────
  */

  // 1. ApplicationStartingEvent: 이 시점에는 Spring의 ApplicationContext나 환경(Environment)이 아예 존재하지 않습니다. 주로 애플리케이션 외부의 설정이나 로깅 시스템(예: Logback)이 초기화되기 전에 아주 기초적인 시스템 프로퍼티를 강제로 설정해야 할 때 사용합니다.
  public static class StartingEventListener implements ApplicationListener<ApplicationStartingEvent>{
    // 주의: 이 시점은 Spring 로깅 시스템 초기화 전이므로 Logback 기본 설정으로 출력됨.
    // logback-spring.xml 의 <springProfile> 분기는 아직 비활성.
    private static final Logger log = LoggerFactory.getLogger(StartingEventListener.class);

    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationStartingEvent event){
      log.info("##### [1] ApplicationStartingEvent: App Started (No Context exists) #####");
      log.debug("event detail: {}", event);
    }
  }

  // 2. ApplicationEnvironmentPreparedEvent: Environment 객체가 생성되었습니다. 여기서 application.yml 등의 프로퍼티를 읽거나 수정할 수 있습니다. 암호화된 프로퍼티를 복호화해서 다시 Environment에 넣는 작업 등을 하기에 적합합니다.
  public static class EnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>{
    // 이 이벤트 처리 중에 Spring Boot 로깅 시스템이 초기화됨.
    // logback-spring.xml 로드 및 <springProperty> 바인딩이 이 시점 이후에 완성됨.
    private static final Logger log = LoggerFactory.getLogger(EnvironmentPreparedEventListener.class);

    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationEnvironmentPreparedEvent event){
      log.info("##### [2] ApplicationEnvironmentPreparedEvent: Environment Preparation Done #####");
      log.debug("active profiles: {}", (Object) event.getEnvironment().getActiveProfiles());
    }
  }

  // 3. ApplicationContextInitializedEvent: 빈(Bean) 팩토리가 생성되었지만 빈 정의는 아직 없습니다. ApplicationContextInitializer들이 실행된 직후이므로, 컨텍스트 자체에 대한 매우 낮은 수준의 조작이 필요할 때 씁니다. (실무 사용 빈도 낮음)
  public static class ContextInitializedEventListener implements ApplicationListener<ApplicationContextInitializedEvent>{
    private static final Logger log = LoggerFactory.getLogger(ContextInitializedEventListener.class);

    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationContextInitializedEvent event){
      log.info("##### [3] ApplicationContextInitializedEvent: Context Initialized (Before Loading Beans) #####");
    }
  }

  // 4. ApplicationPreparedEvent: 빈 정의(Bean Definition)가 로드되었습니다. 아직 빈 객체들이 생성(인스턴스화)되거나 의존성 주입(DI)이 일어나기 전입니다.
  public static class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent>{
    private static final Logger log = LoggerFactory.getLogger(ApplicationPreparedEventListener.class);

    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationPreparedEvent event){
      log.info("##### [4] ApplicationPreparedEvent: Bean Definition Loaded #####");
    }
  }

}
