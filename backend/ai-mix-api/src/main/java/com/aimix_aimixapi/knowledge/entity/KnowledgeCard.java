package com.aimix_aimixapi.knowledge.entity;

import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 개념 카드 엔티티
 * 지식백과의 핵심 개념을 구조화된 카드 형태로 관리
 */
@Entity
@Table(name = "knowledge_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"contributor", "outgoingRelationships", "incomingRelationships", "contributions"})
public class KnowledgeCard {

    /**
     * 기본 키 (BIGSERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 카드 제목 (유니크)
     * 예: "REST API"
     */
    @Column(nullable = false, unique = true)
    private String title;

    /**
     * URL 친화적 슬러그 (유니크)
     * 예: "rest-api"
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * 한 줄 정의
     */
    @Column(name = "one_line_definition", nullable = false, columnDefinition = "TEXT")
    private String oneLineDefinition;

    /**
     * 핵심 포인트 (JSON 배열)
     * 예: ["Stateless", "Resource 중심", "HTTP Method 활용"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "core_points", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> corePoints = new ArrayList<>();

    /**
     * 자주 틀리는 오해 (JSON 배열)
     * 예: ["REST = JSON ❌", "REST는 프로토콜 ❌"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "common_mistakes", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> commonMistakes = new ArrayList<>();

    /**
     * 관련 개념 ID 목록 (JSON 배열)
     * 예: [2, 5, 8] (다른 카드 ID들)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_concepts", columnDefinition = "jsonb")
    @Builder.Default
    private List<Long> relatedConcepts = new ArrayList<>();

    /**
     * 출처 타입
     * QNA, CHAT, BATTLE
     * PostgreSQL source_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "source_type", columnDefinition = "source_type_enum")
    private SourceType sourceType;

    /**
     * 출처 ID (UUID)
     * sourceType에 따라 QNA 질문 ID, CHAT 세션 ID, BATTLE ID 등
     */
    @Column(name = "source_id")
    private UUID sourceId;

    /**
     * 기여자 (카드를 생성한 사용자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id")
    private User contributor;

    /**
     * 조회수
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 추천 수
     */
    @Column(name = "upvote_count", nullable = false)
    @Builder.Default
    private Long upvoteCount = 0L;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 공개 여부
     */
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = true;

    /**
     * KnowledgeCard 기준 - KnowledgeMap과 1:N 연관관계 (from_card_id)
     */
    @OneToMany(mappedBy = "fromCard", fetch = FetchType.LAZY)
    @Builder.Default
    private List<KnowledgeMap> outgoingRelationships = new ArrayList<>();

    /**
     * KnowledgeCard 기준 - KnowledgeMap과 1:N 연관관계 (to_card_id)
     */
    @OneToMany(mappedBy = "toCard", fetch = FetchType.LAZY)
    @Builder.Default
    private List<KnowledgeMap> incomingRelationships = new ArrayList<>();

    /**
     * KnowledgeCard 기준 - CardContribution과 1:N 연관관계
     */
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CardContribution> contributions = new ArrayList<>();

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 추천 수 증가
     */
    public void incrementUpvoteCount() {
        this.upvoteCount++;
    }

    /**
     * 추천 수 감소
     */
    public void decrementUpvoteCount() {
        if (this.upvoteCount > 0) {
            this.upvoteCount--;
        }
    }

}