package com.aimix_aimixapi.gpt.token.service.aggregation;

import com.aimix_aimixapi.gpt.token.dto.GptTokenUsageGraphResponse;
import com.aimix_aimixapi.gpt.token.entity.GptTokenUsage;
import com.aimix_aimixapi.gpt.token.entity.GptUsageType;
import com.aimix_aimixapi.gpt.token.util.TokenUsageDateRangeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GPT 토큰 사용량 집계 및 통계 계산 서비스
 * 토큰 사용량 데이터를 기간별, 사용 유형별, API 키별로 집계하는 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GptTokenUsageAggregationService {

    /**
     * 기간별로 데이터 집계 (daily/weekly/monthly)
     * <p>사용량 데이터를 period에 따라 일별, 주별, 월별로 집계합니다.
     *
     * @param usageList 집계할 사용량 데이터 리스트
     * @param period    집계 기간 (daily/weekly/monthly)
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 기간별로 집계된 DailyData 리스트
     */
    public List<GptTokenUsageGraphResponse.DailyData> aggregateByPeriod(
            List<GptTokenUsage> usageList, String period, LocalDate startDate, LocalDate endDate) {

        if (usageList.isEmpty()) {
            return new ArrayList<>();
        }

        // period에 따라 그룹화
        Map<LocalDate, List<GptTokenUsage>> groupedByDate = usageList.stream()
                .collect(Collectors.groupingBy(usage ->
                        TokenUsageDateRangeUtil.normalizeDateForPeriod(usage.getUsageDate(), period)));

        // 모든 날짜 범위 생성 (빈 날짜도 포함)
        List<LocalDate> dateRange = TokenUsageDateRangeUtil.generateDateRange(startDate, endDate, period);

        // 각 날짜별 데이터 생성
        return dateRange.stream()
                .map(date -> {
                    List<GptTokenUsage> dateUsages = groupedByDate.getOrDefault(date, List.of());
                    return buildDailyData(date, dateUsages);
                })
                .sorted(Comparator.comparing(GptTokenUsageGraphResponse.DailyData::getDate))
                .collect(Collectors.toList());
    }

    /**
     * 날짜별 데이터 생성
     * <p>특정 날짜의 사용량 데이터를 집계하여 DailyData로 변환합니다.
     *
     * @param date   집계할 날짜
     * @param usages 해당 날짜의 사용량 데이터 리스트
     * @return 집계된 DailyData
     */
    public GptTokenUsageGraphResponse.DailyData buildDailyData(LocalDate date, List<GptTokenUsage> usages) {
        if (usages.isEmpty()) {
            // 빈 날짜의 경우 모든 사용 유형을 0으로 초기화
            Map<String, Long> emptyByType = new HashMap<>();
            for (GptUsageType type : GptUsageType.values()) {
                emptyByType.put(type.name(), 0L);
            }

            return GptTokenUsageGraphResponse.DailyData.builder()
                    .date(date)
                    .totalTokens(0L)
                    .byType(emptyByType)
                    .promptTokens(0L)
                    .completionTokens(0L)
                    .requestCount(0)
                    .userApiKeyTokens(0L)
                    .sharedApiKeyTokens(0L)
                    .build();
        }

        // 사용 유형별 집계 (모든 유형 초기화 후 집계)
        Map<String, Long> byType = new HashMap<>();
        for (GptUsageType type : GptUsageType.values()) {
            byType.put(type.name(), 0L);
        }
        usages.forEach(usage -> {
            String typeName = usage.getUsageType().name();
            byType.put(typeName, byType.getOrDefault(typeName, 0L) + usage.getTotalTokens());
        });

        // 총합 계산
        Long totalTokens = usages.stream()
                .mapToLong(GptTokenUsage::getTotalTokens)
                .sum();
        Long promptTokens = usages.stream()
                .mapToLong(GptTokenUsage::getPromptTokens)
                .sum();
        Long completionTokens = usages.stream()
                .mapToLong(GptTokenUsage::getCompletionTokens)
                .sum();
        Integer requestCount = usages.stream()
                .mapToInt(GptTokenUsage::getRequestCount)
                .sum();

        // API 키 타입별 통계 계산
        ApiKeyStatistics apiKeyStats = calculateApiKeyStatistics(usages);

        return GptTokenUsageGraphResponse.DailyData.builder()
                .date(date)
                .totalTokens(totalTokens)
                .byType(byType)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .requestCount(requestCount)
                .userApiKeyTokens(apiKeyStats.getUserApiKeyTokens())
                .sharedApiKeyTokens(apiKeyStats.getSharedApiKeyTokens())
                .build();
    }

    /**
     * 사용 유형별 총합 계산
     * <p>전체 사용량 데이터를 사용 유형별로 집계합니다.
     *
     * @param usageList 집계할 사용량 데이터 리스트
     * @return 사용 유형별 토큰 사용량 맵
     */
    public Map<String, Long> calculateByType(List<GptTokenUsage> usageList) {
        Map<String, Long> byType = new HashMap<>();

        // 모든 사용 유형 초기화
        for (GptUsageType type : GptUsageType.values()) {
            byType.put(type.name(), 0L);
        }

        // 사용량 집계
        usageList.forEach(usage -> {
            String typeName = usage.getUsageType().name();
            byType.put(typeName, byType.getOrDefault(typeName, 0L) + usage.getTotalTokens());
        });

        return byType;
    }

    /**
     * API 키별 통계 계산
     * <p>사용량 데이터를 사용자 API 키와 공용 API 키로 구분하여 집계합니다.
     *
     * @param usageList 집계할 사용량 데이터 리스트
     * @return API 키별 통계 정보
     */
    public ApiKeyStatistics calculateApiKeyStatistics(List<GptTokenUsage> usageList) {
        long userApiKeyTokens = usageList.stream()
                .filter(usage -> usage.getIsUserApiKey() != null && usage.getIsUserApiKey())
                .mapToLong(GptTokenUsage::getTotalTokens)
                .sum();

        long sharedApiKeyTokens = usageList.stream()
                .filter(usage -> usage.getIsUserApiKey() == null || !usage.getIsUserApiKey())
                .mapToLong(GptTokenUsage::getTotalTokens)
                .sum();

        return new ApiKeyStatistics(userApiKeyTokens, sharedApiKeyTokens);
    }

    /**
     * 요약 통계 계산
     * <p>DailyData 리스트로부터 평균, 최대값 등의 요약 통계를 계산합니다.
     *
     * @param dailyDataList 집계된 일별 데이터 리스트
     * @return 요약 통계 정보 (평균, 최대값)
     */
    public SummaryStatistics calculateSummaryStatistics(List<GptTokenUsageGraphResponse.DailyData> dailyDataList) {
        if (dailyDataList.isEmpty()) {
            return new SummaryStatistics(0.0, 0L);
        }

        Double averageDailyTokens = dailyDataList.stream()
                .mapToLong(GptTokenUsageGraphResponse.DailyData::getTotalTokens)
                .average()
                .orElse(0.0);

        Long maxDailyTokens = dailyDataList.stream()
                .mapToLong(GptTokenUsageGraphResponse.DailyData::getTotalTokens)
                .max()
                .orElse(0L);

        return new SummaryStatistics(averageDailyTokens, maxDailyTokens);
    }

    /**
     * 요약 통계 정보
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class SummaryStatistics {
        private final Double averageDailyTokens;
        private final Long maxDailyTokens;
    }
}
