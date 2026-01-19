package com.aimix_aimixapi.battle.entity;

import com.aimix_aimixapi.common.uuid.UuidV7;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 배틀 답변 엔티티
 * 사용자가 제출한 배틀 문제에 대한 답변을 관리
 */
@Entity
@Table(name = "battle_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"battle", "question"})
public class BattleAnswer {

    @Id
    @UuidV7
    private UUID id;

    /**
     * Battle과의 다대일 관계
     * battle_answer.battle_id → battle.id
     * 연관관계의 주인 (외래키 보유)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    /**
     * BattleQuestion과의 다대일 관계
     * battle_answer.question_id → battle_question.id
     * 연관관계의 주인 (외래키 보유)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private BattleQuestion question;

    /**
     * 사용자가 제출한 답변
     */
    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    /**
     * 해당 문제에 대한 점수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    /**
     * AI의 피드백
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;
    /**
     * ⭐ 답변 제출 시각 (추가)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}

