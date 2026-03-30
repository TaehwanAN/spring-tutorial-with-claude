package com.demo.myapplication.global.async;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 태스크 실행기 설정.
 *
 * <p>Spring Boot는 {@code AsyncTaskExecutor} 빈이 없으면 자동으로 {@code ThreadPoolTaskExecutor}를
 * 생성합니다. 이 클래스는 그 대신 {@code applicationTaskExecutor}라는 이름으로 직접 빈을 등록하여,
 * Spring MVC async, JPA bootstrap, {@code @Async} 처리 등이 모두 이 동일한 실행기를 공유하도록 합니다.
 *
 * <p>빈 이름 {@code "applicationTaskExecutor"}는 Spring 인프라 통합의 핵심입니다:
 * <ul>
 *   <li>Spring MVC — {@code Callable} 리턴값 비동기 처리</li>
 *   <li>JPA — 백그라운드 부트스트랩 실행기</li>
 *   <li>ApplicationContext 백그라운드 초기화</li>
 * </ul>
 *
 * <p>스레드 풀 크기 등 세부 설정은 {@code spring.task.execution.*} 프로퍼티로 제어합니다.
 *
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class TaskExecutorConfig {

    /**
     * 커스텀 {@code AsyncTaskExecutor} 빈.
     *
     * <p>{@link ThreadPoolTaskExecutorBuilder}는 {@code spring.task.execution.*} 프로퍼티를
     * 자동으로 반영합니다. 빌더를 그대로 사용하면 application.yml의 설정값이 즉시 적용됩니다.
     */
    @Bean("applicationTaskExecutor")
    ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
        return builder.build();
    }
}
