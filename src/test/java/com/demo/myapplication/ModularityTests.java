package com.demo.myapplication;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Spring Modulith 모듈 구조 검증 테스트.
 *
 * <p>이 테스트가 실패하면 모듈 간 의존성 규칙이 위반된 것이다.
 * 예: CLOSED 모듈의 내부 패키지를 다른 모듈에서 직접 참조.</p>
 */
class ModularityTests {

    @Test
    void verifyModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(MyApplication.class);
        modules.verify();
    }

    @Test
    void printModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(MyApplication.class);
        modules.forEach(System.out::println);
    }
}
