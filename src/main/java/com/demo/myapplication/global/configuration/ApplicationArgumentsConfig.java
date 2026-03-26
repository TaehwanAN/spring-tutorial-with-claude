package com.demo.myapplication.global.configuration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class ApplicationArgumentsConfig {

  public ApplicationArgumentsConfig(ApplicationArguments appArgs){
    System.err.println("##### Application's Arguments ######");
    // ApplicationArguments: [str]
    System.err.println(appArgs.toString()); 
    System.err.println(appArgs.getNonOptionArgs());
    System.err.println(appArgs.getOptionNames());

  }

}
