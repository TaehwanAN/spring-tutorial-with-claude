package com.demo.myapplication.global.monitoring.health.checker;

import org.springframework.stereotype.Component;

@Component
public class SimpleHealthChecker {

    public boolean isHealthy() {
        // 실제로는 DB ping, Redis ping, 외부 API 호출 등
        return true;
    }
}