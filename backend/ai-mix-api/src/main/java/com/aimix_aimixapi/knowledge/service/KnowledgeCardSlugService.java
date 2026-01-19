package com.aimix_aimixapi.knowledge.service;

import com.aimix_aimixapi.knowledge.repository.KnowledgeCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 지식 카드 슬러그 서비스
 * 카드 제목 기반 슬러그 생성 및 중복 체크를 담당합니다.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class KnowledgeCardSlugService {

    private final KnowledgeCardRepository cardRepository;

    /**
     * 슬러그 생성 (제목 기반, DB 조회 포함)
     * 실제 카드 저장 시 사용하며, 중복 체크를 통해 고유한 슬러그를 생성합니다.
     *
     * @param title 카드 제목 (null 불가)
     * @return 고유한 슬러그
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public String generateSlug(String title) {
        log.debug("슬러그 생성 요청: title={}", title);

        String slug = normalizeSlug(title);

        // 중복 체크 및 숫자 추가
        String baseSlug = slug;
        int suffix = 1;
        while (cardRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }

        log.debug("슬러그 생성 완료: originalTitle={}, slug={}", title, slug);
        return slug;
    }

    /**
     * 미리보기용 슬러그 생성 (DB 조회 없이 생성)
     * readOnly 트랜잭션에서도 사용 가능하며, 중복 체크를 하지 않습니다.
     *
     * @param title 카드 제목 (null 불가)
     * @return 정규화된 슬러그 (중복 체크 없음)
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public String generateSlugForPreview(String title) {
        log.debug("미리보기용 슬러그 생성: title={}", title);
        return normalizeSlug(title);
    }

    /**
     * 제목을 슬러그 형식으로 정규화
     * - 소문자 변환
     * - 특수문자 제거 (영문, 숫자, 한글, 공백, 하이픈만 허용)
     * - 공백을 하이픈으로 변환
     * - 연속된 하이픈을 하나로 통합
     * - 앞뒤 하이픈 제거
     *
     * @param title 카드 제목
     * @return 정규화된 슬러그
     * @apiNote 점검O
     * @since 2025-12-29
     */
    private String normalizeSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9가-힣\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
