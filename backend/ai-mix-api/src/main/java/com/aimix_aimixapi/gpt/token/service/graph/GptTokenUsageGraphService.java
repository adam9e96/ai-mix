package com.aimix_aimixapi.gpt.token.service.graph;

import com.aimix_aimixapi.gpt.token.dto.GptTokenUsageGraphResponse;
import com.aimix_aimixapi.gpt.token.entity.GptTokenUsage;
import com.aimix_aimixapi.gpt.token.entity.GptUsageType;
import com.aimix_aimixapi.gpt.token.repository.GptTokenUsageRepository;
import com.aimix_aimixapi.gpt.token.service.aggregation.ApiKeyStatistics;
import com.aimix_aimixapi.gpt.token.service.aggregation.GptTokenUsageAggregationService;
import com.aimix_aimixapi.gpt.token.service.query.GptTokenUsageQueryService;
import com.aimix_aimixapi.gpt.token.validator.GptTokenUsageValidator;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPT 토큰 사용량 그래프 데이터 생성 서비스
 * 그래프화를 위한 토큰 사용량 데이터를 생성하는 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GptTokenUsageGraphService {

    private final GptTokenUsageRepository gptTokenUsageRepository;
    private final GptTokenUsageQueryService queryService;
    private final GptTokenUsageAggregationService aggregationService;

    /**
     * 사용자별 토큰 사용량 그래프 데이터 조회
     * <p>그래프화를 위한 최적화된 토큰 사용량 데이터를 제공합니다.
     * 날짜별, 사용 유형별, API 키 타입별로 집계된 데이터를 반환합니다.
     *
     * <p><b>집계 기간 (period):</b>
     * <ul>
     *   <li><b>daily</b>: 일별 집계 (기본값)</li>
     *   <li><b>weekly</b>: 주별 집계 (월요일 기준)</li>
     *   <li><b>monthly</b>: 월별 집계 (월의 첫 날 기준)</li>
     * </ul>
     *
     * <p><b>반환 데이터 구조:</b>
     * <ul>
     *   <li><b>summary</b>: 전체 기간 요약 통계 (총 토큰 수, 평균, 최대값, 사용자/공용 키별 통계)</li>
     *   <li><b>data</b>: 날짜별 집계 데이터 (period에 따라 일/주/월 단위)</li>
     *   <li><b>byType</b>: 사용 유형별 총합 (CHAT, QNA, BATTLE_QUESTION 등)</li>
     * </ul>
     *
     * <p><b>API 키 타입별 통계:</b>
     * <ul>
     *   <li>summary.userApiKeyTokens: 전체 기간 사용자 API 키 사용량</li>
     *   <li>summary.sharedApiKeyTokens: 전체 기간 공용 API 키 사용량</li>
     *   <li>data[].userApiKeyTokens: 각 날짜별 사용자 API 키 사용량</li>
     *   <li>data[].sharedApiKeyTokens: 각 날짜별 공용 API 키 사용량</li>
     * </ul>
     *
     * <p><b>사용 예시:</b>
     * <pre>{@code
     * // 최근 30일 일별 그래프 데이터 조회
     * GptTokenUsageGraphResponse response = graphService
     *     .getTokenUsageGraph(user, "daily", 30);
     *
     * // 최근 3개월 월별 그래프 데이터 조회
     * GptTokenUsageGraphResponse response = graphService
     *     .getTokenUsageGraph(user, "monthly", 90);
     * }</pre>
     *
     * @param user   사용자 엔티티 (null이면 빈 응답 반환)
     * @param period 집계 기간 (daily/weekly/monthly, 기본값: daily)
     * @param days   조회할 기간 (일 수, 1~365, 기본값: 30)
     * @return 그래프용 토큰 사용량 데이터
     * @apiNote 점검O
     * @see GptTokenUsageRepository#findByUserAndDateRange(User, LocalDate, LocalDate)
     * @since 2025-12-30
     */
    @Transactional(readOnly = true)
    public GptTokenUsageGraphResponse getTokenUsageGraph(User user, String period, int days) {
        // 유효성 검증
        period = GptTokenUsageValidator.validatePeriod(period);
        days = GptTokenUsageValidator.validateDays(days);

        if (user == null) {
            log.warn("사용자 정보가 없어 그래프 데이터를 조회할 수 없습니다");
            return buildEmptyGraphResponse(period, days);
        }

        log.info("토큰 사용량 그래프 조회: userId={}, period={}, days={}", user.getId(), period, days);

        // 오늘을 포함하여 최근 N일간의 데이터 조회
        // 예: days=30이면 오늘부터 29일 전까지 (총 30일)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 기간 내 모든 사용량 데이터 조회
        // findByUserAndDateRange: 날짜 내림차순, 사용 유형 오름차순으로 정렬된 리스트 반환
        List<GptTokenUsage> usageList = gptTokenUsageRepository.findByUserAndDateRange(user, startDate, endDate);

        // 전체 통계 계산
        // sumTotalTokensByUserAndDateRange: 기간 내 모든 레코드의 totalTokens 합계 (사용자/공용 키 구분 없이)
        Long totalTokens = gptTokenUsageRepository.sumTotalTokensByUserAndDateRange(user, startDate, endDate);
        // sumTotalTokensByUserAndDate: 오늘 날짜의 totalTokens 합계
        Long todayTotalTokens = queryService.getTodayTotalTokens(user);

        // 날짜별 데이터 집계
        List<GptTokenUsageGraphResponse.DailyData> dailyDataList = aggregationService
                .aggregateByPeriod(usageList, period, startDate, endDate);

        // 사용 유형별 총합 계산
        Map<String, Long> byType = aggregationService.calculateByType(usageList);

        // API 키 타입별 통계 계산
        ApiKeyStatistics apiKeyStats = aggregationService.calculateApiKeyStatistics(usageList);

        // 통계 계산
        GptTokenUsageAggregationService.SummaryStatistics summaryStats =
                aggregationService.calculateSummaryStatistics(dailyDataList);

        // 요약 정보 생성
        GptTokenUsageGraphResponse.Summary summary = GptTokenUsageGraphResponse.Summary.builder()
                .totalTokens(totalTokens)
                .todayTotalTokens(todayTotalTokens)
                .averageDailyTokens(summaryStats.getAverageDailyTokens())
                .maxDailyTokens(summaryStats.getMaxDailyTokens())
                .period(period)
                .days(days)
                .userApiKeyTokens(apiKeyStats.getUserApiKeyTokens())
                .sharedApiKeyTokens(apiKeyStats.getSharedApiKeyTokens())
                .build();

        return GptTokenUsageGraphResponse.builder()
                .summary(summary)
                .data(dailyDataList)
                .byType(byType)
                .build();
    }

    /**
     * 빈 그래프 응답 생성
     * <p>사용자 정보가 없거나 데이터가 없는 경우 빈 응답을 생성합니다.
     *
     * @param period 집계 기간
     * @param days   조회 기간
     * @return 빈 그래프 응답
     */
    private GptTokenUsageGraphResponse buildEmptyGraphResponse(String period, int days) {
        GptTokenUsageGraphResponse.Summary summary = GptTokenUsageGraphResponse.Summary.builder()
                .totalTokens(0L)
                .todayTotalTokens(0L)
                .averageDailyTokens(0.0)
                .maxDailyTokens(0L)
                .period(period != null ? period : "daily")
                .days(days)
                .userApiKeyTokens(0L)
                .sharedApiKeyTokens(0L)
                .build();

        Map<String, Long> byType = new HashMap<>();
        for (GptUsageType type : GptUsageType.values()) {
            byType.put(type.name(), 0L);
        }

        return GptTokenUsageGraphResponse.builder()
                .summary(summary)
                .data(new ArrayList<>())
                .byType(byType)
                .build();
    }
}
