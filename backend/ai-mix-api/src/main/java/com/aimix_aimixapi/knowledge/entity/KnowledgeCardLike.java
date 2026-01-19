package com.aimix_aimixapi.knowledge.entity;

import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 개념 카드 좋아요 엔티티
 * 사용자가 카드에 좋아요한 기록을 저장 (중복 방지)
 */
@Entity
@Table(name = "knowledge_card_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"card_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"card", "user"})
public class KnowledgeCardLike {

    /**
     * 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 카드 ID
     */
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 카드와의 다대일 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", insertable = false, updatable = false)
    private KnowledgeCard card;

    /**
     * 사용자와의 다대일 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
