package com.demo.myapplication.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 전역 설정.
 *
 * <p>{@link WebMvcConfigurer}를 구현하여 {@code WebMvcAutoConfiguration}을 유지하면서
 * CORS, Formatter 등 MVC 동작을 확장한다.</p>
 *
 * <h2>@EnableWebMvc를 사용하지 않는 이유</h2>
 * <ul>
 *   <li>{@code @EnableWebMvc}는 {@code WebMvcAutoConfiguration} 전체를 비활성화한다.</li>
 *   <li>Jackson ObjectMapper, AcceptHeaderLocaleResolver, ContentNegotiation 등
 *       자동 설정이 모두 사라져 수동 등록이 필요해진다.</li>
 *   <li>{@code WebMvcConfigurer} 구현만으로 자동 설정을 유지하면서 확장할 수 있다.</li>
 * </ul>
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see com.demo.myapplication.global.config.I18nConfig I18nConfig (LocaleResolver는 여기서 등록)
 */
@Configuration(proxyBeanMethods = false)
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 전역 CORS 설정.
     *
     * <p>개발 환경에서 프론트엔드 개발 서버(localhost:3000)와
     * 동일 포트(localhost:8080)로부터의 요청을 허용한다.</p>
     *
     * <p>{@code allowCredentials(true)}를 사용하므로 {@code allowedOrigins("*")}(와일드카드)는
     * 사용할 수 없다 — 브라우저 보안 정책(CORS spec) 상 자격증명과 와일드카드는 함께 허용되지 않는다.</p>
     *
     * <p>운영 환경에서는 허용 출처를 프로퍼티로 외부화할 것을 권장한다.</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",   // 프론트엔드 개발 서버 (예: React, Vue)
                        "http://localhost:8080"    // 동일 서버 또는 API Gateway
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Preflight 응답 캐시: 1시간 (초 단위)
    }

    /**
     * 커스텀 Formatter/Converter 등록.
     *
     * <p>{@code ConfigurableWebBindingInitializer}에 자동으로 반영된다.
     * 이를 통해 요청 파라미터, 경로 변수 등을 도메인 타입으로 직접 변환할 수 있다.</p>
     *
     * <p>예시: String → MemberId, String → LocalDate (ISO 8601 형식)
     * <pre>{@code registry.addFormatter(new MemberIdFormatter()); }</pre>
     * </p>
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 필요 시 커스텀 Formatter를 여기에 등록한다.
        // 예: registry.addFormatter(new MemberIdFormatter());
    }
}
