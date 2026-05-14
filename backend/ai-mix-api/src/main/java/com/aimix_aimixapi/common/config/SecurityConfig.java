package com.aimix_aimixapi.common.config;

import com.aimix_aimixapi.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 클래스
 * - CORS 설정: 모든 Origin 허용 (운영 환경에서는 특정 도메인으로 제한 권장)
 * - CSRF 비활성화
 * - 세션 stateless 설정
 * - HTTP Basic 인증 비활성화
 * - 폼 로그인 비활성화
 * - 로그아웃 비활성화
 * - 인증 불필요 URL: `/api/auth/signup`, `/api/auth/login`
 * - `JwtAuthenticationFilter` 등록
 * - 인증 방식: 쿠키 기반 인증만 지원 (HttpOnly Secure 쿠키)
 *   * Bearer 토큰 방식은 지원하지 않음
 *   * 모든 인증은 쿠키의 "accessToken"을 통해서만 처리됨
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:}")
    private String additionalOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (JWT 사용 시 세션 기반 인증이 아니므로 CSRF 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 stateless 설정 (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 로그아웃 비활성화
                .logout(AbstractHttpConfigurer::disable)
                // 요청별 인증 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요한 URL (signup, login)
                        // Health check (Railway 배포용)
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 정적 리소스 (업로드된 이미지 등) 인증 불필요
                        .requestMatchers("/uploads/**").permitAll()
                        // QNA 게시글 목록 조회, 검색, 상세 조회는 인증 불필요
                        .requestMatchers(HttpMethod.GET, "/api/v1/qna/questions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/qna/questions/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/qna/questions/**").permitAll()
                        // QNA 질문 생성은 인증 선택적 (익명 게시글 생성을 위해)
                        .requestMatchers(HttpMethod.POST, "/api/v1/qna/questions").permitAll()
                        // QNA 질문 수정은 인증 선택적 (익명 게시글 수정을 위해)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/qna/questions/**").permitAll()
                        // QNA 질문 삭제는 인증 선택적 (익명 게시글 삭제를 위해)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/qna/questions/**").permitAll()
                        // QNA 답변 채택/해제는 인증 선택적 (익명 질문의 답변 채택을 위해)
                        .requestMatchers(HttpMethod.POST, "/api/v1/qna/answers/*/accept").permitAll()
                        // QNA 사용자 프로필 조회는 인증 불필요 (공개 정보)
                        .requestMatchers(HttpMethod.GET, "/api/v1/qna/users/**").permitAll()
                        // 지식백과 카드 조회는 인증 불필요 (공개 조회)
                        .requestMatchers(HttpMethod.GET, "/api/v1/knowledge/cards/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/knowledge/map").permitAll()
                        // 지식백과 카드 작성/수정/삭제는 인증 필요
                        .requestMatchers("/api/v1/knowledge/**").authenticated()
                        .requestMatchers(("/api/v1/chat/**")).authenticated()
                        .requestMatchers("/api/v1/battle/**").authenticated()
                        .requestMatchers("/api/v1/user/me").authenticated()
                        .requestMatchers("/api/v1/gamification/**").authenticated()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated())
                // JWT 인증 필터 등록 (쿠키 기반 인증만 지원)
                // 쿠키에 AccessToken이 있는 요청을 인증 처리
                // Bearer 토큰 방식은 지원하지 않음
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * - React 앱 (5173 포트) 허용
     * - 쿠키 기반 인증 지원
     * - 다른 PC에서 접근 시 해당 IP 주소를 추가해야 함
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정
        // 개발 환경: React 앱 (5173 포트)
        // 다른 PC에서 접근 시: "http://{다른PC의IP주소}:5173" 형식으로 추가
        // 예: "http://192.168.1.100:5173"
        // 운영 환경에서는 환경 변수로 관리 권장
        List<String> origins = new ArrayList<>(Arrays.asList(
                "http://localhost:5173",  // React 개발 서버 (Vite 기본 포트)
                "http://127.0.0.1:5173",  // localhost 대체
                "http://localhost:3000",   // React 개발 서버 (Create React App 기본 포트)
                "http://127.0.0.1:3000",    // localhost 대체
                "http://192.168.0.135:5173",  // 다른 PC (슬래시 제거)
                "http://172.30.1.51:5173",
                "http://192.168.45.28:5173",
                "http://172.30.1.88:5173", // cafe
                "http://172.30.1.10:5173",
                "http://172.30.1.65:5173"
        ));
        // 환경변수로 추가 Origin 설정 (Vercel 배포 도메인 등)
        // 예: APP_CORS_ALLOWED_ORIGINS=https://ai-mix.vercel.app,https://custom-domain.com
        if (additionalOrigins != null && !additionalOrigins.isBlank()) {
            origins.addAll(Arrays.asList(additionalOrigins.split(",")));
        }
        configuration.setAllowedOrigins(origins);

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 인증 정보 포함 허용 (쿠키 사용을 위해)
        // 주의: allowCredentials(true)일 때는 setAllowedOrigins에 "*" 사용 불가
        configuration.setAllowCredentials(true);

        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        // Set-Cookie는 자동으로 전송되지만, 명시적으로 노출 설정
        configuration.setExposedHeaders(Arrays.asList(
                "Content-Type",
                "Set-Cookie"
        ));

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}