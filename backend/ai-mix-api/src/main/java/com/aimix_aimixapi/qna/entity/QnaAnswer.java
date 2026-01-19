package com.aimix_aimixapi.qna.entity;

import com.aimix_aimixapi.common.uuid.UuidV7;
import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * QnA 답변 엔티티
 * 질문에 대한 답변을 저장 (유저 또는 AI가 작성)
 */
@Entity
@Table(name = "qna_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaAnswer {

    @Id
    @UuidV7
    private UUID id;

    /**
     * 답변이 속한 질문
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QnaQuestion question;

    /**
     * 답변을 작성한 사용자
     * AI 답변일 경우 NULL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 답변 타입: 'USER', 'AI'
     * PostgreSQL answer_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "answer_type", nullable = false, columnDefinition = "answer_type_enum")
    private AnswerType answerType;

    /**
     * 답변 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * 점수 (upvote - downvote)
     * Stack Overflow 스타일: 통합 점수 필드
     * 기본값은 0
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    /**
     * 채택 여부
     * 질문 작성자가 선택한 답변인지 여부를 나타냅니다.
     */
    @Column(name = "is_accepted", nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    /**
     * 답변이 작성된 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 채택 여부 확인
     */
    public boolean isAccepted() {
        return Boolean.TRUE.equals(this.isAccepted);
    }

    /**
     * 채택 상태 토글
     *
     * @return 토글 후 채택 여부
     */
    public boolean toggleAcceptStatus() {
        boolean currentlyAccepted = isAccepted();
        this.isAccepted = !currentlyAccepted;
        return !currentlyAccepted;
    }

    /**
     * 채택 해제
     */
    public void unaccept() {
        this.isAccepted = false;
    }

    /**
     * AI 답변 여부 확인
     */
    public boolean isAiAnswer() {
        return this.answerType == AnswerType.AI;
    }

    /**
     * 사용자 답변 여부 확인
     */
    public boolean isUserAnswer() {
        return this.answerType == AnswerType.USER;
    }

    /**
     * 양수 점수 여부 확인
     */
    public boolean hasPositiveScore() {
        return this.score != null && this.score > 0;
    }

    /**
     * 답변 내용 업데이트
     */
    public void updateBody(String body) {
        if (body != null && !body.isBlank()) {
            this.body = body;
        }
    }
}