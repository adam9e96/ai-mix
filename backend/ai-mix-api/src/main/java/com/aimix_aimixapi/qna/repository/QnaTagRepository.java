package com.aimix_aimixapi.qna.repository;

import com.aimix_aimixapi.qna.entity.QnaTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * QnA 태그 Repository
 * QnA 태그 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface QnaTagRepository extends JpaRepository<QnaTag, Long> {
    /**
     * 태그 이름으로 태그 조회
     * 
     * @param name 태그 이름
     * @return 태그 엔티티 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<QnaTag> findByName(String name);

    /**
     * 태그 이름으로 존재 여부 확인
     * 
     * @param name 태그 이름
     * @return 태그가 존재하면 true, 없으면 false
     * @apiNote 점검O
     * @since 2025-12-28
     */
    boolean existsByName(String name);
}