package com.aimix_aimixapi.qna.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * QnA 질문 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaQuestionResponse {

    /**
     * 질문 ID
     */
    private UUID id;

    /**
     * 작성자 닉네임 (익명일 경우 null)
     */
    private String authorNickname;

    /**
     * 작성자 프로필 아바타 이미지 URL
     * - 작성자가 유저이고 프로필이 있을 경우에만 값이 있음
     * - 익명 질문이거나 프로필이 없으면 null
     */
    private String authorAvatarUrl;

    /**
     * 질문 제목
     */
    private String title;

    /**
     * 질문 내용
     */
    private String body;

    /**
     * 익명 여부
     */
    private Boolean isAnonymous;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 태그 목록
     */
    private List<String> tags;

    /**
     * 답변 수
     */
    private Integer answerCount;

    /**
     * 조회수
     */
    private Long viewCount;
}

