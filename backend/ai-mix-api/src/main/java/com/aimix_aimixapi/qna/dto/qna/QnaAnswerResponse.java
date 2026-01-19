package com.aimix_aimixapi.qna.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * QnA 답변 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaAnswerResponse {

    /**
     * 답변 ID
     */
    private UUID id;

    /**
     * 질문 ID
     */
    private UUID questionId;

    /**
     * 작성자 닉네임 (AI 답변일 경우 "AI")
     */
    private String authorNickname;

    /**
     * 작성자 프로필 아바타 이미지 URL
     * - 작성자가 유저이고 프로필이 있을 경우에만 값이 있음
     * - AI 답변이거나 프로필이 없으면 null
     */
    private String authorAvatarUrl;

    /**
     * 답변 타입: 'USER' 또는 'AI'
     */
    private String answerType;

    /**
     * 답변 내용
     */
    private String body;

    /**
     * 점수 (upvote - downvote)
     * Stack Overflow 스타일: 통합 점수 필드
     */
    private Integer score;

    /**
     * 채택 여부
     * 질문 작성자가 선택한 답변인지 여부
     */
    private Boolean isAccepted;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;
}
