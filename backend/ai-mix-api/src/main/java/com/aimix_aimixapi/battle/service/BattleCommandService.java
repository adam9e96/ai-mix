package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.dto.*;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleAnswer;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.config.BattleProperties;
import com.aimix_aimixapi.battle.entity.BattleSourceType;
import com.aimix_aimixapi.battle.entity.QuestionType;
import com.aimix_aimixapi.battle.message.BattleMessage;
import com.aimix_aimixapi.battle.repository.BattleRepository;
import com.aimix_aimixapi.common.exception.domain.AccessDeniedException;
import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import com.aimix_aimixapi.common.util.JsonUtils;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 배틀 명령 서비스
 * - 배틀 생성, 답변 제출, 재도전 등 상태를 변경하는 작업 담당
 * - CQRS 패턴의 Command 담당
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleCommandService {

    private final BattleCreationService battleCreationService;
    private final BattleRepository battleRepository;
    private final BattleAnswerService battleAnswerService;
    private final BattleQuestionService battleQuestionService;
    private final BattleAnswerScoringService battleAnswerScoringService;
    private final BattleAnswerValidationService battleAnswerValidationService;
    private final BattleStatisticsService battleStatisticsService;
    private final UserService userService;
    private final JsonUtils jsonUtils;
    private final BattleProperties battleProperties;

    /**
     * 배틀 생성
     */
    @Transactional
    public BattleCreateResponse createBattle(String email, BattleCreateRequest request) {
        User user = userService.findUserByEmail(email);
        BattleSourceType sourceType = request.getSourceType();
        QuestionType questionType = request.getQuestionType();

        return switch (sourceType) {
            case QNA -> battleCreationService.createBattleFromQna(user, request, questionType);
            case CHAT -> battleCreationService.createBattleFromChat(user, request, questionType);
            default -> throw new IllegalArgumentException(
                    BattleMessage.UNSUPPORTED_SOURCE_TYPE.format(sourceType));
        };
    }

    /**
     * 배틀 답변 제출 및 채점
     */
    @Transactional
    public BattleAnswerSubmitResponse submitAnswer(String email, BattleAnswerSubmitRequest request) {
        log.info("배틀 답변 제출 요청: email={}, battleId={}, questionId={}", email, request.getBattleId(),
                request.getQuestionId());

        User user = userService.findUserByEmail(email);
        Battle battle = battleRepository.findById(request.getBattleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        BattleMessage.BATTLE_NOT_FOUND_BY_ID.format(request.getBattleId())));

        validateBattleAccess(battle, user);

        BattleQuestion question = battleQuestionService.findByIdAndBattle(request.getQuestionId(), battle);

        validateAnswerNotExists(battle.getId(), question.getId());

        // 답변 검증 및 정규화
        String normalizedAnswer = battleAnswerValidationService.validateAndNormalizeAnswer(question,
                request.getUserAnswer());

        // 채점
        ScoringResult scoringResult = switch (question.getQuestionType()) {
            case OBJECTIVE -> battleAnswerScoringService.scoreObjectiveAnswer(question, normalizedAnswer);
            case SUBJECTIVE -> battleAnswerScoringService.scoreAnswerWithGpt(question, normalizedAnswer, user);
        };

        // 저장
        BattleAnswer answer = BattleAnswer.builder()
                .battle(battle)
                .question(question)
                .userAnswer(normalizedAnswer)
                .score(scoringResult.getScore())
                .feedback(scoringResult.getFeedback())
                .createdAt(LocalDateTime.now())
                .build();

        BattleAnswer savedAnswer = battleAnswerService.save(answer);
        battle.addAnswer(answer);
        question.addAnswer(answer);

        return BattleAnswerSubmitResponse.builder()
                .answerId(savedAnswer.getId())
                .battleId(battle.getId())
                .questionId(question.getId())
                .userAnswer(normalizedAnswer)
                .correctAnswer(question.getCorrectAnswer())
                .score(scoringResult.getScore())
                .feedback(scoringResult.getFeedback())
                .isCorrect(scoringResult.getScore() >= battleStatisticsService.getCorrectScoreThreshold())
                .build();
    }

    /**
     * 배틀 재도전
     */
    @Transactional
    public BattleCreateResponse retryBattle(String email, UUID battleId) {
        log.info("배틀 재도전 요청: email={}, battleId={}", email, battleId);

        User user = userService.findUserByEmail(email);
        Battle battle = battleRepository.findByIdAndUser(battleId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        BattleMessage.BATTLE_NOT_FOUND_BY_ID.format(battleId)));

        // 기존 답변 삭제
        List<BattleAnswer> existingAnswers = battleAnswerService.findByBattleId(battleId);
        if (!existingAnswers.isEmpty()) {
            battleAnswerService.deleteAll(existingAnswers);
            battle.getAnswers().clear();
        }

        // 문제 목록 조회 및 DTO 변환
        List<BattleQuestion> questions = battle.getQuestions().stream()
                .sorted((q1, q2) -> q1.getOrderNo().compareTo(q2.getOrderNo()))
                .toList();

        List<BattleQuestionDto> questionDtos = new ArrayList<>();
        for (BattleQuestion question : questions) {
            List<String> parsedChoices = null;
            if (question.getChoices() != null && !question.getChoices().trim().isEmpty()) {
                try {
                    parsedChoices = jsonUtils.fromJsonToList(question.getChoices(), String.class);
                } catch (Exception e) {
                    log.warn("선택지 파싱 실패, 무시함: {}", e.getMessage());
                }
            }

            questionDtos.add(BattleQuestionDto.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .questionType(question.getQuestionType())
                    .orderNo(question.getOrderNo())
                    .choices(parsedChoices)
                    .build());
        }

        return BattleCreateResponse.builder()
                .battleId(battle.getId())
                .sourceType(battle.getSourceType().name())
                .sourceId(battle.getSourceId())
                .level(battle.getLevel())
                .totalQuestions(battle.getTotalQuestions())
                .questions(questionDtos)
                .build();
    }

    /**
     * 배틀 접근 권한 검증
     */
    private void validateBattleAccess(Battle battle, User user) {
        if (!battle.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(BattleMessage.BATTLE_ACCESS_DENIED.getMessage());
        }
    }

    /**
     * 답변이 이미 존재하는지 검증
     */
    private void validateAnswerNotExists(UUID battleId, UUID questionId) {
        Optional<BattleAnswer> existingAnswer = battleAnswerService.findByBattleIdAndQuestionId(battleId, questionId);
        if (existingAnswer.isPresent()) {
            throw new IllegalArgumentException(BattleMessage.ANSWER_ALREADY_EXISTS.getMessage());
        }
    }
}
