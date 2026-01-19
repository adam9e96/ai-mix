package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 생성 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleCreateResponse {
    /**
     * 배틀 ID
     */
    private UUID battleId;

    /**
     * 배틀 출제 소스 타입
     */
    private String sourceType;

    /**
     * 배틀 출제 소스 ID (세션 ID)
     */
    private UUID sourceId;

    /**
     * 배틀 난이도 등급
     */
    private String level;

    /**
     * 전체 문제 수
     */
    private Integer totalQuestions;


    /**
     * 문제 목록
     */
    private List<BattleQuestionDto> questions;
}

