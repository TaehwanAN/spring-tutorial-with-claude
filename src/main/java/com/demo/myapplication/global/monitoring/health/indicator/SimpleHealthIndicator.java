package com.demo.myapplication.global.monitoring.health.indicator;

import com.demo.myapplication.global.monitoring.health.checker.SimpleHealthChecker;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("simpleHealthIndicator")
public class SimpleHealthIndicator implements HealthIndicator {

    private final SimpleHealthChecker checker;

    public SimpleHealthIndicator(SimpleHealthChecker checker) {
        this.checker = checker;
    }

    @Override
    public Health health() {
        boolean result = checker.isHealthy();

        if (result) {
            return Health.up()
                    .withDetail("simple-check", "OK")
                    .build();
        }

        return Health.down()
                .withDetail("simple-check", "FAILED")
                .build();
    }
}