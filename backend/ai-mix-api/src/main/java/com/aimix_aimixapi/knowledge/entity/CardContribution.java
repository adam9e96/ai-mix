package com.aimix_aimixapi.knowledge.entity;

import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 카드 기여 이력 엔티티
 * 개념 카드에 대한 사용자 기여 내역을 기록
 */
@Entity
@Table(name = "card_contribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"card", "contributor"})
public class CardContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 관련 개념 카드
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private KnowledgeCard card;

    /**
     * 기여자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id", nullable = false)
    private User contributor;

    /**
     * 기여 타입
     * CREATE: 카드 생성
     * UPDATE: 카드 수정
     * MISTAKE_REPORT: 오답 보고
     * PostgreSQL contribution_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "contribution_type", nullable = false, columnDefinition = "contribution_type_enum")
    private ContributionType contributionType;

    /**
     * 기여 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}