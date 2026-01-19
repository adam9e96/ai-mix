package com.aimix_aimixapi.gpt.token.service.aggregation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 키별 통계 정보
 * 사용자 API 키와 공용 API 키의 토큰 사용량을 담는 데이터 클래스
 */
@Getter
@AllArgsConstructor
public class ApiKeyStatistics {
    /**
     * 사용자 API 키 사용량
     */
    private final long userApiKeyTokens;

    /**
     * 공용 API 키 사용량
     */
    private final long sharedApiKeyTokens;
}
