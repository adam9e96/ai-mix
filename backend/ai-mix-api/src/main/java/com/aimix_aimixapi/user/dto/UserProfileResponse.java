package com.aimix_aimixapi.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * QnA 게시판에서 사용할 간단한 사용자 프로필 정보 DTO
 * - 작성자 클릭 시 표시되는 간단한 정보
 * - 공개 정보만 포함 (이메일, 생년월일 등 민감 정보 제외)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON 응답에서 제외
public class UserProfileResponse {

    /**
     * 사용자 닉네임 (공개 정보)
     */
    private String nickname;

    /**
     * 프로필 아바타 이미지 URL
     * - 프로필 이미지가 없으면 null
     */
    private String avatarUrl;

    /**
     * 자기소개
     * - 자기소개가 없으면 null
     */
    private String bio;

    /**
     * 가입일
     * - 사용자 경험 향상 (가입 기간 표시 등)
     */
    private LocalDateTime createdAt;

    /**
     * QnA 활동 통계 정보
     */
    private QnaStatistics statistics;

    /**
     * QnA 활동 통계 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QnaStatistics {
        /**
         * 작성한 질문 수 (익명 질문 제외)
         */
        private long questionCount;

        /**
         * 작성한 답변 수 (AI 답변 제외)
         */
        private long answerCount;

        /**
         * 채택된 답변 수
         */
        private long acceptedAnswerCount;

        /**
         * 받은 총 점수 (답변의 score 합계)
         */
        private long totalScore;
    }
}
