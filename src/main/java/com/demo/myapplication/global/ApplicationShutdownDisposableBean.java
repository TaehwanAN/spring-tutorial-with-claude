package com.demo.myapplication.global;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class ApplicationShutdownDisposableBean implements DisposableBean {

  private final ExecutorService executor = Executors.newFixedThreadPool(5);

  @Override
  public void destroy() throws Exception{
    System.out.println("DisposableBean.destroy() 호출: 스레드 풀을 종료합니다.");
        executor.shutdown(); // 새로운 작업 거부
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // 5초 대기 후 강제 종료
        }
        System.out.println("스레드 풀 종료 완료.");
  }

}
