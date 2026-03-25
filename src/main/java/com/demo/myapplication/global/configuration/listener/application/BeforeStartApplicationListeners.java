package com.demo.myapplication.global.configuration.listener.application;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

// 주의: 이 클래스는 @Component를 붙이지 않습니다. main 메서드에서 수동 등록합니다.
public class BeforeStartApplicationListeners {

  // 1. ApplicationStartingEvent: 이 시점에는 Spring의 ApplicationContext나 환경(Environment)이 아예 존재하지 않습니다. 주로 애플리케이션 외부의 설정이나 로깅 시스템(예: Logback)이 초기화되기 전에 아주 기초적인 시스템 프로퍼티를 강제로 설정해야 할 때 사용합니다.
  public static class StartingEventListener implements ApplicationListener<ApplicationStartingEvent>{
    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationStartingEvent event){
      System.out.println("##### [1] ApplicationStartingEvent: App Started (No Context exists) #####");
      System.out.println(event);
    }
  }
  
  // 2. ApplicationEnvironmentPreparedEvent: Environment 객체가 생성되었습니다. 여기서 application.yml 등의 프로퍼티를 읽거나 수정할 수 있습니다. 암호화된 프로퍼티를 복호화해서 다시 Environment에 넣는 작업 등을 하기에 적합합니다.
  public static class EnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>{
    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationEnvironmentPreparedEvent event){
      System.out.println("##### [2] ApplicationEnvironmentPreparedEvent: Environment Preaparation Done #####");
      System.out.println(event);
    }
  }

  // 3. ApplicationContextInitializedEvent: 빈(Bean) 팩토리가 생성되었지만 빈 정의는 아직 없습니다. ApplicationContextInitializer들이 실행된 직후이므로, 컨텍스트 자체에 대한 매우 낮은 수준의 조작이 필요할 때 씁니다. (실무 사용 빈도 낮음)
  public static class ContextInitializedEventListener implements ApplicationListener<ApplicationContextInitializedEvent>{
    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationContextInitializedEvent event){
      System.out.println("##### [3] ApplicationContextInitializedEvent: Context Initialized(Before Loading Beans) #####");
      System.out.println(event);
    }
  }

  // 4. ApplicationPreparedEvent: 빈 정의(Bean Definition)가 로드되었습니다. 아직 빈 객체들이 생성(인스턴스화)되거나 의존성 주입(DI)이 일어나기 전입니다.
  public static class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent>{
    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ApplicationPreparedEvent event){
      System.out.println("##### [4] ApplicationPreparedEvent: Bean Definition Loaded #####");
      System.out.println(event);
    }
  }

}