package com.aimix_aimixapi.qna.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * QnA 질문 목록 응답 DTO (간단한 정보만 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaQuestionListResponse {

    /**
     * 질문 ID
     */
    private UUID id;

    /**
     * 작성자 닉네임 (익명일 경우 null)
     */
    private String authorNickname;

    /**
     * 질문 제목
     */
    private String title;

    /**
     * 질문 내용 미리보기 (일부만)
     */
    private String bodyPreview;

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

