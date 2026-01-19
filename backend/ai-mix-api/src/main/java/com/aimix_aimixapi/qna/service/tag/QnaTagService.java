package com.aimix_aimixapi.qna.service.tag;

import com.aimix_aimixapi.gpt.service.GptService;
import com.aimix_aimixapi.common.util.JsonUtils;
import com.aimix_aimixapi.qna.dto.tag.GptTagResponse;
import com.aimix_aimixapi.qna.dto.qna.QnaQuestionResponse;
import com.aimix_aimixapi.qna.dto.tag.QnaTagGenerateRequest;
import com.aimix_aimixapi.qna.dto.tag.QnaTagGenerateResponse;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.entity.QnaQuestionTag;
import com.aimix_aimixapi.qna.entity.QnaTag;
import com.aimix_aimixapi.qna.mapper.QnaMapper;
import com.aimix_aimixapi.qna.repository.QnaQuestionTagRepository;
import com.aimix_aimixapi.qna.repository.QnaTagRepository;
import com.aimix_aimixapi.qna.service.battle.QnaGptPromptBuilder;
import com.aimix_aimixapi.qna.service.qna.QnaAuthorizationService;
import com.aimix_aimixapi.qna.service.qna.QnaQuestionService;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * QNA 태그 서비스
 * - GPT를 이용한 태그 생성
 * - 질문에 태그 저장
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaTagService {

    private final QnaTagRepository qnaTagRepository;
    private final QnaQuestionTagRepository qnaQuestionTagRepository;
    private final QnaQuestionService questionService;
    private final QnaAuthorizationService authorizationService;
    private final QnaGptPromptBuilder gptPromptBuilder;
    private final GptService gptService;
    private final QnaMapper qnaMapper;
    private final JsonUtils jsonUtils;

    /**
     * GPT를 사용하여 추천 태그 목록 생성
     * - DB에 저장하지 않고 태그 리스트만 반환
     * - 요청한 사용자(현재 로그인 사용자)의 API 키 사용
     *
     * @param user 요청한 사용자 (현재 로그인 사용자, null 가능)
     * @param request 태그 생성 요청 (questionId)
     * @return 추천 태그 목록
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 질문 ID로 질문 조회
     * 2. GPT 프롬프트 생성
     * 3. GPT API 호출 (요청한 사용자의 API 키 사용)
     * 4. GPT 응답에서 태그 목록 파싱
     * 5. 태그 목록 반환
     * <p>
     * 주의사항:
     * - 오류 발생 시 빈 리스트 반환
     * - 태그는 DB에 저장되지 않음
     * - 사용자가 null인 경우 토큰 사용량을 기록하지 않음
     */
    @Transactional(readOnly = true)
    public QnaTagGenerateResponse generateRecommendedTags(User user, QnaTagGenerateRequest request) {
        log.info("태그 생성 요청: questionId={}, userId={}", 
                request.getQuestionId(), user != null ? user.getId() : null);

        try {
            // 1. questionId로 질문 조회
            QnaQuestion question = questionService.findQuestionById(request.getQuestionId());
            log.debug("질문 조회 완료: questionId={}, title={}", question.getId(), question.getTitle());

            // 2. GPT 프롬프트 생성
            String prompt = gptPromptBuilder.buildTagPrompt(question);
            log.debug("태그 생성 프롬프트: {}", prompt);

            // 3. GPT API 호출
            // 요청한 사용자(현재 로그인 사용자)의 API 키 사용
            // 사용자가 null인 경우 토큰 사용량을 기록하지 않음
            String gptResponse = user != null
                    ? gptService.callGptApi(user, prompt, null, null,
                            com.aimix_aimixapi.gpt.token.entity.GptUsageType.QNA)
                    : gptService.callGptApi(prompt);
            log.debug("GPT 태그 생성 응답: {}", gptResponse);

            // 4. GPT 응답에서 태그 목록 파싱
            List<String> tagNames = parseTagsFromGptResponse(gptResponse);
            if (tagNames == null || tagNames.isEmpty()) {
                log.warn("태그가 생성되지 않았습니다: questionId={}", question.getId());
                return QnaTagGenerateResponse.builder()
                        .tags(List.of())
                        .build();
            }

            log.info("생성된 태그 목록: questionId={}, tags={}", question.getId(), tagNames);

            return QnaTagGenerateResponse.builder()
                    .tags(tagNames)
                    .build();

        } catch (Exception e) {
            log.error("태그 생성 중 오류 발생: questionId={}, error={}",
                    request.getQuestionId(), e.getMessage(), e);
            // 오류 발생 시 빈 리스트 반환
            return QnaTagGenerateResponse.builder()
                    .tags(List.of())
                    .build();
        }
    }

    /**
     * 선택한 태그 목록을 질문에 저장
     * - 기존 태그는 모두 제거하고 새로운 태그로 교체
     *
     * @param email             사용자 이메일 (권한 검증용)
     * @param questionId        질문 ID
     * @param tagNames          저장할 태그 이름 목록
     * @param anonymousPassword 익명 게시글 비밀번호 (익명 게시글인 경우 필수)
     * @return 업데이트된 질문 정보
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 질문 조회
     * 2. 권한 검증 (질문 작성자만 태그 수정 가능)
     * 3. 기존 태그 연결 모두 삭제
     * 4. 새로운 태그 연결 (태그가 없으면 생성)
     * 5. 질문을 다시 조회하여 최신 태그 정보 반영
     */
    @Transactional
    public QnaQuestionResponse saveTagsToQuestion(String email, UUID questionId, List<String> tagNames, String anonymousPassword) {
        log.info("태그 저장 요청: email={}, questionId={}, tagCount={}", email, questionId,
                tagNames != null ? tagNames.size() : 0);

        // 1. 질문 조회
        QnaQuestion question = questionService.findQuestionById(questionId);

        // 2. 권한 검증 (질문 작성자만 태그 수정 가능)
        authorizationService.validateQuestionUpdatePermission(question, email, anonymousPassword);

        // 3. 기존 태그 연결 모두 삭제
        qnaQuestionTagRepository.deleteByQuestionId(questionId);
        question.getQuestionTags().clear();
        log.info("기존 태그 연결 삭제 완료: questionId={}", questionId);

        // 4. 새로운 태그 연결
        if (tagNames != null && !tagNames.isEmpty()) {
            for (String tagName : tagNames) {
                if (tagName == null || tagName.trim().isEmpty()) {
                    continue;
                }

                // 태그 이름 정리 및 길이 제한 (50자)
                String trimmedTagName = tagName.trim();
                if (trimmedTagName.length() > 50) {
                    trimmedTagName = trimmedTagName.substring(0, 50);
                }

                final String finalTagName = trimmedTagName;

                // 태그가 이미 존재하는지 확인
                QnaTag tag = qnaTagRepository.findByName(finalTagName)
                        .orElseGet(() -> {
                            // 태그가 없으면 새로 생성
                            QnaTag newTag = QnaTag.builder()
                                    .name(finalTagName)
                                    .build();
                            QnaTag savedTag = qnaTagRepository.save(newTag);
                            log.info("새 태그 생성: tagId={}, name={}", savedTag.getId(), savedTag.getName());
                            return savedTag;
                        });

                // 질문과 태그 연결
                QnaQuestionTag questionTag = QnaQuestionTag.builder()
                        .questionId(question.getId())
                        .tagId(tag.getId())
                        .question(question)
                        .tag(tag)
                        .build();
                qnaQuestionTagRepository.save(questionTag);
                log.debug("태그 연결 완료: questionId={}, tagId={}, tagName={}",
                        question.getId(), tag.getId(), tag.getName());
            }
        }

        // 5. 질문을 다시 조회하여 최신 태그 정보 반영
        QnaQuestion savedQuestion = questionService.findQuestionById(questionId);
        log.info("태그 저장 완료: questionId={}, tagCount={}", questionId,
                savedQuestion.getQuestionTags().size());

        return qnaMapper.toQuestionResponse(savedQuestion);
    }

    /**
     * GPT 응답에서 태그 목록 파싱
     *
     * @param gptResponse GPT API 응답 문자열
     * @return 태그 이름 목록
     * @apiNote 점검O
     * @since 2025-01-15
     */
    private List<String> parseTagsFromGptResponse(String gptResponse) {
        try {
            // GPT 응답에서 JSON 추출 및 DTO로 파싱
            GptTagResponse tagResponse = jsonUtils.fromGptResponse(gptResponse, GptTagResponse.class);

            if (tagResponse.getTags() == null || tagResponse.getTags().isEmpty()) {
                log.warn("GPT 응답에 태그가 없습니다");
                return List.of();
            }

            return tagResponse.getTags();

        } catch (Exception e) {
            log.error("태그 파싱 실패: error={}, response={}", e.getMessage(), gptResponse, e);
            // 파싱 실패 시 빈 리스트 반환
            return List.of();
        }
    }
}
