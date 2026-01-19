package com.aimix_aimixapi.gpt.token.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * GPT 토큰 사용량 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GptTokenUsageResponse {
    /**
     * 오늘의 총 토큰 사용량
     */
    private Long todayTotalTokens;

    /**
     * 전체 총 토큰 사용량
     */
    private Long totalTokens;

    /**
     * 날짜별 사용량 목록
     */
    private List<DailyUsage> dailyUsageList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyUsage {
        /**
         * 사용 날짜
         */
        private LocalDate date;

        /**
         * 사용 유형
         */
        private String usageType;

        /**
         * 모델명
         */
        private String model;

        /**
         * 프롬프트 토큰 수
         */
        private Integer promptTokens;

        /**
         * 완료 토큰 수
         */
        private Integer completionTokens;

        /**
         * 총 토큰 수
         */
        private Integer totalTokens;

        /**
         * API 호출 횟수
         */
        private Integer requestCount;

        /**
         * 사용자 API 키 사용 여부
         * true: 사용자 개인 API 키 사용, false: 공용(기본) API 키 사용
         */
        private Boolean isUserApiKey;
    }
}
