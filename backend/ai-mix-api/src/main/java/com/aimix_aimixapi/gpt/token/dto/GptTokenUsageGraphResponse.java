package com.aimix_aimixapi.gpt.token.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * GPT 토큰 사용량 그래프 응답 DTO
 * - 그래프화를 위한 집계된 데이터 제공
 * - 날짜별, 사용 유형별 토큰 사용량 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GptTokenUsageGraphResponse {

    /**
     * 요약 통계 정보
     */
    private Summary summary;

    /**
     * 날짜별 데이터 목록 (기간에 따라 집계)
     */
    private List<DailyData> data;

    /**
     * 전체 기간 사용 유형별 총합
     */
    private Map<String, Long> byType;

    /**
     * 요약 통계 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /**
         * 전체 기간 총 토큰 수
         */
        private Long totalTokens;

        /**
         * 오늘 총 토큰 수
         */
        private Long todayTotalTokens;

        /**
         * 일평균 토큰 수
         */
        private Double averageDailyTokens;

        /**
         * 일 최대 토큰 수
         */
        private Long maxDailyTokens;

        /**
         * 집계 기간 (daily/weekly/monthly)
         */
        private String period;

        /**
         * 조회 기간 (일 수)
         */
        private Integer days;

        /**
         * 사용자 API 키 사용량 (전체 기간)
         */
        private Long userApiKeyTokens;

        /**
         * 공용 API 키 사용량 (전체 기간)
         */
        private Long sharedApiKeyTokens;
    }

    /**
     * 날짜별 데이터 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyData {
        /**
         * 날짜 (또는 주/월)
         */
        private LocalDate date;

        /**
         * 해당 날짜 총 토큰 수
         */
        private Long totalTokens;

        /**
         * 사용 유형별 토큰 수
         */
        private Map<String, Long> byType;

        /**
         * 프롬프트 토큰 수
         */
        private Long promptTokens;

        /**
         * 완료 토큰 수
         */
        private Long completionTokens;

        /**
         * API 호출 횟수
         */
        private Integer requestCount;

        /**
         * 사용자 API 키 사용량 (해당 날짜)
         */
        private Long userApiKeyTokens;

        /**
         * 공용 API 키 사용량 (해당 날짜)
         */
        private Long sharedApiKeyTokens;
    }
}
