package com.aimix_aimixapi.gpt.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * GPT 토큰 사용량 정보 DTO
 * - promptTokens, completionTokens, totalTokens를 포함
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Getter
@RequiredArgsConstructor
public class GptTokenUsage {
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;
}
