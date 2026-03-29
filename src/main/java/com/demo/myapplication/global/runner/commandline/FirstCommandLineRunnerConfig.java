package com.demo.myapplication.global.runner.commandline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Runner의 순서가 지정되어야 하는 경우.
public class FirstCommandLineRunnerConfig implements CommandLineRunner {
  // 이 구조는 애플리케이션이 가동된 후, 외부 트래픽을 받기 시작하기 전에 수행해야 하는 작업에 매우 적합합니다.

  private static final Logger log = LoggerFactory.getLogger(FirstCommandLineRunnerConfig.class);

  @Override
  public void run(String... args){
    /*
      [MDC와 OpenTelemetry 자동 주입 이해]

      HTTP 요청 처리 중 (자동 주입):
        Spring MVC 가 요청을 받으면 Observation 을 생성하고,
        micrometer-tracing-bridge-otel 이 OTel SDK 를 통해 Span 을 시작한다.
        이때 MDCScopeDecorator 가 traceId / spanId 를 현재 스레드 MDC 에 자동 주입.
        → 로그에 실제 OTel Trace ID(128-bit hex) 가 자동으로 포함됨.

      CommandLineRunner 등 HTTP 외 컨텍스트 (수동 주입):
        Span 이 자동 생성되지 않으므로 MDC.put() 으로 직접 컨텍스트를 설정해야 함.
        실제 분산 추적과 연결하려면 ObservationRegistry 로 Observation 을 직접 생성해야 하나,
        스타트업 Runner 에서는 아래처럼 식별 가능한 값을 직접 넣는 것으로 충분하다.

      MDC 사용 시 주의사항:
        스레드 풀 환경에서 스레드가 재사용되므로 작업 완료 후 반드시 MDC.clear() 호출.
        try-finally 패턴으로 예외 발생 시에도 정리를 보장한다.
    */
    MDC.put("traceId", "startup-runner-first");
    try {
      log.info("===== First Command Line Runner =====");
      log.debug("전달된 args 개수: {}", args.length);
    } finally {
      MDC.clear();
    }
  }
  /*
  ##### [6] LivenessState Changed: CORRECT
  org.springframework.boot.availability.AvailabilityChangeEvent[source=org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@8e192e4, started on Wed Mar 25 22:48:40 CST 2026]
  ##### Command Line Runner #####
  2026-03-25T22:48:41.881-06:00  INFO 42288 --- [  restartedMain] .ConditionEvaluationDeltaLoggingListener : Condition evaluation unchanged
  (##### [7] ApplicationReadyEvent Received
  */
}
