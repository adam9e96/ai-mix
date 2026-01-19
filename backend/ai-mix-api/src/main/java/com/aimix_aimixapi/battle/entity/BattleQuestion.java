package com.aimix_aimixapi.battle.entity;

import com.aimix_aimixapi.common.uuid.UuidV7;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 배틀 문제 엔티티
 * 배틀에 출제된 개별 문제를 관리
 */
@Entity
@Table(name = "battle_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"battle", "answers"})
public class BattleQuestion {

    /**
     * 기본 키 (UUID v7)
     * uuid-creator 라이브러리를 사용하여 타임스탬프 기반 UUID v7 생성
     */
    @Id
    @UuidV7
    private UUID id;

    /**
     * Battle과의 다대일 관계
     * battle_question.battle_id → battle.id
     * 연관관계의 주인 (외래키 보유)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    /**
     * 문제의 내용
     */
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    /**
     * 문제의 정답
     */
    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    /**
     * 문제의 순서
     */
    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    /**
     * 문제의 난이도
     * EASY, MEDIUM, HARD
     */
    @Column(length = 20)
    private String difficulty;

    /**
     * 문제 유형
     * SUBJECTIVE: 주관식, OBJECTIVE: 객관식
     * PostgreSQL question_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "question_type", nullable = false, columnDefinition = "question_type_enum")
    @Builder.Default
    private QuestionType questionType = QuestionType.SUBJECTIVE;

    /**
     * 객관식 선택지 (JSON 형태로 저장)
     * 예: ["선택지1", "선택지2", "선택지3", "선택지4"]
     * 주관식인 경우 null
     */
    @Column(columnDefinition = "TEXT")
    private String choices;

    /**
     * BattleQuestion 기준 - BattleAnswer와 1:N 연관관계
     * 하나의 문제에 여러 답변이 포함될 수 있음
     * cascade = CascadeType.ALL → 문제 삭제 시 답변도 함께 삭제
     * orphanRemoval = true → 답변 참조 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BattleAnswer> answers = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * BattleAnswer를 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addAnswer(BattleAnswer answer) {
        answers.add(answer);
        answer.setQuestion(this);
    }
}

