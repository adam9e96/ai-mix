package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 배틀 전적 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleHistoryResponse {
    /**
     * 전체 전적 통계
     */
    private BattleHistoryStatistics statistics;

    /**
     * 배틀 전적 목록 (최신순)
     */
    private List<BattleHistoryItem> battles;

    /**
     * 전체 배틀 개수
     */
    private Long totalCount;
}

