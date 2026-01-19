package com.aimix_aimixapi.common.exception.handler;

import com.aimix_aimixapi.common.exception.domain.AnswerNotFoundException;
import com.aimix_aimixapi.common.exception.domain.QuestionNotFoundException;
import com.aimix_aimixapi.common.exception.domain.qna.*;
import com.aimix_aimixapi.common.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * QNA 도메인 예외 처리 핸들러
 * - 질문, 답변, 익명 게시글 관련 예외 처리
 */
@Log4j2
@RestControllerAdvice
public class QnaExceptionHandler {

    /**
     * 질문을 찾을 수 없을 때 예외 처리
     */
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQuestionNotFoundException(
            QuestionNotFoundException e, HttpServletRequest request) {
        log.warn("질문을 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "Question Not Found", e.getMessage(), request);
    }

    /**
     * 답변을 찾을 수 없을 때 예외 처리
     */
    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAnswerNotFoundException(
            AnswerNotFoundException e, HttpServletRequest request) {
        log.warn("답변을 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "Answer Not Found", e.getMessage(), request);
    }

    /**
     * 익명 게시글 비밀번호 필수 예외 처리
     */
    @ExceptionHandler(AnonymousPasswordRequiredException.class)
    public ResponseEntity<ErrorResponse> handleAnonymousPasswordRequiredException(
            AnonymousPasswordRequiredException e, HttpServletRequest request) {
        log.warn("익명 게시글 비밀번호 필수: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Anonymous Password Required", e.getMessage(), request);
    }

    /**
     * 일반 게시글 작성 시 로그인 필수 예외 처리
     */
    @ExceptionHandler(LoginRequiredForNormalPostException.class)
    public ResponseEntity<ErrorResponse> handleLoginRequiredForNormalPostException(
            LoginRequiredForNormalPostException e, HttpServletRequest request) {
        log.warn("일반 게시글 작성 시 로그인 필수: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "Login Required For Normal Post", e.getMessage(), request);
    }

    /**
     * QNA 비밀번호 불일치 예외 처리
     */
    @ExceptionHandler(QnaPasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handleQnaPasswordMismatchException(
            QnaPasswordMismatchException e, HttpServletRequest request) {
        log.warn("QNA 비밀번호 불일치: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "QNA Password Mismatch", e.getMessage(), request);
    }

    /**
     * AI 답변은 수정할 수 없음 예외 처리
     */
    @ExceptionHandler(AiAnswerCannotBeModifiedException.class)
    public ResponseEntity<ErrorResponse> handleAiAnswerCannotBeModifiedException(
            AiAnswerCannotBeModifiedException e, HttpServletRequest request) {
        log.warn("AI 답변은 수정할 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.FORBIDDEN, "AI Answer Cannot Be Modified", e.getMessage(), request);
    }

    /**
     * GPT 답변을 찾을 수 없을 때 예외 처리
     */
    @ExceptionHandler(GptAnswerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGptAnswerNotFoundException(
            GptAnswerNotFoundException e, HttpServletRequest request) {
        log.warn("GPT 답변을 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "GPT Answer Not Found", e.getMessage(), request);
    }
}
