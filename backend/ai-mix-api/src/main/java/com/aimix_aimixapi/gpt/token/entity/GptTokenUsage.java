package com.aimix_aimixapi.gpt.token.entity;

import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * GPT 토큰 사용량 엔티티
 * 사용자별, 날짜별, 사용 유형별 GPT 토큰 사용량을 추적합니다.
 * Bucket4j 제한 및 요금제 정책 적용을 위한 기초 데이터로 활용됩니다.
 */
@Entity
@Table(name = "gpt_token_usage", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, usage_date"),
        @Index(name = "idx_usage_date", columnList = "usage_date"),
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_user_date_type_key", columnList = "user_id, usage_date, usage_type, is_user_api_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class GptTokenUsage {

    /**
     * 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자와의 다대일 관계
     * gpt_token_usage.user_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 사용 날짜 (날짜별 집계용)
     */
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    /**
     * 사용 유형
     * CHAT: 채팅, QNA: QnA 답변 생성, BATTLE_QUESTION: 배틀 문제 생성,
     * BATTLE_SCORING: 배틀 답변 채점, KNOWLEDGE_CARD: 지식 카드 생성
     * PostgreSQL gpt_usage_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "usage_type", nullable = false, columnDefinition = "gpt_usage_type_enum")
    private GptUsageType usageType;

    /**
     * 사용한 모델명 (예: gpt-4o-mini)
     */
    @Column(name = "model", length = 50)
    private String model;

    /**
     * 프롬프트 토큰 수 (입력)
     */
    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private Integer promptTokens = 0;

    /**
     * 완료 토큰 수 (출력)
     */
    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private Integer completionTokens = 0;

    /**
     * 총 토큰 수 (promptTokens + completionTokens)
     */
    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private Integer totalTokens = 0;

    /**
     * API 호출 횟수 (같은 날짜, 같은 유형의 호출 횟수)
     */
    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private Integer requestCount = 1;

    /**
     * 사용자 API 키 사용 여부
     * true: 사용자 개인 API 키 사용, false: 공용(기본) API 키 사용
     * 기존 데이터와의 호환성을 위해 nullable로 설정 (기존 데이터는 null로 간주)
     */
    @Column(name = "is_user_api_key")
    @Builder.Default
    private Boolean isUserApiKey = false;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 마지막 업데이트 시각
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 수정 시각 업데이트
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
