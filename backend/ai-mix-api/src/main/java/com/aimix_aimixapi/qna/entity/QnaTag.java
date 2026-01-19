package com.aimix_aimixapi.qna.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * QnA 태그 엔티티
 * 질문에 부여할 태그 정보를 저장
 */
@Entity
@Table(name = "qna_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaTag {

    /**
     * 기본 키 (SERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 태그 이름 (유니크)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * QnaTag 기준 - QnaQuestionTag와 1:N 연관관계
     */
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaQuestionTag> questionTags = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * QnaQuestionTag를 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addQuestionTag(QnaQuestionTag questionTag) {
        questionTags.add(questionTag);
        questionTag.setTag(this);
        questionTag.setTagId(this.id);
    }
}