package com.aimix_aimixapi.knowledge.entity;

/**
 * 지식 맵 관계 타입
 */
public enum RelationshipType {
    /**
     * 선행 개념 (예: HTTP → REST)
     */
    PREREQUISITE,

    /**
     * 관련 개념 (예: REST ↔ GraphQL)
     */
    RELATED,

    /**
     * 포함 관계 (예: HTTP Method → REST)
     */
    PART_OF
}