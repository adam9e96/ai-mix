package com.aimix_aimixapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 확인 응답 DTO
 * - 개인정보 수정 전 비밀번호 검증 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordCheckResponse {

    /**
     * 비밀번호 일치 여부
     */
    private boolean verified;

    /**
     * 응답 메시지
     */
    private String message;
}
