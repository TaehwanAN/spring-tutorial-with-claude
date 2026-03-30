package com.demo.myapplication.global.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code @Scheduled} 트리거 3종 예제.
 *
 * <p>학습 목적으로 {@code fixedRate}, {@code fixedDelay}, {@code cron} 세 가지 방식을 보여줍니다.
 * 실제 프로덕션에서는 비즈니스 도메인에 맞는 적절한 패키지에 배치하세요.
 *
 * <h3>트리거 방식 비교</h3>
 * <ul>
 *   <li><b>fixedRate</b> — 이전 실행 시작 시점 기준으로 일정 간격 반복.
 *       작업이 오래 걸리면 다음 실행이 겹칠 수 있음.</li>
 *   <li><b>fixedDelay</b> — 이전 실행 완료 시점 기준으로 일정 대기 후 반복.
 *       작업 완료를 보장한 뒤 다음 실행을 시작.</li>
 *   <li><b>cron</b> — Unix cron 표현식으로 정확한 시각 지정.
 *       {@code "초 분 시 일 월 요일"} 형식.</li>
 * </ul>
 */
@Component
public class ExampleScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(ExampleScheduledTask.class);

    /**
     * fixedRate: 이전 실행 시작으로부터 30초마다 실행.
     *
     * <p>짧은 주기의 상태 점검, 폴링 작업에 적합합니다.
     * initialDelay로 애플리케이션 기동 직후 첫 실행을 늦출 수 있습니다.
     */
    @Scheduled(fixedRate = 30_000, initialDelay = 10_000)
    public void fixedRateTask() {
        log.debug("[fixedRate] 30초 간격 태스크 실행 — thread: {}", Thread.currentThread().getName());
    }

    /**
     * fixedDelay: 이전 실행 완료 후 60초 대기 뒤 실행.
     *
     * <p>외부 API 폴링처럼 직전 작업 완료를 보장해야 하는 경우에 적합합니다.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    public void fixedDelayTask() {
        log.debug("[fixedDelay] 완료 후 60초 대기 태스크 실행 — thread: {}", Thread.currentThread().getName());
    }

    /**
     * cron: 매분 0초에 실행 (= 1분마다).
     *
     * <p>cron 표현식: {@code "초 분 시 일 월 요일"}
     * <ul>
     *   <li>{@code "0 * * * * *"} — 매분 0초 (1분마다)</li>
     *   <li>{@code "0 0 * * * *"} — 매시 정각</li>
     *   <li>{@code "0 0 9 * * MON-FRI"} — 평일 오전 9시</li>
     * </ul>
     *
     * <p>Spring은 6자리 cron 표현식을 사용합니다 (초 필드 포함).
     * Unix cron의 5자리와 다르므로 주의하세요.
     */
    @Scheduled(cron = "0 * * * * *")
    public void cronTask() {
        log.debug("[cron] 매분 0초 태스크 실행 — thread: {}", Thread.currentThread().getName());
    }
}
