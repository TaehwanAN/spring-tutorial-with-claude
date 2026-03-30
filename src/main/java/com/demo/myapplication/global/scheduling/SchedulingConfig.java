package com.demo.myapplication.global.scheduling;

import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 스케줄링 설정.
 *
 * <p>Spring Boot는 {@code @EnableScheduling}이 감지되면 {@code ThreadPoolTaskScheduler}를
 * 자동으로 생성합니다. 이 클래스는 {@link ThreadPoolTaskSchedulerBuilder}를 사용해
 * {@code spring.task.scheduling.*} 프로퍼티를 반영한 커스텀 스케줄러를 명시적으로 등록합니다.
 *
 * <p>가상 스레드({@code spring.threads.virtual.enabled=true}) 활성화 시,
 * Spring Boot는 {@code ThreadPoolTaskScheduler} 대신 {@code SimpleAsyncTaskScheduler}로
 * 자동 전환합니다. 가상 스레드는 데몬 스레드이므로 스케줄링만으로 JVM을 유지하려면
 * {@code spring.main.keep-alive=true}를 함께 설정해야 합니다.
 *
 * @see org.springframework.scheduling.annotation.Scheduled
 * @see org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class SchedulingConfig {

    /**
     * 커스텀 {@code TaskScheduler} 빈.
     *
     * <p>{@link ThreadPoolTaskSchedulerBuilder}는 {@code spring.task.scheduling.*} 프로퍼티를
     * 자동으로 반영합니다.
     */
    @Bean
    ThreadPoolTaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder builder) {
        return builder.build();
    }
}
