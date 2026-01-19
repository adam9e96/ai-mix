package com.aimix_aimixapi.auth.jwt;

import com.aimix_aimixapi.auth.dao.RedisDao;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JwtTokenProvider
 * --------------------------------------------------------
 * ✔ 역할 요약:
 * 1) AccessToken / RefreshToken 생성
 * 2) JWT 서명 검증 + 만료 검증
 * 3) JWT 의 subject(username) 추출
 * 4) JWT 로부터 Authentication(UserDetails 기반) 생성
 * <p>
 * ✔ JWT 인증의 핵심 로직을 가진 클래스.
 * <p>
 * ✔ AccessToken: HttpOnly Secure 쿠키로 API 요청 인증
 * ✔ RefreshToken: Redis + 쿠키에 저장하여 토큰 재발급용
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final UserDetailsService userDetailsService;
    private final RedisDao redisDao;

    /**
     * -- GETTER --
     * JwtProperties 반환 (만료 시간 계산용)
     */
    @Getter
    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    // ACCESS TOKEN 생성
    public String generateAccessToken(String username) {
        return buildToken(username, jwtProperties.getAccessTokenExpiration());
    }

    // REFRESH TOKEN 생성
    public String generateRefreshToken(String username) {
        String token = buildToken(username, jwtProperties.getRefreshTokenExpiration());

        // Redis 저장 추가
        saveRefreshTokenToRedis(username, token);

        return token;
    }

    /**
     * JWT 토큰 생성
     * - username을 subject에 저장
     * - 사용자 권한을 claims에 포함 (UserDetailsService 통해 조회)
     * - issuer, issuedAt, expiration 설정
     * - 서명 키로 서명
     */
    private String buildToken(String username, long expiryMilliseconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMilliseconds);

        // 사용자 권한(UserDetailsService 통해 조회 필요)
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(username)  // username을 JWT의 subject에 저장
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // JWT 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT가 만기됨: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT 유효하지않음: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰에서 subject(username) 추출
     *
     * @param token JWT 토큰
     * @return JWT의 subject (username/email)
     */
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); // 만료되어도 subject는 존재
        }
    }

    /**
     * JWT → Authentication 변환
     * - 반드시 UserDetailsService로 실제 유저 객체를 가져와야 함
     * - principal을 UserDetails 객체로 설정
     *
     * @param token JWT 토큰
     * @return Authentication 객체 (실패 시 null)
     */
    public Authentication getAuthentication(String token) {
        try {
            String username = getUsername(token);
            log.debug("JWT에서 추출된 username = {}", username);

            // 반드시 UserDetailsService로 실제 유저 객체를 가져와야 함
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // principal을 UserDetails 객체로 넣는다.
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        } catch (Exception e) {
            log.error("JWT 인증 객체 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    // claims 전체 반환 (optional)
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private void saveRefreshTokenToRedis(String username, String token) {
        long expiration = jwtProperties.getRefreshTokenExpiration();
        redisDao.setValues("RT:" + username, token, Duration.ofMillis(expiration));
        log.info("[Redis 저장 완료] RT:{} stored", username);
    }

    public boolean validateRefreshToken(String username, String token) {
        if (!validateToken(token)) return false; // JWT 자체 만료/서명 검증
        String saved = (String) redisDao.getValues("RT:" + username);
        return saved != null && saved.equals(token);
    }

    public void deleteRefresh(String username) {
        redisDao.deleteValues("RT:" + username);
    }
}
