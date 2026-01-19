package com.aimix_aimixapi.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 키 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiKeyResponse {

    /**
     * API 키 존재 여부
     */
    private boolean hasApiKey;

    /**
     * 마스킹된 API 키 (보안용)
     * 예: "sk-...****"
     */
    private String maskedKey;

    /**
     * 마지막 검증 시간 (선택적, 추후 구현)
     */
    private LocalDateTime lastValidatedAt;
}
