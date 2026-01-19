package com.aimix_aimixapi.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 지식 맵 관계 엔티티
 * 개념 카드 간의 관계를 정의
 */
@Entity
@Table(name = "knowledge_map",
        uniqueConstraints = @UniqueConstraint(columnNames = {"from_card_id", "to_card_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"fromCard", "toCard"})
public class KnowledgeMap {

    /**
     * 기본 키 (BIGSERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 출발 카드 (관계의 시작점)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private KnowledgeCard fromCard;

    /**
     * 도착 카드 (관계의 끝점)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private KnowledgeCard toCard;

    /**
     * 관계 타입
     * PREREQUISITE: 선행 개념 (예: HTTP → REST)
     * RELATED: 관련 개념 (예: REST ↔ GraphQL)
     * PART_OF: 포함 관계 (예: HTTP Method → REST)
     * PostgreSQL relationship_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "relationship_type", nullable = false, columnDefinition = "relationship_type_enum")
    private RelationshipType relationshipType;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}