package com.demo.myapplication;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit을 사용한 Clean Architecture 의존성 규칙 검증.
 *
 * <p>이 테스트들이 실패하면 Clean Architecture 레이어 규칙이 위반된 것이다.</p>
 */
class ArchitectureTests {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.demo.myapplication");
    }

    @Test
    @DisplayName("도메인 레이어는 Spring Framework에 의존하지 않는다")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(classes);
    }

    @Test
    @DisplayName("도메인 레이어는 Jakarta Persistence에 의존하지 않는다")
    void domainShouldNotDependOnJakartaPersistence() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .check(classes);
    }

    @Test
    @DisplayName("Inbound 어댑터는 Outbound 어댑터에 의존하지 않는다")
    void inboundAdapterShouldNotDependOnOutbound() {
        noClasses()
                .that().resideInAPackage("..adapter.in..")
                .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
                .check(classes);
    }

    @Test
    @DisplayName("Outbound 어댑터는 Inbound 어댑터에 의존하지 않는다")
    void outboundAdapterShouldNotDependOnInbound() {
        noClasses()
                .that().resideInAPackage("..adapter.out..")
                .should().dependOnClassesThat().resideInAPackage("..adapter.in..")
                .check(classes);
    }

    @Test
    @DisplayName("Application 서비스는 도메인과 포트에만 의존한다")
    void applicationServiceShouldOnlyDependOnDomainAndPorts() {
        noClasses()
                .that().resideInAPackage("..application.service..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .check(classes);
    }
}
