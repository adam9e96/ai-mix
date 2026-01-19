package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.dto.BattleEvaluation;
import com.aimix_aimixapi.battle.dto.BattleStatistics;
import com.aimix_aimixapi.battle.dto.QuestionResult;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 배틀 평가 서비스
 * <p>
 * 배틀 결과를 기반으로 종합적인 평가를 생성하는 서비스
 * <p>
 * 주요 기능:
 * - 등급 결정 (S, A+, A, B+, B, C+, C, D, F)
 * - 종합 평가 코멘트 생성
 * - 강점/약점 분석
 * - 다음 학습 추천
 *
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleEvaluationService {

    private final com.aimix_aimixapi.battle.config.BattleProperties battleProperties;
    private final BattleStatisticsService battleStatisticsService;

    /**
     * 배틀 평가 생성
     * <p>
     * 배틀 통계와 문제별 결과를 기반으로 종합적인 평가를 생성
     * <p>
     * 평가 항목:
     * - 등급 및 등급 설명
     * - 종합 평가 코멘트
     * - 강점 분석
     * - 약점 분석
     * - 다음 학습 추천
     *
     * @param battle          배틀 엔티티
     * @param statistics      배틀 통계
     * @param questionResults 문제별 결과 목록
     * @return 배틀 평가 정보
     * @since 2025-12-18
     */
    public BattleEvaluation evaluateBattle(Battle battle, BattleStatistics statistics,
            List<QuestionResult> questionResults) {

        double avgScore = statistics.getAverageScore();
        String battleLevel = battle.getLevel();

        // 등급 결정
        String grade = determineGrade(avgScore);
        String gradeDescription = getGradeDescription(grade);

        // 종합 평가 코멘트
        String comment = generateComment(statistics, battleLevel);

        // 강점/약점 분석
        String strengths = analyzeStrengths(questionResults);
        String weaknesses = analyzeWeaknesses(questionResults);

        // 다음 학습 추천
        String recommendation = generateRecommendation(statistics, battleLevel);

        return BattleEvaluation.builder().grade(grade).gradeDescription(gradeDescription).comment(comment)
                .strengths(strengths).weaknesses(weaknesses).recommendation(recommendation).build();
    }

    /**
     * 등급 결정
     * <p>
     * 평균 점수에 따라 등급을 결정
     * <p>
     * 등급 기준:
     * - 95점 이상: S
     * - 90점 이상: A+
     * - 85점 이상: A
     * - 80점 이상: B+
     * - 75점 이상: B
     * - 70점 이상: C+
     * - 65점 이상: C
     * - 60점 이상: D
     * - 60점 미만: F
     *
     * @param avgScore 평균 점수
     * @return 등급 (S, A+, A, B+, B, C+, C, D, F)
     * @since 2025-12-18
     */
    public String determineGrade(double avgScore) {
        if (avgScore >= 95)
            return "S";
        if (avgScore >= 90)
            return "A+";
        if (avgScore >= 85)
            return "A";
        if (avgScore >= 80)
            return "B+";
        if (avgScore >= 75)
            return "B";
        if (avgScore >= 70)
            return "C+";
        if (avgScore >= 65)
            return "C";
        if (avgScore >= 60)
            return "D";
        return "F";
    }

    /**
     * 등급 설명
     * <p>
     * 등급에 대한 친근한 설명을 반환
     *
     * @param grade 등급
     * @return 등급 설명
     * @since 2025-12-18
     */
    public String getGradeDescription(String grade) {
        // Properties에서 등급 설명 가져오기
        return battleProperties.getEvaluation().getGradeDescriptions().getOrDefault(grade, "학습을 계속 해주세요!");
    }

    /**
     * 종합 평가 코멘트 생성
     * <p>
     * 배틀 통계를 기반으로 종합적인 평가 코멘트를 생성
     *
     * @param statistics  배틀 통계
     * @param battleLevel 배틀 난이도 등급 (S, A, B)
     * @return 종합 평가 코멘트
     * @since 2025-12-18
     */
    public String generateComment(BattleStatistics statistics, String battleLevel) {
        double avgScore = statistics.getAverageScore();
        double correctRate = statistics.getCorrectRate();

        StringBuilder comment = new StringBuilder();

        // 배틀 레벨별 평가
        comment.append(String.format("난이도 %s 배틀에서 ", battleLevel));

        if (avgScore >= 90) {
            comment.append(battleProperties.getEvaluation().getCommentHigh());
        } else if (avgScore >= 80) {
            comment.append(battleProperties.getEvaluation().getCommentGood());
        } else if (avgScore >= 70) {
            comment.append(battleProperties.getEvaluation().getCommentFair());
        } else {
            comment.append(battleProperties.getEvaluation().getCommentLow());
        }

        comment.append(String.format(battleProperties.getEvaluation().getCommentFormat(), avgScore, correctRate));

        return comment.toString();
    }

    /**
     * 강점 분석
     * <p>
     * 정답한 문제들을 분석하여 학습자의 강점을 파악
     * <p>
     * 분석 항목:
     * - 난이도별 정답 개수 (HARD, MEDIUM)
     * - 문제 유형별 정답 개수 (주관식, 객관식)
     *
     * @param questionResults 문제별 결과 목록
     * @return 강점 분석 결과
     * @since 2025-12-18
     */
    public String analyzeStrengths(List<QuestionResult> questionResults) {
        // 잘 푼 문제들의 난이도와 유형 분석
        List<QuestionResult> correctAnswers = questionResults.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsAnswered()) && q.getScore() != null
                        && q.getScore() >= battleStatisticsService.getCorrectScoreThreshold())
                .toList();

        if (correctAnswers.isEmpty()) {
            return "더 많은 연습을 통해 강점을 발견해보세요!";
        }

        // 난이도별 정답 개수
        long hardCorrect = correctAnswers.stream().filter(q -> "HARD".equals(q.getDifficulty())).count();
        long mediumCorrect = correctAnswers.stream().filter(q -> "MEDIUM".equals(q.getDifficulty())).count();

        StringBuilder strengths = new StringBuilder();

        if (hardCorrect > 0) {
            strengths.append(String.format("어려운 문제 %d개를 정확히 해결하셨어요! ", hardCorrect));
        } else if (mediumCorrect > 0) {
            strengths.append(String.format("중급 문제 %d개를 잘 해결하셨어요! ", mediumCorrect));
        }

        // 유형별 분석
        long subjectiveCorrect = correctAnswers.stream().filter(q -> q.getQuestionType() == QuestionType.SUBJECTIVE)
                .count();
        long objectiveCorrect = correctAnswers.stream().filter(q -> q.getQuestionType() == QuestionType.OBJECTIVE)
                .count();

        if (subjectiveCorrect > objectiveCorrect) {
            strengths.append("주관식 문제에 강점을 보이시네요!");
        } else if (objectiveCorrect > subjectiveCorrect) {
            strengths.append("객관식 문제에 강점을 보이시네요!");
        }

        return !strengths.isEmpty() ? strengths.toString() : "꾸준한 학습으로 강점을 발견해보세요!";
    }

    /**
     * 약점 분석
     * <p>
     * 오답한 문제들을 분석하여 학습자의 약점을 파악
     * <p>
     * 분석 항목:
     * - 난이도별 오답 개수 (HARD, MEDIUM, EASY)
     * - 문제 유형별 오답 개수 (주관식, 객관식)
     *
     * @param questionResults 문제별 결과 목록
     * @return 약점 분석 결과
     * @since 2025-12-18
     */
    public String analyzeWeaknesses(List<QuestionResult> questionResults) {
        // 틀린 문제들의 난이도와 유형 분석
        List<QuestionResult> incorrectAnswers = questionResults.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsAnswered()) && q.getScore() != null
                        && q.getScore() < battleStatisticsService.getCorrectScoreThreshold())
                .toList();

        if (incorrectAnswers.isEmpty()) {
            return "취약점이 발견되지 않았습니다. 완벽해요!";
        }

        // 난이도별 오답 개수
        long hardIncorrect = incorrectAnswers.stream().filter(q -> "HARD".equals(q.getDifficulty())).count();
        long mediumIncorrect = incorrectAnswers.stream().filter(q -> "MEDIUM".equals(q.getDifficulty())).count();
        long easyIncorrect = incorrectAnswers.stream().filter(q -> "EASY".equals(q.getDifficulty())).count();

        StringBuilder weaknesses = new StringBuilder();

        if (hardIncorrect > 0) {
            weaknesses.append(String.format("어려운 문제 %d개를 틀리셨어요. ", hardIncorrect));
        }
        if (easyIncorrect > 0) {
            weaknesses.append(String.format("기초 문제 %d개를 놓치셨어요. 기본 개념 복습이 필요해요. ", easyIncorrect));
        } else if (mediumIncorrect > 0) {
            weaknesses.append(String.format("중급 문제 %d개를 틀리셨어요. ", mediumIncorrect));
        }

        // 유형별 분석
        long subjectiveIncorrect = incorrectAnswers.stream().filter(q -> q.getQuestionType() == QuestionType.SUBJECTIVE)
                .count();
        long objectiveIncorrect = incorrectAnswers.stream().filter(q -> q.getQuestionType() == QuestionType.OBJECTIVE)
                .count();

        if (subjectiveIncorrect > objectiveIncorrect) {
            weaknesses.append("주관식 문제에서 더 많은 연습이 필요해요.");
        } else if (objectiveIncorrect > subjectiveIncorrect) {
            weaknesses.append("객관식 문제에서 더 많은 연습이 필요해요.");
        }

        return weaknesses.toString();
    }

    /**
     * 다음 학습 추천
     * <p>
     * 배틀 통계와 난이도를 기반으로 다음 학습 방향을 추천
     *
     * @param statistics  배틀 통계
     * @param battleLevel 배틀 난이도 등급 (S, A, B)
     * @return 학습 추천 내용
     * @since 2025-12-18
     */
    public String generateRecommendation(BattleStatistics statistics, String battleLevel) {
        double avgScore = statistics.getAverageScore();

        if (avgScore >= 90) {
            // 더 높은 난이도 추천
            if ("B".equals(battleLevel)) {
                return battleProperties.getEvaluation().getRecommendationMedium();
            } else if ("A".equals(battleLevel)) {
                return battleProperties.getEvaluation().getRecommendationHigh();
            } else {
                return battleProperties.getEvaluation().getRecommendationPerfect();
            }
        } else if (avgScore >= 70) {
            return String.format(battleProperties.getEvaluation().getRecommendationNextLevel(), battleLevel);
        } else {
            return battleProperties.getEvaluation().getRecommendationBasic();
        }
    }
}
