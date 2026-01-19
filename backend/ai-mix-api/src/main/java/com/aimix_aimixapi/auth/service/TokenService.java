package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.auth.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 토큰 서비스
 * - JWT 토큰 생성 및 HttpOnly Secure 쿠키 설정/삭제 담당
 * - AccessToken과 RefreshToken을 쿠키로 관리
 * - 환경별 동적 설정 지원 (Secure, SameSite)
 *
 * <p>주요 기능:
 * <ul>
 *   <li>AccessToken 및 RefreshToken 생성 및 쿠키 설정</li>
 *   <li>HttpOnly Secure 쿠키로 토큰 전달 (XSS 공격 방지)</li>
 *   <li>환경별 쿠키 설정 (개발/프로덕션)</li>
 *   <li>토큰 쿠키 삭제 (로그아웃 시)</li>
 * </ul>
 *
 * <p>보안 고려사항:
 * <ul>
 *   <li>HttpOnly: JavaScript 접근 방지 (XSS 공격 방지)</li>
 *   <li>Secure: HTTPS에서만 전송 (프로덕션 환경)</li>
 *   <li>SameSite: CSRF 공격 방지 (Lax 또는 None)</li>
 *   <li>RefreshToken은 Redis에 저장되어 관리됨</li>
 * </ul>
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * 토큰 생성 및 쿠키 설정
     * - AccessToken과 RefreshToken을 생성하고 HttpOnly Secure 쿠키로 설정
     *
     * <p>동작 과정:
     * <ol>
     *   <li>JwtProvider를 통해 AccessToken 생성</li>
     *   <li>JwtProvider를 통해 RefreshToken 생성 (Redis에 자동 저장)</li>
     *   <li>AccessToken을 HttpOnly Secure 쿠키로 설정</li>
     *   <li>RefreshToken을 HttpOnly Secure 쿠키로 설정</li>
     * </ol>
     *
     * <p>주의사항:
     * <ul>
     *   <li>RefreshToken은 JwtProvider.generateRefreshToken()에서 Redis에 자동 저장됨</li>
     *   <li>쿠키 설정은 환경 변수에 따라 Secure, SameSite 값이 달라짐</li>
     *   <li>토큰 만료 시간은 JwtProperties에서 설정된 값을 사용</li>
     * </ul>
     *
     * @param email        사용자 이메일 (토큰에 포함될 사용자 식별자, null 불가)
     * @param httpResponse HTTP 응답 객체 (쿠키 설정용, null 불가)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public void generateAndSetTokens(String email, HttpServletResponse httpResponse) {
        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(email);
        String refreshToken = jwtProvider.generateRefreshToken(email);

        // 쿠키 설정
        setAccessTokenCookie(httpResponse, accessToken);
        setRefreshTokenCookie(httpResponse, refreshToken);

        // RefreshToken은 JwtProvider.generateRefreshToken()에서 Redis에 자동 저장됨
    }

    /**
     * AccessToken을 HttpOnly Secure 쿠키로 설정
     * - 환경별 동적 설정 지원 (Secure, SameSite)
     * - 개발: HTTP + Secure=false + SameSite=Lax
     * - 프로덕션: HTTPS + Secure=true + SameSite=Lax (또는 None)
     *
     * <p>쿠키 속성:
     * <ul>
     *   <li>httpOnly: true (JavaScript 접근 방지)</li>
     *   <li>secure: 환경 변수에 따라 설정 (${app.cookie.secure})</li>
     *   <li>path: "/" (모든 경로에서 사용 가능)</li>
     *   <li>maxAge: JwtProperties에서 설정된 AccessToken 만료 시간</li>
     *   <li>sameSite: 환경 변수에 따라 설정 (${app.cookie.same-site})</li>
     * </ul>
     *
     * @param response    HTTP 응답 객체 (null 불가)
     * @param accessToken AccessToken 값 (null이거나 빈 문자열이면 설정하지 않음)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            log.warn("AccessToken 쿠키 설정: accessToken이 null 또는 빈 문자열");
            return;
        }

        long maxAgeSeconds = jwtProvider.getJwtProperties().getAccessTokenExpiration() / 1000;

        ResponseCookie responseCookie = ResponseCookie
                .from("accessToken", accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString());

        log.debug("AccessToken 쿠키 설정: Secure={}, SameSite={}, maxAge={}초 ({}분)",
                cookieSecure, cookieSameSite, maxAgeSeconds, maxAgeSeconds / 60);
    }

    /**
     * RefreshToken을 HttpOnly Secure 쿠키로 설정
     * - AccessToken과 동일한 보안 정책 적용
     * - 환경별 동적 설정 지원 (Secure, SameSite)
     *
     * <p>쿠키 속성:
     * <ul>
     *   <li>httpOnly: true (JavaScript 접근 방지)</li>
     *   <li>secure: 환경 변수에 따라 설정 (${app.cookie.secure})</li>
     *   <li>path: "/" (모든 경로에서 사용 가능)</li>
     *   <li>maxAge: JwtProperties에서 설정된 RefreshToken 만료 시간</li>
     *   <li>sameSite: 환경 변수에 따라 설정 (${app.cookie.same-site})</li>
     * </ul>
     *
     * @param response     HTTP 응답 객체 (null 불가)
     * @param refreshToken RefreshToken 값 (null이거나 빈 문자열이면 설정하지 않음)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            log.warn("RefreshToken 쿠키 설정: refreshToken이 null 또는 빈 문자열");
            return;
        }

        long maxAgeSeconds = jwtProvider.getJwtProperties().getRefreshTokenExpiration() / 1000;

        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString());

        log.debug("RefreshToken 쿠키 설정: Secure={}, SameSite={}, maxAge={}초 ({}일)",
                cookieSecure, cookieSameSite, maxAgeSeconds, maxAgeSeconds / 86400);
    }

    /**
     * AccessToken 쿠키 삭제
     * - 로그아웃 시 호출됨
     * - 설정 시와 동일한 속성 사용 필수 (Secure, SameSite)
     * - maxAge=0으로 설정하여 쿠키 만료
     *
     * <p>주의사항:
     * <ul>
     *   <li>쿠키 삭제 시 설정 시와 동일한 Secure, SameSite 값 사용 필요</li>
     *   <li>maxAge=0으로 설정하여 브라우저에서 즉시 삭제</li>
     * </ul>
     *
     * @param response HTTP 응답 객체 (null 불가)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie responseCookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString());
        log.debug("AccessToken 쿠키 삭제 완료");
    }

    /**
     * RefreshToken 쿠키 삭제
     * - 로그아웃 시 호출됨
     * - 설정 시와 동일한 속성 사용 필수 (Secure, SameSite)
     * - maxAge=0으로 설정하여 쿠키 만료
     *
     * <p>주의사항:
     * <ul>
     *   <li>쿠키 삭제 시 설정 시와 동일한 Secure, SameSite 값 사용 필요</li>
     *   <li>maxAge=0으로 설정하여 브라우저에서 즉시 삭제</li>
     *   <li>Redis에 저장된 RefreshToken은 별도로 삭제해야 함 (AuthService.logout에서 처리)</li>
     * </ul>
     *
     * @param response HTTP 응답 객체 (null 불가)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString());
        log.debug("RefreshToken 쿠키 삭제 완료");
    }
}
