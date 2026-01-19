package com.aimix_aimixapi.knowledge.dto;

import com.aimix_aimixapi.knowledge.entity.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * 개념 카드 생성 요청 DTO
 */
@Getter
@Setter
@Builder
public class KnowledgeCardCreateRequest {

    /**
     * 카드 제목
     */
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    /**
     * 한 줄 정의
     */
    @NotBlank(message = "한 줄 정의는 필수입니다")
    private String oneLineDefinition;

    /**
     * 핵심 포인트 목록
     */
    @NotEmpty(message = "핵심 포인트는 최소 1개 이상 필요합니다")
    private List<String> corePoints;

    /**
     * 자주 틀리는 오해 목록 (선택)
     */
    private List<String> commonMistakes;

    /**
     * 관련 개념 ID 목록 (선택)
     */
    private List<Long> relatedConcepts;

    /**
     * 출처 타입 (선택)
     * QNA: QnA에서 생성된 카드
     * CHAT: 챗봇 대화에서 생성된 카드
     * BATTLE: 배틀에서 생성된 카드
     */
    private SourceType sourceType;

    /**
     * 출처 ID (선택)
     */
    private UUID sourceId;

    /**
     * 공개 여부 (선택, 기본값: true)
     * false로 설정하면 비공개 카드로 생성됩니다
     */
    private Boolean isPublished;
}