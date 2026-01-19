package com.aimix_aimixapi.gpt.token.repository;

import com.aimix_aimixapi.gpt.token.entity.GptTokenUsage;
import com.aimix_aimixapi.gpt.token.entity.GptUsageType;
import com.aimix_aimixapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GptTokenUsageRepository extends JpaRepository<GptTokenUsage, Long> {

    /**
     * 사용자, 날짜, 유형, API 키 타입으로 토큰 사용량 조회 (집계용)
     * <p>같은 날짜, 같은 유형, 같은 API 키 타입의 기존 레코드를 조회하여 집계에 사용
     *
     * <p><b>용도:</b>
     * <ul>
     *   <li>토큰 사용량 기록 시 기존 레코드가 있는지 확인</li>
     *   <li>기존 레코드가 있으면 업데이트(집계), 없으면 새로 생성</li>
     *   <li>사용자 API 키와 공용 키를 구분하여 별도로 집계</li>
     * </ul>
     *
     * <p><b>집계 기준:</b>
     * <ul>
     *   <li>같은 사용자(user)</li>
     *   <li>같은 날짜(usageDate)</li>
     *   <li>같은 사용 유형(usageType: CHAT, QNA, BATTLE_QUESTION 등)</li>
     *   <li>같은 API 키 타입(isUserApiKey: true=사용자 키, false=공용 키)</li>
     * </ul>
     *
     * <p><b>주의사항:</b>
     * <ul>
     *   <li>isUserApiKey가 null인 경우 false로 처리 (기존 데이터 호환성)</li>
     *   <li>레코드가 없으면 Optional.empty() 반환</li>
     *   <li>같은 조건의 레코드는 하나만 존재해야 함 (유니크 제약 조건)</li>
     * </ul>
     *
     * @param user         조회할 사용자 엔티티 (null이면 예외 발생 가능)
     * @param usageDate    조회할 날짜 (null 불가)
     * @param usageType    조회할 사용 유형 (null 불가)
     * @param isUserApiKey API 키 타입 (true: 사용자 키, false: 공용 키, null: false로 처리)
     * @return 토큰 사용량 엔티티 (없으면 Optional.empty())
     */
    Optional<GptTokenUsage> findByUserAndUsageDateAndUsageTypeAndIsUserApiKey(
            User user, LocalDate usageDate, GptUsageType usageType, Boolean isUserApiKey);

    /**
     * 사용자별 특정 날짜의 총 토큰 사용량 합계 조회
     * <p>특정 사용자의 특정 날짜에 기록된 모든 토큰 사용량의 총합을 계산
     *
     * <p><b>동작 방식:</b>
     * <ul>
     *   <li>해당 날짜의 모든 레코드의 totalTokens 필드를 합산</li>
     *   <li>사용자 API 키와 공용 키 사용량을 구분하지 않고 모두 합산</li>
     *   <li>데이터가 없으면 0을 반환 (COALESCE 사용)</li>
     *   <li>같은 날짜에 여러 사용 유형(CHAT, QNA 등)이 있어도 모두 합산</li>
     * </ul>
     *
     * <p><b>사용 예시:</b>
     * <pre>{@code
     * // 오늘 날짜의 총 토큰 사용량 조회
     * Long todayTokens = repository.sumTotalTokensByUserAndDate(user, LocalDate.now());
     *
     * LocalDate targetDate = LocalDate.of(2026, 1, 15);
     * Long tokens = repository.sumTotalTokensByUserAndDate(user, targetDate);
     * }</pre>
     * @param user 조회할 사용자 엔티티 (null이면 예외 발생 가능)
     * @param date 조회할 날짜 (null 불가)
     * @return 해당 날짜의 총 토큰 사용량 합계 (데이터가 없으면 0)
     */
    @Query("SELECT COALESCE(SUM(gtu.totalTokens), 0) FROM GptTokenUsage gtu " +
            "WHERE gtu.user = :user AND gtu.usageDate = :date")
    Long sumTotalTokensByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    /**
     * 사용자별 특정 기간의 총 토큰 사용량 합계 조회
     * <p>특정 사용자의 특정 날짜 범위 내 모든 토큰 사용량의 총합을 계산
     *
     * <p><b>동작 방식:</b>
     * <ul>
     *   <li>기간 내 모든 레코드의 totalTokens 필드를 합산</li>
     *   <li>사용자 API 키와 공용 키 사용량을 구분하지 않고 모두 합산</li>
     *   <li>데이터가 없으면 0을 반환 (COALESCE 사용)</li>
     * </ul>
     *
     * <p><b>날짜 범위:</b>
     * <ul>
     *   <li>BETWEEN 연산자 사용 (시작일과 종료일 모두 포함)</li>
     *   <li>예: startDate=2026-01-01, endDate=2026-01-31이면 양쪽 날짜 모두 포함</li>
     *   <li>주의: startDate가 endDate보다 늦으면 0 반환</li>
     * </ul>
     *
     * @param user      조회할 사용자 엔티티 (null이면 예외 발생 가능)
     * @param startDate 시작 날짜 (포함, null 불가)
     * @param endDate   종료 날짜 (포함, null 불가)
     * @return 총 토큰 사용량 합계 (데이터가 없으면 0)
     */
    @Query("SELECT COALESCE(SUM(gtu.totalTokens), 0) FROM GptTokenUsage gtu " +
            "WHERE gtu.user = :user AND gtu.usageDate BETWEEN :startDate AND :endDate")
    Long sumTotalTokensByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 사용자별 전체 토큰 사용량 합계 조회
     * <p>특정 사용자의 모든 날짜에 기록된 토큰 사용량의 총합을 계산
     *
     * <p><b>동작 방식:</b>
     * <ul>
     *   <li>해당 사용자의 모든 레코드의 totalTokens 필드를 합산</li>
     *   <li>날짜 제한 없이 전체 기간의 데이터를 조회</li>
     *   <li>사용자 API 키와 공용 키 사용량을 구분하지 않고 모두 합산</li>
     *   <li>데이터가 없으면 0을 반환 (COALESCE 사용)</li>
     *   <li>모든 사용 유형(CHAT, QNA, BATTLE_QUESTION 등)의 사용량을 합산</li>
     * </ul>
     *
     * @param user 조회할 사용자 엔티티 (null이면 예외 발생 가능)
     * @return 전체 기간의 총 토큰 사용량 합계 (데이터가 없으면 0)
     */
    @Query("SELECT COALESCE(SUM(gtu.totalTokens), 0) FROM GptTokenUsage gtu WHERE gtu.user = :user")
    Long sumTotalTokensByUser(@Param("user") User user);

    /**
     * 사용자별 특정 기간의 토큰 사용량 목록 조회
     * <p>특정 사용자의 특정 날짜 범위 내 모든 토큰 사용량 레코드를 조회
     *
     * <p><b>정렬 순서:</b>
     * <ul>
     *   <li>1순위: 사용 날짜 내림차순 (최신순)</li>
     *   <li>2순위: 사용 유형 오름차순 (BATTLE_QUESTION → BATTLE_SCORING → CHAT → KNOWLEDGE_CARD → QNA)</li>
     * </ul>
     *
     * <p><b>날짜 범위:</b>
     * <ul>
     *   <li>BETWEEN 연산자 사용 (시작일과 종료일 모두 포함)</li>
     *   <li>예: startDate=2026-01-01, endDate=2026-01-31이면 양쪽 날짜 모두 포함</li>
     *   <li>주의: startDate가 endDate보다 늦으면 빈 리스트 반환</li>
     * </ul>
     *
     * @param user      조회할 사용자 엔티티 (null이면 예외 발생 가능)
     * @param startDate 시작 날짜 (포함, null 불가)
     * @param endDate   종료 날짜 (포함, null 불가)
     * @return 토큰 사용량 목록 (날짜 내림차순, 사용 유형 오름차순 정렬)
     */
    @Query("SELECT gtu FROM GptTokenUsage gtu " +
            "WHERE gtu.user = :user AND gtu.usageDate BETWEEN :startDate AND :endDate " +
            "ORDER BY gtu.usageDate DESC, gtu.usageType")
    List<GptTokenUsage> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
