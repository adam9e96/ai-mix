package com.aimix_aimixapi.auth.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 관련 설정값을 주입받는 클래스
 * application.yml의 jwt.* 설정값을 자동으로 바인딩
 * <p>
 * 기본값:
 * - accessTokenExpiration: 30분 (1,800,000ms)
 * - refreshTokenExpiration: 7일 (604,800,000ms)
 * <p>
 * 환경 변수로 오버라이드 가능:
 * - AIMIX_JWT_ACCESS_EXPIRE: AccessToken 만료 시간 (밀리초)
 * - AIMIX_JWT_REFRESH_EXPIRE: RefreshToken 만료 시간 (밀리초)
 */
@Log4j2
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt") // jwt. 로시작하는 설정값을 자동으로 바인딩
public class JwtProperties {
    /**
     * JWT 시크릿 키
     */
    private String secretKey;

    /**
     * AccessToken 만료 시간 (밀리초)
     * 기본값: 30분 (1,800,000ms)
     */
    private long accessTokenExpiration;

    /**
     * RefreshToken 만료 시간 (밀리초)
     * 기본값: 7일 (604,800,000ms)
     */
    private long refreshTokenExpiration;

    /**
     * JWT 발급자
     */
    private String issuer;

    /**
     * 설정값 바인딩 후 검증 및 로깅
     */
    @PostConstruct
    public void init() {
        log.info("JwtProperties 초기화:");
        log.info("  - accessTokenExpiration: {}ms ({}초, {}분)", 
                accessTokenExpiration, accessTokenExpiration / 1000, accessTokenExpiration / 60000);
        log.info("  - refreshTokenExpiration: {}ms ({}초, {}일)", 
                refreshTokenExpiration, refreshTokenExpiration / 1000, refreshTokenExpiration / 86400000);
        log.info("  - issuer: {}", issuer);
        
        // 값 검증
        if (accessTokenExpiration <= 0) {
            log.error("⚠️ accessTokenExpiration이 0 이하입니다! 기본값 3600000ms로 설정합니다.");
            this.accessTokenExpiration = 3600000L; // 1시간
        }
        if (refreshTokenExpiration <= 0) {
            log.error("⚠️ refreshTokenExpiration이 0 이하입니다! 기본값 604800000ms로 설정합니다.");
            this.refreshTokenExpiration = 604800000L; // 7일
        }
    }
}
