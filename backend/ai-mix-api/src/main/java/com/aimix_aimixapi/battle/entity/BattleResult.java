package com.aimix_aimixapi.battle.entity;

/**
 * 배틀 결과 타입
 * 배틀의 완료 여부와 정답률을 기반으로 판정되는 결과
 */
public enum BattleResult {
    /**
     * 승리
     * 정답률 80% 이상
     */
    WIN,

    /**
     * 무승부
     * 정답률 50% 이상 80% 미만
     */
    DRAW,

    /**
     * 패배
     * 정답률 50% 미만
     */
    LOSE,

    /**
     * 진행 중
     * 미완료 또는 답변이 없는 경우
     */
    IN_PROGRESS;

    /**
     * 문자열을 BattleResult로 변환
     * 
     * @param value 문자열 값
     * @return BattleResult enum, 매칭되는 값이 없으면 null
     */
    public static BattleResult fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
