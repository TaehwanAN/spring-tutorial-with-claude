package com.demo.myapplication.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP 요청/응답 로깅 필터.
 *
 * <p>{@link OncePerRequestFilter}를 상속하여 FORWARD, INCLUDE 등의 내부 디스패치에서
 * 중복 호출되지 않음을 보장한다.</p>
 *
 * <p>일반 {@code Filter}를 직접 구현하면, Spring MVC의 오류 처리 시
 * {@code /error}로 FORWARD될 때 필터가 한 번 더 실행될 수 있다.
 * {@code OncePerRequestFilter}는 이를 방지한다.</p>
 *
 * <h2>등록 방식</h2>
 * {@code @Component}를 붙이지 않는다 — {@link com.demo.myapplication.global.config.FilterConfig}의
 * {@code FilterRegistrationBean}을 통해서만 등록하여 이중 등록을 방지한다.
 *
 * <h2>로그 포맷</h2>
 * <pre>[HTTP_STATUS] METHOD /uri - Xms</pre>
 * 예시: {@code [200] GET /api/info - 12ms}
 *
 * @see com.demo.myapplication.global.config.FilterConfig
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            // finally 블록: 필터 체인에서 예외가 발생하더라도 반드시 로깅한다.
            // response.getStatus()는 응답이 커밋된 후 실제 상태 코드를 반환한다.
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.info("[{}] {} {} - {}ms",
                    response.getStatus(),
                    request.getMethod(),
                    request.getRequestURI(),
                    elapsedMs);
        }
    }
}
