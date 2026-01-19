package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 배틀 전적 통계 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleHistoryStatistics {
    /**
     * 총 배틀 수
     */
    private Long totalBattles;

    /**
     * 승리 수
     */
    private Long winCount;

    /**
     * 무승부 수
     */
    private Long drawCount;

    /**
     * 패배 수
     */
    private Long loseCount;

    /**
     * 진행 중인 배틀 수
     */
    private Long inProgressCount;

    /**
     * 승률 (%)
     */
    private Double winRate;

    /**
     * 현재 연승 수
     */
    private Integer currentWinStreak;

    /**
     * 최대 연승 수
     */
    private Integer maxWinStreak;

    /**
     * 현재 연패 수
     */
    private Integer currentLoseStreak;

    /**
     * 최대 연패 수
     */
    private Integer maxLoseStreak;
}

