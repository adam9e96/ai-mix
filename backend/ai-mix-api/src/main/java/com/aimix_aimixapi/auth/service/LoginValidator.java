package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.auth.dto.LoginRequest;
import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.common.exception.domain.auth.EmailRequiredException;
import com.aimix_aimixapi.common.exception.domain.auth.PasswordRequiredException;
import com.aimix_aimixapi.common.exception.domain.auth.RequestRequiredException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 로그인 요청 검증 서비스
 * - 로그인 요청의 유효성 검증 담당
 * - 필수 필드(이메일, 비밀번호) 존재 여부 확인
 * - 이메일 trim 처리 및 반환
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Component
public class LoginValidator {

    /**
     * 로그인 요청 검증
     * - 로그인에 필요한 필수 필드들을 검증하고 이메일을 반환
     *
     * <p>동작 과정:
     * <ol>
     *   <li>요청 DTO가 null이 아닌지 확인</li>
     *   <li>이메일 필수값 검증 및 앞뒤 공백 제거 (trim)</li>
     *   <li>비밀번호 필수값 검증</li>
     *   <li>검증된 이메일 반환</li>
     * </ol>
     *
     * <p>주의사항:
     * <ul>
     *   <li>이메일은 trim 처리되어 반환되므로, 호출자는 반환된 값을 사용해야 함</li>
     *   <li>비밀번호는 존재 여부만 확인하며, 형식이나 길이는 검증하지 않음</li>
     *   <li>검증 실패 시 각 필드에 맞는 예외 발생</li>
     * </ul>
     *
     * @param request 로그인 요청 DTO (null 불가)
     * @return 검증된 이메일 (trim 처리됨, null 불가)
     * @throws RequestRequiredException  요청이 null인 경우
     * @throws EmailRequiredException    이메일이 null이거나 빈 문자열인 경우
     * @throws PasswordRequiredException 비밀번호가 null이거나 빈 문자열인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public String validate(LoginRequest request) {
        validateRequestNotNull(request);
        String email = validateAndTrimEmail(request);
        validatePassword(request);
        return email;
    }

    /**
     * 요청이 null이 아닌지 확인
     * - 로그인 요청 DTO의 기본 null 체크
     *
     * @param request 로그인 요청 DTO
     * @throws RequestRequiredException 요청이 null인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void validateRequestNotNull(LoginRequest request) {
        if (request == null) {
            log.warn("로그인 요청: request가 null입니다");
            throw new RequestRequiredException(AuthMessage.LOGIN_REQUIRED.getMessage());
        }
    }

    /**
     * 이메일 검증 및 trim 처리
     * - 이메일 필드의 존재 여부 확인 및 앞뒤 공백 제거
     *
     * <p>검증 내용:
     * <ul>
     *   <li>이메일이 null이 아니고 빈 문자열이 아닌지 확인</li>
     *   <li>앞뒤 공백 제거 (trim)</li>
     * </ul>
     *
     * @param request 로그인 요청 DTO (null 불가)
     * @return trim 처리된 이메일 (null 불가)
     * @throws EmailRequiredException 이메일이 null이거나 빈 문자열인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String validateAndTrimEmail(LoginRequest request) {
        String email = request.getEmail();
        if (!StringUtils.hasText(email)) {
            log.warn("로그인 요청: 이메일이 제공되지 않음");
            throw new EmailRequiredException(AuthMessage.EMAIL_REQUIRED.getMessage());
        }
        return email.trim();
    }

    /**
     * 비밀번호 검증
     * - 비밀번호 필드의 존재 여부 확인
     *
     * <p>검증 내용:
     * <ul>
     *   <li>비밀번호가 null이 아니고 빈 문자열이 아닌지 확인</li>
     *   <li>비밀번호 형식이나 길이는 검증하지 않음 (인증 단계에서 처리)</li>
     * </ul>
     *
     * @param request 로그인 요청 DTO (null 불가)
     * @throws PasswordRequiredException 비밀번호가 null이거나 빈 문자열인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void validatePassword(LoginRequest request) {
        String password = request.getPassword();
        if (!StringUtils.hasText(password)) {
            log.warn("로그인 요청: 비밀번호가 제공되지 않음");
            throw new PasswordRequiredException(AuthMessage.PASSWORD_REQUIRED.getMessage());
        }
    }
}
