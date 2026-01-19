package com.aimix_aimixapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 재발급 응답 DTO
 * - AccessToken과 RefreshToken은 모두 HttpOnly Secure 쿠키로 전달
 * - 응답 body는 성공 메시지만 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    /**
     * 성공 메시지
     * - 토큰은 쿠키로 전달되므로 응답 body에는 메시지만 포함
     */
    @Builder.Default
    private String message = "토큰이 재발급되었습니다";
    
    public static TokenResponse empty() {
        return TokenResponse.builder().build();
    }
}

