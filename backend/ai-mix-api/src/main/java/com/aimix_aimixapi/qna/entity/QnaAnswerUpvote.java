package com.aimix_aimixapi.qna.entity;

import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * QnA 답변 추천/비추천 엔티티
 * 사용자가 답변에 추천/비추천한 기록을 저장 (중복 방지)
 */
@Entity
@Table(name = "qna_answer_vote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"answer_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaAnswerUpvote {

    /**
     * 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 답변 ID
     */
    @Column(name = "answer_id", nullable = false)
    private UUID answerId;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long  userId;

    /**
     * 추천 타입: UP(추천), DOWN(비추천)
     * PostgreSQL vote_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "vote_type", nullable = false, columnDefinition = "vote_type_enum")
    private VoteType voteType;

    /**
     * 답변과의 다대일 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", insertable = false, updatable = false)
    private QnaAnswer answer;

    /**
     * 사용자와의 다대일 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}