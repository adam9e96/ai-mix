package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 배틀 목록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleListResponse {
    /**
     * 배틀 목록
     */
    private List<BattleListItem> battles;

    /**
     * 전체 배틀 개수
     */
    private Long totalCount;
}

