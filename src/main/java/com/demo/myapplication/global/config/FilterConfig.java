package com.demo.myapplication.global.config;

import com.demo.myapplication.global.filter.RequestLoggingFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 서블릿 필터 등록 설정.
 *
 * <p>{@link FilterRegistrationBean}을 사용하면:
 * <ul>
 *   <li>실행 순서(order)를 명시적으로 제어할 수 있다.</li>
 *   <li>URL 패턴을 제한하여 특정 경로에만 필터를 적용할 수 있다.</li>
 *   <li>{@code @WebFilter + @ServletComponentScan} 방식보다 Spring Boot 자동 설정과
 *       더 잘 통합된다 ({@code Ordered} 인터페이스 미지원 문제 없음).</li>
 * </ul>
 *
 * <h2>필터 실행 순서 전략</h2>
 * <pre>
 * ORDER = HIGHEST_PRECEDENCE + 1  →  RequestLoggingFilter (요청 진입 시 가장 먼저)
 *   ↓ 처리 위임
 * ... (다른 필터들)
 *   ↓
 * DispatcherServlet → Controller → Service
 *   ↑ 응답 반환 시 역순
 * ORDER = HIGHEST_PRECEDENCE + 1  →  RequestLoggingFilter (응답 반환 시 가장 마지막)
 * </pre>
 * 가장 바깥에서 감싸므로 전체 요청 처리 시간을 정확히 측정할 수 있다.
 */
@Configuration(proxyBeanMethods = false)
public class FilterConfig {

    /**
     * 요청 로깅 필터 등록.
     *
     * <p>URL 패턴을 {@code /api/*}로 제한하여 정적 리소스({@code /static/}, {@code /error} 등)
     * 요청에는 로깅을 적용하지 않는다.</p>
     *
     * <p>필터를 {@code new RequestLoggingFilter()}로 직접 생성한다.
     * 만약 {@code RequestLoggingFilter}에 {@code @Component}가 붙어 있으면
     * Spring이 이미 빈으로 등록하여 이중 등록이 발생하므로, 두 방법 중 하나만 사용해야 한다.</p>
     */
    @Bean
    FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("requestLoggingFilter");
        return registration;
    }
}
