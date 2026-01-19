package com.aimix_aimixapi.gpt.token.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 토큰 사용량 날짜 범위 생성 유틸리티
 * 기간별(daily/weekly/monthly) 날짜 범위를 생성하는 유틸리티 클래스
 */
public class TokenUsageDateRangeUtil {

    /**
     * 기간별 날짜 범위 생성
     * <p>period에 따라 일별, 주별, 월별 날짜 범위를 생성합니다.
     *
     * <p><b>기간별 동작:</b>
     * <ul>
     *   <li><b>daily</b>: startDate부터 endDate까지 모든 날짜</li>
     *   <li><b>weekly</b>: startDate가 속한 주의 월요일부터 endDate가 속한 주의 월요일까지 (매주 월요일)</li>
     *   <li><b>monthly</b>: startDate가 속한 월의 1일부터 endDate가 속한 월의 1일까지 (매월 1일)</li>
     * </ul>
     *
     * @param startDate 시작 날짜 (포함)
     * @param endDate   종료 날짜 (포함)
     * @param period    집계 기간 (daily/weekly/monthly)
     * @return 날짜 범위 리스트 (period에 따라 정규화된 날짜들)
     */
    public static List<LocalDate> generateDateRange(LocalDate startDate, LocalDate endDate, String period) {
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate current = startDate;

        if ("weekly".equals(period)) {
            // 주 단위: 매주 월요일
            current = normalizeDateForPeriod(current, "weekly");
            while (!current.isAfter(endDate)) {
                dateRange.add(current);
                current = current.plusWeeks(1);
            }
        } else if ("monthly".equals(period)) {
            // 월 단위: 매월 1일
            current = normalizeDateForPeriod(current, "monthly");
            while (!current.isAfter(endDate)) {
                dateRange.add(current);
                current = current.plusMonths(1);
            }
        } else {
            // 일 단위: 모든 날짜
            while (!current.isAfter(endDate)) {
                dateRange.add(current);
                current = current.plusDays(1);
            }
        }

        return dateRange;
    }

    /**
     * 기간에 맞게 날짜 정규화
     * <p>주별 집계의 경우 해당 주의 월요일로, 월별 집계의 경우 해당 월의 1일로 변환합니다.
     *
     * @param date   정규화할 날짜
     * @param period 집계 기간 (weekly/monthly)
     * @return 정규화된 날짜
     */
    public static LocalDate normalizeDateForPeriod(LocalDate date, String period) {
        if ("weekly".equals(period)) {
            // 주 단위 집계 (월요일 기준)
            return date.minusDays(date.getDayOfWeek().getValue() - 1);
        } else if ("monthly".equals(period)) {
            // 월 단위 집계 (월의 첫 날)
            return date.withDayOfMonth(1);
        } else {
            // 일 단위 집계 (변환 없음)
            return date;
        }
    }
}
