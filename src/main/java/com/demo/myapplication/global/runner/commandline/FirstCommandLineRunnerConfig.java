package com.demo.myapplication.global.runner.commandline;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Runner의 순서가 지정되어야 하는 경우.
public class FirstCommandLineRunnerConfig implements CommandLineRunner {
  // 이 구조는 애플리케이션이 가동된 후, 외부 트래픽을 받기 시작하기 전에 수행해야 하는 작업에 매우 적합합니다.
  @Override
  public void run(String... args){
    System.err.println("##### First Command Line Runner #####");
  }
  /*
  ##### [6] LivenessState Changed: CORRECT
  org.springframework.boot.availability.AvailabilityChangeEvent[source=org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@8e192e4, started on Wed Mar 25 22:48:40 CST 2026]
  ##### Command Line Runner #####
  2026-03-25T22:48:41.881-06:00  INFO 42288 --- [  restartedMain] .ConditionEvaluationDeltaLoggingListener : Condition evaluation unchanged        
  (##### [7] ApplicationReadyEvent Received
  */
}
