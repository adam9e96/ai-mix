package com.aimix_aimixapi.battle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 배틀 답변 제출 요청 DTO
 */
@Getter
@Setter
public class BattleAnswerSubmitRequest {

    /**
     * 배틀 ID
     */
    @NotNull(message = "배틀 ID는 필수입니다")
    private UUID battleId;

    /**
     * 문제 ID
     */
    @NotNull(message = "문제 ID는 필수입니다")
    private UUID questionId;

    /**
     * 사용자가 제출한 답변
     * - 주관식: 텍스트 답변
     * - 객관식: 숫자 (1, 2, 3, 4)
     */
    @NotBlank(message = "답변은 필수입니다")
    private String userAnswer;
}

