package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.qna.dto.qna.QnaQuestionListResponse;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.entity.SearchType;
import com.aimix_aimixapi.qna.mapper.QnaMapper;
import com.aimix_aimixapi.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QNA 검색 서비스
 * - 질문 검색 기능
 * - 다양한 검색 타입 지원 (제목, 작성자, 내용, 태그, 전체)
 * - 다중 태그 검색 지원
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaSearchService {

    private final QnaQuestionRepository questionRepository;
    private final QnaMapper qnaMapper;

    /**
     * 질문 검색
     * - 다양한 검색 타입 지원
     * - 다중 태그 검색 지원 (쉼표로 구분)
     *
     * @param keyword    검색어
     * @param searchType 검색 타입 (title: 제목만, author: 작성자만, body: 내용만, tag: 태그만, all: 전체)
     * @param pageable   페이징 정보
     * @return 페이징된 질문 목록 응답
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 검색 타입 문자열을 SearchType enum으로 변환
     * 2. 검색 타입에 따라 적절한 검색 메서드 호출
     * 3. 검색 결과를 DTO로 변환하여 반환
     * <p>
     * 주의사항:
     * - TAG 검색인 경우 다중 태그 지원 (쉼표로 구분)
     * - 검색 타입이 지정되지 않은 경우 기본값은 ALL
     */
    @Transactional(readOnly = true)
    public Page<QnaQuestionListResponse> searchQuestions(String keyword, String searchType, Pageable pageable) {
        log.info("질문 검색: keyword={}, searchType={}, page={}, size={}",
                keyword, searchType, pageable.getPageNumber(), pageable.getPageSize());

        SearchType type = SearchType.fromString(searchType);
        Page<QnaQuestion> questions = searchByType(keyword, type, pageable);

        return questions.map(qnaMapper::toQuestionListResponse);
    }

    /**
     * 검색 타입에 따라 질문 검색
     *
     * @param keyword    검색어
     * @param searchType 검색 타입
     * @param pageable   페이징 정보
     * @return 검색된 질문 목록
     * @apiNote 점검O
     * @since 2025-01-15
     */
    private Page<QnaQuestion> searchByType(String keyword, SearchType searchType, Pageable pageable) {
        // TAG 검색인 경우 다중 태그 지원 (쉼표로 구분)
        if (searchType == SearchType.TAG && keyword != null && keyword.contains(",")) {
            return searchByMultipleTags(keyword, pageable);
        }

        return switch (searchType) {
            case TITLE -> questionRepository.searchByTitle(keyword, pageable);
            case AUTHOR -> questionRepository.searchByAuthor(keyword, pageable);
            case BODY -> questionRepository.searchByBody(keyword, pageable);
            case TAG -> questionRepository.searchByTag(keyword, pageable);
            case ALL -> questionRepository.searchByKeywordAndTags(keyword, pageable);
        };
    }

    /**
     * 다중 태그로 검색 (쉼표로 구분된 태그)
     * - 예: "REST,API,웹개발" -> REST 또는 API 또는 웹개발 태그를 가진 질문 검색
     *
     * @param keywords 쉼표로 구분된 태그 목록
     * @param pageable 페이징 정보
     * @return 검색된 질문 목록
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 쉼표로 구분하여 태그 목록 추출
     * 2. 태그 이름 정리 (trim, 소문자 변환, 중복 제거)
     * 3. 태그 목록이 비어있으면 빈 페이지 반환
     * 4. 태그 목록으로 검색 수행
     */
    private Page<QnaQuestion> searchByMultipleTags(String keywords, Pageable pageable) {
        // 쉼표로 구분하여 태그 목록 추출
        List<String> tagNames = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.toList());

        if (tagNames.isEmpty()) {
            log.warn("태그가 없습니다: keywords={}", keywords);
            return Page.empty(pageable);
        }

        log.info("다중 태그 검색: tagNames={}", tagNames);
        return questionRepository.searchByTags(tagNames, pageable);
    }
}
