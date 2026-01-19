package com.aimix_aimixapi.common.exception.handler;

import com.aimix_aimixapi.common.exception.domain.knowledge.InvalidSourceTypeException;
import com.aimix_aimixapi.common.exception.domain.knowledge.card.*;
import com.aimix_aimixapi.common.exception.domain.knowledge.contribution.*;
import com.aimix_aimixapi.common.exception.domain.knowledge.gpt.CardGenerationFailedException;
import com.aimix_aimixapi.common.exception.domain.knowledge.gpt.CardParsingFailedException;
import com.aimix_aimixapi.common.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 지식(Knowledge) 도메인 예외 처리 핸들러
 * - 지식 카드, 기여, GPT 관련 예외 처리
 */
@Log4j2
@RestControllerAdvice
public class KnowledgeExceptionHandler {

    /**
     * 지식 카드를 찾을 수 없을 때 예외 처리
     */
    @ExceptionHandler(KnowledgeCardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleKnowledgeCardNotFoundException(
            KnowledgeCardNotFoundException e, HttpServletRequest request) {
        log.warn("지식 카드를 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "Knowledge Card Not Found", e.getMessage(), request);
    }

    /**
     * 중복 지식 카드 예외 처리
     */
    @ExceptionHandler(DuplicateKnowledgeCardException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKnowledgeCardException(
            DuplicateKnowledgeCardException e, HttpServletRequest request) {
        log.warn("중복 지식 카드 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.CONFLICT, "Duplicate Knowledge Card", e.getMessage(), request);
    }

    /**
     * 지식 카드 접근 권한 없음 예외 처리
     */
    @ExceptionHandler(KnowledgeCardAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleKnowledgeCardAccessDeniedException(
            KnowledgeCardAccessDeniedException e, HttpServletRequest request) {
        log.warn("지식 카드 접근 권한 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.FORBIDDEN, "Knowledge Card Access Denied", e.getMessage(), request);
    }

    /**
     * 대화 내용이 없을 때 예외 처리
     */
    @ExceptionHandler(EmptyConversationException.class)
    public ResponseEntity<ErrorResponse> handleEmptyConversationException(
            EmptyConversationException e, HttpServletRequest request) {
        log.warn("대화 내용이 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Empty Conversation", e.getMessage(), request);
    }

    /**
     * 카드 생성 실패 예외 처리
     */
    @ExceptionHandler(CardGenerationFailedException.class)
    public ResponseEntity<ErrorResponse> handleCardGenerationFailedException(
            CardGenerationFailedException e, HttpServletRequest request) {
        log.error("카드 생성 실패: {}", e.getMessage(), e);
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR, "Card Generation Failed", e.getMessage(), request);
    }

    /**
     * 카드 데이터 파싱 실패 예외 처리
     */
    @ExceptionHandler(CardParsingFailedException.class)
    public ResponseEntity<ErrorResponse> handleCardParsingFailedException(
            CardParsingFailedException e, HttpServletRequest request) {
        log.error("카드 데이터 파싱 실패: {}", e.getMessage(), e);
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR, "Card Parsing Failed", e.getMessage(), request);
    }

    /**
     * 카드 필수 예외 처리
     */
    @ExceptionHandler(KnowledgeCardRequiredException.class)
    public ResponseEntity<ErrorResponse> handleKnowledgeCardRequiredException(
            KnowledgeCardRequiredException e, HttpServletRequest request) {
        log.warn("카드 필수 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Knowledge Card Required", e.getMessage(), request);
    }

    /**
     * 기여자 필수 예외 처리
     */
    @ExceptionHandler(ContributorRequiredException.class)
    public ResponseEntity<ErrorResponse> handleContributorRequiredException(
            ContributorRequiredException e, HttpServletRequest request) {
        log.warn("기여자 필수 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Contributor Required", e.getMessage(), request);
    }

    /**
     * 기여 타입 필수 예외 처리
     */
    @ExceptionHandler(ContributionTypeRequiredException.class)
    public ResponseEntity<ErrorResponse> handleContributionTypeRequiredException(
            ContributionTypeRequiredException e, HttpServletRequest request) {
        log.warn("기여 타입 필수 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Contribution Type Required", e.getMessage(), request);
    }

    /**
     * 지원하지 않는 출처 타입 예외 처리
     */
    @ExceptionHandler(InvalidSourceTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSourceTypeException(
            InvalidSourceTypeException e, HttpServletRequest request) {
        log.warn("지원하지 않는 출처 타입 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Invalid Source Type", e.getMessage(), request);
    }
}
