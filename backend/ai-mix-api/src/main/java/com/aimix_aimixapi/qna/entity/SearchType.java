package com.aimix_aimixapi.qna.entity;

/**
 * 질문 검색 타입 열거형
 */
public enum SearchType {
    /**
     * 제목만 검색
     */
    TITLE,

    /**
     * 작성자만 검색
     */
    AUTHOR,

    /**
     * 내용만 검색
     */
    BODY,

    /**
     * 태그만 검색
     */
    TAG,

    /**
     * 전체 검색 (제목, 내용, 태그)
     */
    ALL;

    /**
     * 문자열을 SearchType으로 변환
     * null이거나 빈 문자열이면 TITLE을 기본값으로 반환
     *
     * @param searchType 검색 타입 문자열
     * @return SearchType enum
     */
    public static SearchType fromString(String searchType) {
        if (searchType == null || searchType.trim().isEmpty()) {
            return TITLE;
        }

        try {
            return valueOf(searchType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
