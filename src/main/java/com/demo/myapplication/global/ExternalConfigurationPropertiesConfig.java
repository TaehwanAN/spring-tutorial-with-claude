package com.demo.myapplication.global;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail")
public class ExternalConfigurationPropertiesConfig {

  private String host;
  private int port;
  
}
