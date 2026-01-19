package com.aimix_aimixapi.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 개념 카드 수정 요청 DTO
 */
@Getter
@Setter
public class KnowledgeCardUpdateRequest {

    /**
     * 카드 제목 (선택적, null이면 수정하지 않음)
     */
    private String title;

    /**
     * 한 줄 정의
     */
    @NotBlank(message = "한 줄 정의는 필수입니다")
    private String oneLineDefinition;

    /**
     * 핵심 포인트 목록
     */
    private List<String> corePoints;

    /**
     * 자주 틀리는 오해 목록
     */
    private List<String> commonMistakes;

    /**
     * 관련 개념 ID 목록
     */
    private List<Long> relatedConcepts;

    /**
     * 공개 여부 (선택적, null이면 수정하지 않음)
     */
    private Boolean isPublished;
}