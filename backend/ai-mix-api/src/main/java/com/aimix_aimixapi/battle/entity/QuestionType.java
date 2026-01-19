package com.aimix_aimixapi.battle.entity;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * 문제 유형을 나타내는 enum 클래스
 * 배틀 시스템에서 사용되는 문제의 유형을 정의
 */
public enum QuestionType {
    /**
     * 주관식 문제 유형
     * 사용자가 직접 답을 입력하는 형태의 문제
     * 알 수 없는 값이거나 null인 경우 기본값으로 사용
     */
    @JsonEnumDefaultValue
    SUBJECTIVE,

    /**
     * 객관식 문제 유형
     * 여러 선택지 중에서 답을 선택하는 형태의 문제
     */
    OBJECTIVE
}