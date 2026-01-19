package com.aimix_aimixapi.qna.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * QnA 질문 상세 응답 DTO (질문 + 답변 목록 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaQuestionDetailResponse {

    /**
     * 질문 정보
     */
    private QnaQuestionResponse question;

    /**
     * 답변 목록
     */
    private List<QnaAnswerResponse> answers;

    /**
     * GPT 답변 여부
     * 배틀 카드 생성 가능 여부를 판단하는 데 사용됩니다.
     * true인 경우에만 배틀 카드를 생성할 수 있습니다.
     */
    private Boolean hasGptAnswer;

    /**
     * 지식카드 생성 여부
     * 해당 QNA 게시글에서 지식카드가 생성되었는지 여부를 나타냅니다.
     * true인 경우 이미 지식카드가 생성된 것입니다.
     */
    private Boolean hasKnowledgeCard;
}

