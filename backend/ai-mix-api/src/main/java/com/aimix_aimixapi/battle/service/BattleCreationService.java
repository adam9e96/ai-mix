package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.dto.BattleCreateRequest;
import com.aimix_aimixapi.battle.dto.BattleCreateResponse;
import com.aimix_aimixapi.battle.dto.BattleQuestionDto;
import com.aimix_aimixapi.battle.dto.QuestionData;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.config.BattleProperties;
import com.aimix_aimixapi.battle.entity.BattleSourceType;
import com.aimix_aimixapi.battle.entity.QuestionType;
import com.aimix_aimixapi.battle.message.BattleMessage;
import com.aimix_aimixapi.battle.repository.BattleRepository;
import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.service.ChatMessageService;
import com.aimix_aimixapi.chat.service.ChatSessionService;
import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import com.aimix_aimixapi.gpt.util.GptResponseUtils;
import com.aimix_aimixapi.common.util.JsonUtils;
import com.aimix_aimixapi.qna.service.qna.QnaService;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 배틀 생성 서비스
 * - 배틀 생성 로직을 전담 (Chat, QnA 등 다양한 소스 기반)
 * - BattleService의 생성 로직을 분리
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleCreationService {

    private final BattleRepository battleRepository;
    private final BattleQuestionService battleQuestionService;
    private final BattleQuestionGenerationService battleQuestionGenerationService;
    private final BattleAnswerValidationService battleAnswerValidationService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final QnaService qnaService;
    private final JsonUtils jsonUtils;
    private final BattleProperties battleProperties;

    /**
     * 채팅 세션 기반 배틀 생성
     */
    @Transactional
    public BattleCreateResponse createBattleFromChat(User user, BattleCreateRequest request,
            QuestionType questionType) {
        UUID id = request.getId();
        if (id == null) {
            throw new IllegalArgumentException(BattleMessage.CHAT_ID_REQUIRED.getMessage());
        }

        log.info("채팅 기반 배틀 생성: email={}, id={}, questionType={}", user.getEmail(), id, questionType);

        // 1. 세션 조회 및 권한 확인
        chatSessionService.findByIdAndUser(id, user);

        // 2. 세션의 모든 메시지 조회 (대화 내용)
        List<ChatMessage> messages = chatMessageService.findBySessionIdOrderByCreatedAtAsc(id);

        if (messages.isEmpty()) {
            throw new ResourceNotFoundException(BattleMessage.CHAT_MESSAGES_EMPTY.format(id));
        }

        // 3. 대화 내용을 텍스트로 변환
        String conversationText = GptResponseUtils.buildConversationText(messages);

        // 4. 문제 생성 및 저장
        return generateAndSaveBattle(user, conversationText, BattleSourceType.CHAT, id, questionType);
    }

    /**
     * QnA 기반 배틀 생성
     */
    @Transactional
    public BattleCreateResponse createBattleFromQna(User user, BattleCreateRequest request, QuestionType questionType) {
        UUID id = request.getId();
        if (id == null) {
            throw new IllegalArgumentException(BattleMessage.QNA_ID_REQUIRED.getMessage());
        }

        log.info("QnA 기반 배틀 생성: email={}, id={}, questionType={}", user.getEmail(), id, questionType);

        // 1. QnA 데이터 수집
        String qnaText = qnaService.collectQnaDataForBattle(id);

        // 2. 문제 생성 및 저장
        return generateAndSaveBattle(user, qnaText, BattleSourceType.QNA, id, questionType);
    }

    /**
     * 공통 배틀 생성 및 저장 로직
     */
    private BattleCreateResponse generateAndSaveBattle(User user, String textContent, BattleSourceType sourceType,
            UUID sourceId, QuestionType questionType) {
        // 1. ChatGPT API 호출하여 문제 생성
        List<QuestionData> questions = battleQuestionGenerationService.generateQuestionsFromConversation(textContent,
                questionType, user);

        if (questions.isEmpty()) {
            throw new RuntimeException(BattleMessage.QUESTION_GENERATION_FAILED.getMessage());
        }

        // 2. 배틀 레벨 결정
        String battleLevel = battleQuestionService.determineBattleLevel(questions);

        // 3. Battle 엔티티 생성 및 저장
        Battle battle = Battle.builder()
                .user(user)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .level(battleLevel)
                .totalQuestions(questions.size())
                .createdAt(LocalDateTime.now())
                .build();

        Battle savedBattle = battleRepository.save(battle);
        log.info("배틀 저장 완료: battleId={}", savedBattle.getId());

        // 4. BattleQuestion 엔티티들 생성 및 저장
        List<BattleQuestionDto> questionDtos = saveQuestions(savedBattle, questions, questionType);

        // 5. 응답 생성
        return BattleCreateResponse.builder()
                .battleId(savedBattle.getId())
                .sourceType(savedBattle.getSourceType().name())
                .sourceId(savedBattle.getSourceId())
                .level(savedBattle.getLevel())
                .totalQuestions(savedBattle.getTotalQuestions())
                .questions(questionDtos)
                .build();
    }

    /**
     * BattleQuestion 엔티티들 생성 및 저장
     */
    private List<BattleQuestionDto> saveQuestions(Battle battle, List<QuestionData> questions,
            QuestionType questionType) {
        List<BattleQuestionDto> questionDtos = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuestionData questionData = questions.get(i);

            // 객관식인 경우 choices를 JSON 문자열로 변환
            String choicesJson = null;
            int choiceCount = 0;
            if (questionData.getChoices() != null && !questionData.getChoices().isEmpty()) {
                try {
                    choicesJson = jsonUtils.toJson(questionData.getChoices());
                    choiceCount = questionData.getChoices().size();
                } catch (Exception e) {
                    log.warn(BattleMessage.CHOICES_PARSING_FAILED.format(e.getMessage()));
                }
            }

            // 객관식인 경우 정답을 숫자로 정규화
            String normalizedCorrectAnswer = questionData.getCorrectAnswer();
            if (questionData.getQuestionType() == QuestionType.OBJECTIVE && choiceCount > 0) {
                try {
                    normalizedCorrectAnswer = battleAnswerValidationService
                            .normalizeObjectiveAnswer(questionData.getCorrectAnswer(), choiceCount);
                } catch (IllegalArgumentException e) {
                    log.error("객관식 정답 정규화 실패: {}", e.getMessage());
                    throw new RuntimeException("객관식 정답 형식이 올바르지 않습니다.", e);
                }
            }

            BattleQuestion question = BattleQuestion.builder()
                    .battle(battle)
                    .questionText(questionData.getQuestionText())
                    .correctAnswer(normalizedCorrectAnswer)
                    .orderNo(i + 1)
                    .difficulty(questionData.getDifficulty())
                    .questionType(
                            questionData.getQuestionType() != null ? questionData.getQuestionType() : questionType)
                    .choices(choicesJson)
                    .build();

            BattleQuestion savedQuestion = battleQuestionService.save(question);
            battle.addQuestion(savedQuestion);

            // 저장된 choices를 파싱하여 DTO에 담기
            List<String> parsedChoices = null;
            if (savedQuestion.getChoices() != null && !savedQuestion.getChoices().trim().isEmpty()) {
                // BattleAnswerScoringService의존성 대신 JsonUtils 사용 권장하지만, 기존 로직 유지를 위해 서비스 사용
                // 하지만 여기선 JsonUtils로 대체 가능
                try {
                    parsedChoices = jsonUtils.fromJsonToList(savedQuestion.getChoices(), String.class);
                } catch (Exception e) {
                    parsedChoices = questionData.getChoices();
                }
            }

            questionDtos.add(BattleQuestionDto.builder()
                    .questionId(savedQuestion.getId())
                    .questionText(savedQuestion.getQuestionText())
                    .orderNo(savedQuestion.getOrderNo())
                    .questionType(savedQuestion.getQuestionType())
                    .choices(parsedChoices)
                    .build());
        }

        return questionDtos;
    }
}
