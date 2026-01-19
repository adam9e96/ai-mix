package com.aimix_aimixapi.gpt.token.service.query;

import com.aimix_aimixapi.gpt.token.dto.GptTokenUsageResponse;
import com.aimix_aimixapi.gpt.token.entity.GptTokenUsage;
import com.aimix_aimixapi.gpt.token.mapper.GptTokenUsageMapper;
import com.aimix_aimixapi.gpt.token.repository.GptTokenUsageRepository;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * GPT 토큰 사용량 조회 서비스
 * 토큰 사용량 데이터를 조회하는 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GptTokenUsageQueryService {

    private final GptTokenUsageRepository gptTokenUsageRepository;

    /**
     * 사용자별 오늘의 총 토큰 사용량 조회
     *
     * @param user 조회할 사용자
     * @return 오늘의 총 토큰 사용량 (사용자가 null이면 0)
     */
    @Transactional(readOnly = true)
    public Long getTodayTotalTokens(User user) {
        if (user == null) {
            return 0L;
        }
        return gptTokenUsageRepository.sumTotalTokensByUserAndDate(user, LocalDate.now());
    }

    /**
     * 사용자별 전체 토큰 사용량 조회
     *
     * @param user 조회할 사용자
     * @return 전체 토큰 사용량 (사용자가 null이면 0)
     */
    @Transactional(readOnly = true)
    public Long getTotalTokens(User user) {
        if (user == null) {
            return 0L;
        }
        return gptTokenUsageRepository.sumTotalTokensByUser(user);
    }

    /**
     * 사용자별 날짜별 사용량 목록 조회 (최근 N일)
     * <p>특정 사용자의 최근 N일간의 토큰 사용량 목록을 조회합니다.
     *
     * <p><b>동작 방식:</b>
     * <ul>
     *   <li>오늘을 포함하여 최근 N일간의 데이터를 조회</li>
     *   <li>예: days=30이면 오늘부터 29일 전까지 (총 30일)</li>
     *   <li>결과는 날짜 내림차순, 사용 유형 오름차순으로 정렬</li>
     * </ul>
     *
     * <p><b>사용 예시:</b>
     * <pre>{@code
     * // 최근 30일 사용량 조회
     * List<GptTokenUsage> usageList = queryService.getDailyUsageList(user, 30);
     * }</pre>
     *
     * @param user 조회할 사용자 엔티티 (null이면 빈 리스트 반환)
     * @param days 조회할 일 수 (오늘 포함, 최소 1일)
     * @return 토큰 사용량 목록 (날짜 내림차순, 사용 유형 오름차순 정렬)
     */
    @Transactional(readOnly = true)
    public List<GptTokenUsage> getDailyUsageList(User user, int days) {
        if (user == null) {
            return List.of();
        }
        // 오늘을 포함하여 최근 N일간의 데이터 조회
        // 예: days=30이면 오늘부터 29일 전까지 (총 30일)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return gptTokenUsageRepository.findByUserAndDateRange(user, startDate, endDate);
    }

    /**
     * 사용자별 토큰 사용량 응답 생성
     * <p>사용자의 토큰 사용량을 조회하여 DTO로 변환하여 반환합니다.
     *
     * @param user 조회할 사용자
     * @return 토큰 사용량 응답 DTO
     */
    @Transactional(readOnly = true)
    public GptTokenUsageResponse getTokenUsageResponse(User user) {
        // 오늘의 총 토큰 사용량
        Long todayTotalTokens = getTodayTotalTokens(user);

        // 전체 총 토큰 사용량
        Long totalTokens = getTotalTokens(user);

        // 날짜별 사용량 목록 (최근 30일)
        List<GptTokenUsageResponse.DailyUsage> dailyUsageList = GptTokenUsageMapper
                .toDailyUsageList(getDailyUsageList(user, 30));

        return GptTokenUsageResponse.builder()
                .todayTotalTokens(todayTotalTokens)
                .totalTokens(totalTokens)
                .dailyUsageList(dailyUsageList)
                .build();
    }
}
