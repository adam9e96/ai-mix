package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.config.BattleProperties;
import com.aimix_aimixapi.battle.dto.*;
import com.aimix_aimixapi.battle.message.BattleMessage;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleAnswer;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.entity.BattleResult;
import com.aimix_aimixapi.battle.repository.BattleRepository;
import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import com.aimix_aimixapi.common.util.JsonUtils;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 배틀 조회 서비스
 * - 배틀 목록, 전적, 결과 등 데이터 조회 담당
 * - CQRS 패턴의 Query 담당
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleQueryService {

    private final BattleRepository battleRepository;
    private final UserService userService;
    private final BattleStatisticsService battleStatisticsService;
    private final BattleEvaluationService battleEvaluationService;
    private final JsonUtils jsonUtils;
    private final BattleProperties battleProperties;

    /**
     * 사용자의 배틀 목록 조회 (N+1 최적화)
     * Fetch Join으로 배틀 + 질문 + 답변을 한 번에 로딩하여
     * 기존 배틀 N개당 2N번의 LAZY 로딩 쿼리를 제거
     */
    public BattleListResponse getBattleList(String email) {
        User user = userService.findUserByEmail(email);
        // Fetch Join으로 질문/답변을 한 번에 로딩
        List<Battle> battles = battleRepository.findByUserWithQuestionsAndAnswers(user);

        List<BattleListItem> battleItems = battles.stream()
                .map(battle -> {
                    BattleHistoryItem historyItem = battleStatisticsService.calculateBattleHistoryItem(battle);
                    return BattleListItem.builder()
                            .battleId(battle.getId())
                            .sourceType(battle.getSourceType().name())
                            .level(battle.getLevel())
                            .totalQuestions(historyItem.getTotalQuestions())
                            .answeredQuestions(historyItem.getAnsweredQuestions())
                            .isCompleted(historyItem.getIsCompleted())
                            .averageScore(historyItem.getAverageScore())
                            .createdAt(battle.getCreatedAt())
                            .build();
                })
                .toList();

        return BattleListResponse.builder()
                .battles(battleItems)
                .totalCount((long) battleItems.size())
                .build();
    }

    /**
     * 사용자의 배틀 전적 조회 (N+1 최적화)
     * Fetch Join으로 배틀 + 질문 + 답변을 한 번에 로딩
     */
    public BattleHistoryResponse getBattleHistory(String email) {
        User user = userService.findUserByEmail(email);
        // Fetch Join으로 질문/답변을 한 번에 로딩
        List<Battle> battles = battleRepository.findByUserWithQuestionsAndAnswers(user);

        List<BattleHistoryItem> historyItems = battles.stream()
                .map(battleStatisticsService::calculateBattleHistoryItem)
                .toList();

        BattleHistoryStatistics statistics = battleStatisticsService.calculateHistoryStatistics(historyItems);

        return BattleHistoryResponse.builder()
                .statistics(statistics)
                .battles(historyItems)
                .totalCount(statistics.getTotalBattles())
                .build();
    }

    /**
     * 배틀 결과 조회
     */
    public BattleResultResponse getBattleResult(String email, UUID battleId) {
        User user = userService.findUserByEmail(email);
        Battle battle = findBattleByIdAndUser(battleId, user);

        List<BattleQuestion> sortedQuestions = getSortedQuestions(battle);
        Map<UUID, BattleAnswer> answerMap = getAnswerMap(battle);

        BattleStatistics statistics = battleStatisticsService.calculateStatistics(battle, sortedQuestions, answerMap);
        List<QuestionResult> questionResults = buildQuestionResults(sortedQuestions, answerMap);

        LocalDateTime completedAt = calculateCompletedAt(battle, statistics.getIsCompleted());

        BattleResult battleResult = battleStatisticsService.determineBattleResult(
                statistics.getIsCompleted(),
                statistics.getAnsweredQuestions(),
                statistics.getCorrectRate());

        BattleEvaluation evaluation = battleEvaluationService.evaluateBattle(battle, statistics, questionResults);

        return BattleResultResponse.builder()
                .battleId(battle.getId())
                .sourceType(battle.getSourceType().name())
                .level(battle.getLevel())
                .result(battleResult.name())
                .createdAt(battle.getCreatedAt())
                .completedAt(completedAt)
                .statistics(statistics)
                .questionResults(questionResults)
                .evaluation(evaluation)
                .build();
    }

    /**
     * 배틀 간단 승패 결과 조회
     */
    public BattleSimpleResultResponse getBattleSimpleResult(String email, UUID battleId) {
        User user = userService.findUserByEmail(email);
        Battle battle = findBattleByIdAndUser(battleId, user);

        List<BattleQuestion> questions = battle.getQuestions();
        Map<UUID, BattleAnswer> answerMap = getAnswerMap(battle);

        BattleStatistics statistics = battleStatisticsService.calculateStatistics(battle, questions, answerMap);
        BattleResult battleResult = battleStatisticsService.determineBattleResult(
                statistics.getIsCompleted(),
                statistics.getAnsweredQuestions(),
                statistics.getCorrectRate());

        LocalDateTime completedAt = calculateCompletedAt(battle, statistics.getIsCompleted());

        return BattleSimpleResultResponse.builder()
                .battleId(battle.getId())
                .result(battleResult.name())
                .correctRate(statistics.getCorrectRate())
                .averageScore(statistics.getAverageScore())
                .completedAt(completedAt)
                .build();
    }

    /**
     * 배틀 재개 정보 조회
     */
    public BattleResumeResponse resumeBattle(String email, UUID battleId) {
        User user = userService.findUserByEmail(email);
        Battle battle = findBattleByIdAndUser(battleId, user);

        List<BattleQuestion> sortedQuestions = getSortedQuestions(battle);
        Map<UUID, BattleAnswer> answerMap = getAnswerMap(battle);

        List<BattleQuestionWithProgressDto> questionDtos = buildQuestionWithProgressDtos(sortedQuestions, answerMap);
        int answeredQuestions = countAnsweredQuestions(questionDtos);
        boolean isCompleted = isBattleCompleted(answeredQuestions, battle.getTotalQuestions());
        Integer nextQuestionOrder = calculateNextQuestionOrder(questionDtos, isCompleted);

        return BattleResumeResponse.builder()
                .battleId(battle.getId())
                .sourceType(battle.getSourceType().name())
                .sourceId(battle.getSourceId())
                .level(battle.getLevel())
                .totalQuestions(battle.getTotalQuestions())
                .answeredQuestions(answeredQuestions)
                .isCompleted(isCompleted)
                .nextQuestionOrder(nextQuestionOrder)
                .createdAt(battle.getCreatedAt())
                .questions(questionDtos)
                .build();
    }

    /**
     * 배틀 ID와 사용자로 배틀 조회 (질문/답변 Fetch Join)
     * 단일 쿼리로 배틀 + 질문 + 답변을 한 번에 로딩하여 N+1 방지
     */
    private Battle findBattleByIdAndUser(UUID battleId, User user) {
        return battleRepository.findByIdAndUserWithQuestionsAndAnswers(battleId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        BattleMessage.BATTLE_NOT_FOUND_BY_ID.format(battleId)));
    }

    /**
     * 배틀의 문제 목록을 순서대로 정렬
     */
    private List<BattleQuestion> getSortedQuestions(Battle battle) {
        return battle.getQuestions().stream()
                .sorted((q1, q2) -> q1.getOrderNo().compareTo(q2.getOrderNo()))
                .toList();
    }

    /**
     * 배틀의 답변을 문제 ID로 매핑
     * 동일 문제에 여러 답변이 있는 경우 가장 최근 답변을 사용
     */
    private Map<UUID, BattleAnswer> getAnswerMap(Battle battle) {
        return battle.getAnswers().stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer,
                        (existing, replacement) -> replacement.getCreatedAt().isAfter(existing.getCreatedAt())
                                ? replacement
                                : existing));
    }

    /**
     * 배틀 완료 시각 계산
     */
    private LocalDateTime calculateCompletedAt(Battle battle, boolean isCompleted) {
        if (!isCompleted) {
            return null;
        }
        return battle.getAnswers().stream()
                .map(BattleAnswer::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * 문제 결과 목록 생성
     */
    private List<QuestionResult> buildQuestionResults(List<BattleQuestion> questions,
            Map<UUID, BattleAnswer> answerMap) {
        return questions.stream()
                .map(question -> buildQuestionResult(question, answerMap.get(question.getId())))
                .toList();
    }

    /**
     * 단일 문제 결과 생성
     */
    private QuestionResult buildQuestionResult(BattleQuestion question, BattleAnswer answer) {
        List<String> parsedChoices = parseChoices(question.getChoices());
        boolean isAnswered = answer != null;

        QuestionResult.QuestionResultBuilder builder = QuestionResult.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .orderNo(question.getOrderNo())
                .correctAnswer(question.getCorrectAnswer())
                .difficulty(question.getDifficulty())
                .choices(parsedChoices)
                .isAnswered(isAnswered);

        if (answer != null) {
            int correctScoreThreshold = battleStatisticsService.getCorrectScoreThreshold();
            builder.userAnswer(answer.getUserAnswer())
                    .score(answer.getScore())
                    .feedback(answer.getFeedback())
                    .isCorrect(answer.getScore() >= correctScoreThreshold);
        }

        return builder.build();
    }

    /**
     * 문제 진행 상태 DTO 목록 생성
     */
    private List<BattleQuestionWithProgressDto> buildQuestionWithProgressDtos(
            List<BattleQuestion> questions, Map<UUID, BattleAnswer> answerMap) {
        return questions.stream()
                .map(question -> buildQuestionWithProgressDto(question, answerMap.get(question.getId())))
                .toList();
    }

    /**
     * 단일 문제 진행 상태 DTO 생성
     */
    private BattleQuestionWithProgressDto buildQuestionWithProgressDto(BattleQuestion question, BattleAnswer answer) {
        List<String> parsedChoices = parseChoices(question.getChoices());
        boolean isAnswered = answer != null;

        BattleQuestionWithProgressDto.BattleQuestionWithProgressDtoBuilder builder = BattleQuestionWithProgressDto
                .builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .orderNo(question.getOrderNo())
                .choices(parsedChoices)
                .isAnswered(isAnswered);

        if (answer != null) {
            int correctScoreThreshold = battleStatisticsService.getCorrectScoreThreshold();
            builder.userAnswer(answer.getUserAnswer())
                    .score(answer.getScore())
                    .feedback(answer.getFeedback())
                    .isCorrect(answer.getScore() >= correctScoreThreshold);
        }

        return builder.build();
    }

    /**
     * 답변한 문제 수 계산
     */
    private int countAnsweredQuestions(List<BattleQuestionWithProgressDto> questionDtos) {
        return (int) questionDtos.stream()
                .filter(BattleQuestionWithProgressDto::getIsAnswered)
                .count();
    }

    /**
     * 배틀 완료 여부 확인
     */
    private boolean isBattleCompleted(int answeredQuestions, int totalQuestions) {
        return answeredQuestions >= totalQuestions;
    }

    /**
     * 다음 풀어야 할 문제 순서 계산
     */
    private Integer calculateNextQuestionOrder(List<BattleQuestionWithProgressDto> questionDtos, boolean isCompleted) {
        if (isCompleted) {
            return null;
        }
        return questionDtos.stream()
                .filter(q -> !q.getIsAnswered())
                .map(BattleQuestionWithProgressDto::getOrderNo)
                .min(Integer::compareTo)
                .orElse(null);
    }

    /**
     * 선택지 JSON 파싱
     */
    private List<String> parseChoices(String choicesJson) {
        if (choicesJson == null || choicesJson.trim().isEmpty()) {
            return null;
        }
        try {
            return jsonUtils.fromJsonToList(choicesJson, String.class);
        } catch (Exception e) {
            log.warn(BattleMessage.CHOICES_PARSING_FAILED.format(e.getMessage()));
            return null;
        }
    }
}
