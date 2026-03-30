package com.demo.myapplication.global.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebMvc.fn 함수형 라우팅 설정 (학습 예시).
 *
 * <p>어노테이션 기반 {@code @Controller}/{@code @RequestMapping} 없이
 * {@link RouterFunction} DSL만으로 HTTP 엔드포인트를 등록하는 방법을 시연한다.</p>
 *
 * <h2>핵심 타입</h2>
 * <ul>
 *   <li>{@link RouterFunction} — 요청 조건 → 핸들러 함수 매핑 규칙</li>
 *   <li>{@code HandlerFunction<ServerResponse>} — 실제 요청 처리 (req → response)</li>
 *   <li>{@link ServerResponse} — 응답 빌더 (상태 코드, 헤더, 바디)</li>
 * </ul>
 *
 * <h2>@Controller와의 공존</h2>
 * {@code RouterFunctionMapping}(Order=3)과 {@code RequestMappingHandlerMapping}(Order=0)은
 * 공존 가능하다. Order 숫자가 낮을수록 우선순위가 높으므로,
 * {@code @Controller} 매핑이 먼저 평가되고 일치하지 않으면 {@code RouterFunction}이 처리한다.
 *
 * <h2>제공 엔드포인트</h2>
 * <ul>
 *   <li>GET /api/info — 서비스 기본 정보</li>
 *   <li>GET /api/info/time — 서버 현재 시간</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class RouterFunctionConfig {

    /**
     * 서비스 정보 조회 엔드포인트.
     *
     * <p>{@code /api/members} 기반의 {@code MemberController}와 경로가 겹치지 않도록
     * {@code /api/info} 경로를 사용한다.</p>
     *
     * <p>{@code Map.of(...)}를 응답 바디로 사용하면 Jackson이 자동으로 JSON 직렬화한다.</p>
     */
    @Bean
    RouterFunction<ServerResponse> apiInfoRouter() {
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/info"), builder -> builder
                        .GET("", req -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                        "service", "spring-tutorial",
                                        "version", "0.1.0",
                                        "status", "UP",
                                        "timestamp", LocalDateTime.now().toString()
                                )))
                        .GET("/time", req -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                        "serverTime", LocalDateTime.now().toString()
                                )))
                )
                .build();
    }
}
