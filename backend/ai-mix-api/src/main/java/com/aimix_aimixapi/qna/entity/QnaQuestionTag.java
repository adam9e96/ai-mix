package com.aimix_aimixapi.qna.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * QnA 질문-태그 매핑 엔티티
 * qna_question과 qna_tag 간의 N:N 관계를 나타내는 매핑 테이블
 */
@Entity
@Table(name = "qna_question_tag", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "tag_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(QnaQuestionTag.PK.class)
public class QnaQuestionTag {

    /**
     * 질문 ID (복합 키의 일부)
     */
    @Id
    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    /**
     * 태그 ID (복합 키의 일부)
     */
    @Id
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    /**
     * 질문과의 다대일 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QnaQuestion question;

    /**
     * 태그와의 다대일 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private QnaTag tag;

    /**
     * 복합 키 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private UUID questionId;
        private Long tagId;
    }
}

