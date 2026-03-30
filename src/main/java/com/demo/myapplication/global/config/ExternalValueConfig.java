package com.demo.myapplication.global.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExternalValueConfig {

  // 시작 시점에 application.yml 에서 주입된 난수를 가져온다.
  @Value("${my.secret}")
  private String mySecret;

  @Value("${my.long}")
  public Long myLong;

  @Value("${my.uuid}")
  private UUID myUuid;
}
