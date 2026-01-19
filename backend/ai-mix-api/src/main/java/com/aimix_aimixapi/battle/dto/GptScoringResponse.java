package com.aimix_aimixapi.battle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GPT 채점 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptScoringResponse {

    /**
     * 채점 점수
     */
    @JsonProperty("score")
    private Integer score;

    /**
     * 채점 피드백
     */
    @JsonProperty("feedback")
    private String feedback;

    /**
     * 정답 여부
     */
    @JsonProperty("isCorrect")
    private Boolean isCorrect;
}

