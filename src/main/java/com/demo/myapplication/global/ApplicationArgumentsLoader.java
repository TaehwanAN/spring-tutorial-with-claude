package com.demo.myapplication.global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class ApplicationArgumentsLoader {

  // LoggerFactory.getLogger(클래스명.class)
  // → SLF4J API 호출 → 런타임에 Logback 구현체가 실제 처리
  // static final 로 선언해 클래스당 하나의 Logger 인스턴스만 생성 (관례)
  private static final Logger log = LoggerFactory.getLogger(ApplicationArgumentsLoader.class);

  public ApplicationArgumentsLoader(ApplicationArguments appArgs){
    // 파라미터화된 로깅: 문자열 연결(+) 대신 {} 플레이스홀더 사용
    // → log level 이 비활성화된 경우 toString() 호출 자체를 건너뛰어 성능상 유리
    log.info("===== Application Arguments =====");
    log.info("raw args     : {}", appArgs.toString());
    log.debug("nonOptionArgs: {}", appArgs.getNonOptionArgs());
    log.debug("optionNames  : {}", appArgs.getOptionNames());
  }

}
