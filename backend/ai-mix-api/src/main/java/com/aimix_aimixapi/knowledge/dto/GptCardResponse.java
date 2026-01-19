package com.aimix_aimixapi.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * GPT 카드 생성 응답 DTO
 * GPT API로부터 받은 카드 생성 응답을 파싱하기 위한 DTO
 * <p>JSON 형식 예시:
 * <pre>{@code
 * {
 *   "title": "개념 제목",
 *   "oneLineDefinition": "한 줄 정의",
 *   "corePoints": ["핵심 포인트 1", "핵심 포인트 2"],
 *   "commonMistakes": ["자주 틀리는 오해 1"]
 * }
 * }</pre>
 * </p>
 *
 * @since 2025-12-18
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptCardResponse {

    /**
     * 카드 제목
     */
    @JsonProperty("title")
    private String title;

    /**
     * 한 줄 정의
     */
    @JsonProperty("oneLineDefinition")
    private String oneLineDefinition;

    /**
     * 핵심 포인트 목록
     */
    @JsonProperty("corePoints")
    private List<String> corePoints;

    /**
     * 자주 틀리는 오해 목록 (선택)
     */
    @JsonProperty("commonMistakes")
    private List<String> commonMistakes;
}
