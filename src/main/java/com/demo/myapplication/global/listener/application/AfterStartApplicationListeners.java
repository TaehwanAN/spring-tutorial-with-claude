package com.demo.myapplication.global.listener.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/*
ApplicationFailedEvent: 시작 중 에러가 발생했을 때 호출됩니다. 실패 원인을 외부 모니터링 시스템에 전송하거나, 리소스를 우아하게 종료(Graceful Shutdown)하는 데 사용합니다. */


// Bean이 생성된 이후의 이벤트만 수신 (@EventListener 사용)
// 실무에서는 대부분 빈(Bean)이 생성된 이후의 상태(Started, Ready, Availability)에 관심이 많습니다. 이 경우는 익숙한 어노테이션 방식으로 깔끔하게 구현할 수 있습니다.
@Component
public class AfterStartApplicationListeners {

  // @Component 빈으로 등록되므로 Spring 컨텍스트가 완성된 이후에 동작.
  // 이 시점에는 logback-spring.xml 의 <springProfile> 설정이 완전히 활성화되어 있음.
  private static final Logger log = LoggerFactory.getLogger(AfterStartApplicationListeners.class);

  // 5. ApplicationStartedEvent: (중요) 모든 빈이 생성되고 의존성 주입이 완료되었습니다. 애플리케이션이 사실상 구동되었지만, 웹 서버가 트래픽을 받기 직전입니다. 초기 데이터베이스 세팅이나 필수 캐시 로딩을 시작하기 좋습니다.
  @EventListener
  public void onApplicationStarted(ApplicationStartedEvent event) {
    log.info("##### [5] ApplicationStartedEvent: App Started #####");
    log.debug("event detail: {}", event);
  }

  // 6. AvailabilityChangeEvent (LivenessState.CORRECT): 쿠버네티스(Kubernetes) 환경에서 매우 중요합니다. 애플리케이션의 내부 상태가 '정상적으로 살아있음'을 외부(Liveness Probe)에 알립니다.
  @EventListener
  public void onLivenessChange(AvailabilityChangeEvent<LivenessState> event) {
    log.info("##### [6] LivenessState Changed: {} #####", event.getState());
    log.debug("event detail: {}", event);
  }

  // 7. ApplicationReadyEvent: (중요) CommandLineRunner나 ApplicationRunner까지 모두 실행 완료된 완벽한 준비 상태입니다. 사용자의 요청을 처리할 수 있는 상태이며, 배포 완료 알림(Slack, Email 등)을 보내거나 본격적인 캐시 워밍업(Cache Warm-up)을 하기에 가장 좋은 시점입니다.
  @EventListener
  public void onApplicationReady(ApplicationReadyEvent event) {
    log.info("##### [7] ApplicationReadyEvent: Ready to serve traffic #####");
    log.debug("event detail: {}", event);
  }

  // 8. AvailabilityChangeEvent (ReadinessState.ACCEPTING_TRAFFIC): 쿠버네티스의 Readiness Probe와 연동되어, 이제 라우터나 로드밸런서로부터 트래픽을 받아도 된다고 알립니다.
  @EventListener
  public void onReadinessChange(AvailabilityChangeEvent<ReadinessState> event) {
    log.info("##### [8] ReadinessState Changed: {} #####", event.getState());
    log.debug("event detail: {}", event);
  }
}
