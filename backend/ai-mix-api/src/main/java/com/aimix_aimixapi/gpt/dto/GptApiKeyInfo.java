package com.aimix_aimixapi.gpt.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * GPT API 키 정보 DTO
 * - API 키 값과 사용자 API 키 여부를 함께 관리
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Getter
@RequiredArgsConstructor
public class GptApiKeyInfo {
    private final String apiKey;
    private final boolean isUserApiKey;
}
