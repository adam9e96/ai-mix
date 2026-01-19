package com.aimix_aimixapi.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 쿠키에서 AccessToken을 추출하여 인증 처리 (쿠키 기반 인증만 지원)
 * - Authorization 헤더의 Bearer 토큰은 지원하지 않음
 * - 토큰 검증 후 SecurityContext에 인증 정보 설정
 * - JwtProvider의 getAuthentication()을 사용하여 Authentication 객체 생성
 * <p>
 * 인증 방식: HttpOnly Secure 쿠키만 사용
 * - 쿠키 이름: "accessToken"
 * - Bearer 토큰 방식은 지원하지 않음
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 존재하고 유효한 경우
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // JwtProvider의 getAuthentication()을 통해 Authentication 객체 생성
            Authentication authentication = jwtProvider.getAuthentication(token);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 인증 성공: username={}, authorities={}",
                        authentication.getName(), authentication.getAuthorities());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * AccessToken 추출
     * 쿠키에서 accessToken 읽기 (HttpOnly Secure 쿠키 방식만 지원)
     * 주의: Authorization 헤더의 Bearer 토큰은 지원하지 않음
     * 모든 인증은 쿠키 기반으로만 처리됨
     */
    private String resolveToken(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (StringUtils.hasText(token)) {
                        return token;
                    }
                }
            }
        }
        
        return null;
    }
}

