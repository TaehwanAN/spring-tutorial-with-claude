package com.demo.myapplication.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * 국제화(i18n) 설정.
 *
 * <p>Spring Boot는 classpath root의 {@code messages.properties}가 있으면
 * {@link org.springframework.context.MessageSource}를 자동 구성한다.
 * 이 클래스는 {@link LocaleResolver}를 명시적으로 등록해 지원 locale과
 * fallback 정책을 문서화한다.
 *
 * <h2>동작 원리</h2>
 * <ol>
 *   <li>클라이언트가 {@code Accept-Language: ko} 헤더를 전송한다.</li>
 *   <li>{@link AcceptHeaderLocaleResolver}가 헤더를 파싱해 {@code Locale.KOREAN}을 결정한다.</li>
 *   <li>지원 목록에 없는 locale이면 {@code defaultLocale}(영어)로 fallback한다.</li>
 *   <li>{@link org.springframework.context.MessageSource}가 locale에 맞는
 *       {@code messages_ko.properties} 파일에서 메시지를 조회한다.</li>
 * </ol>
 *
 * <h2>지원 locale</h2>
 * <ul>
 *   <li>{@code en} — 영어 (기본값, messages.properties)</li>
 *   <li>{@code ko} — 한국어 (messages_ko.properties)</li>
 *   <li>{@code es-MX} — 스페인어/멕시코 (messages_es_MX.properties)</li>
 * </ul>
 *
 * <h2>MessageSource 설정</h2>
 * YAML에서 {@code spring.messages} 네임스페이스로 제어한다.
 * {@code fallback-to-system-locale: false}로 설정해 서버 JVM 언어에
 * 의도치 않게 끌려가는 상황을 방지한다.
 */
@Configuration
public class I18nConfig {

    @Bean
    @SuppressWarnings("null")  // List.of()는 항상 non-null — Eclipse 정적 분석 한계
    LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(
                Locale.ENGLISH,
                Locale.KOREAN,
                Locale.forLanguageTag("es-MX")
        ));
        return resolver;
    }
}
