package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.auth.dto.SignupRequest;
import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.common.exception.domain.auth.*;
import com.aimix_aimixapi.common.exception.domain.user.DuplicateNicknameException;
import com.aimix_aimixapi.user.message.UserMessage;
import com.aimix_aimixapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 회원가입 요청 검증 서비스
 * - 회원가입 요청의 유효성 검증 담당
 * - 필수 필드 존재 여부 및 중복 체크
 *
 * <p>주요 기능:
 * <ul>
 *   <li>요청 DTO null 체크</li>
 *   <li>이메일 필수값 검증 및 중복 체크</li>
 *   <li>닉네임 필수값 검증 및 중복 체크</li>
 *   <li>비밀번호 필수값 검증</li>
 *   <li>개인정보 동의 여부 확인</li>
 * </ul>
 *
 * <p>검증 순서:
 * <ol>
 *   <li>요청 DTO null 체크</li>
 *   <li>이메일 검증 및 trim 처리</li>
 *   <li>이메일 중복 체크</li>
 *   <li>닉네임 검증 및 trim 처리</li>
 *   <li>닉네임 중복 체크</li>
 *   <li>비밀번호 검증</li>
 *   <li>개인정보 동의 여부 확인</li>
 * </ol>
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class SignupValidator {

    private final UserRepository userRepository;

    /**
     * 회원가입 요청 검증
     * - 모든 필수 필드와 중복 여부를 검증
     *
     * <p>검증 항목:
     * <ul>
     *   <li>요청 DTO null 체크</li>
     *   <li>이메일 필수값 및 중복 체크</li>
     *   <li>닉네임 필수값 및 중복 체크</li>
     *   <li>비밀번호 필수값 체크</li>
     *   <li>개인정보 동의 여부 확인</li>
     * </ul>
     *
     * <p>주의사항:
     * <ul>
     *   <li>이메일과 닉네임은 trim 처리되어 중복 체크됨</li>
     *   <li>비밀번호는 존재 여부만 확인하며, 형식이나 길이는 검증하지 않음</li>
     *   <li>검증 실패 시 각 필드에 맞는 예외 발생</li>
     * </ul>
     *
     * @param request 회원가입 요청 DTO (null 불가)
     * @throws RequestRequiredException   요청이 null인 경우
     * @throws EmailRequiredException     이메일이 null이거나 빈 문자열인 경우
     * @throws DuplicateEmailException    이메일이 이미 사용 중인 경우
     * @throws NicknameRequiredException  닉네임이 null이거나 빈 문자열인 경우
     * @throws DuplicateNicknameException 닉네임이 이미 사용 중인 경우
     * @throws PasswordRequiredException  비밀번호가 null이거나 빈 문자열인 경우
     * @throws AgreementRequiredException 개인정보 동의가 false이거나 null인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public void validate(SignupRequest request) {
        validateRequestNotNull(request);
        String email = validateAndTrimEmail(request);
        validateEmailNotDuplicate(email);
        String nickname = validateAndTrimNickname(request);
        validateNicknameNotDuplicate(nickname);
        validatePassword(request);
        validateAgreement(request);
    }

    /**
     * 요청이 null이 아닌지 확인
     * - 회원가입 요청 DTO의 기본 null 체크
     *
     * @param request 회원가입 요청 DTO
     * @throws RequestRequiredException 요청이 null인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void validateRequestNotNull(SignupRequest request) {
        if (request == null) {
            log.warn("회원가입 요청: request가 null입니다");
            throw new RequestRequiredException(AuthMessage.REQUEST_REQUIRED.getMessage());
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
     * @param request 회원가입 요청 DTO (null 불가)
     * @return trim 처리된 이메일 (null 불가)
     * @throws EmailRequiredException 이메일이 null이거나 빈 문자열인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String validateAndTrimEmail(SignupRequest request) {
        String email = request.getEmail();
        if (!StringUtils.hasText(email)) {
            log.warn("회원가입 요청: 이메일이 제공되지 않음");
            throw new EmailRequiredException(AuthMessage.EMAIL_REQUIRED.getMessage());
        }
        return email.trim();
    }

    /**
     * 이메일 중복 체크
     * - UserRepository를 통해 이메일이 이미 사용 중인지 확인
     *
     * @param email 검증할 이메일 (trim 처리됨, null 불가)
     * @throws DuplicateEmailException 이메일이 이미 사용 중인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void validateEmailNotDuplicate(String email) {
        if (!isEmailAvailable(email)) {
            log.warn("회원가입 실패: 이메일 중복 - email={}", email);
            throw new DuplicateEmailException(
                    UserMessage.DUPLICATE_EMAIL.format(email));
        }
    }

    /**
     * 닉네임 검증 및 trim 처리
     * - 닉네임 필드의 존재 여부 확인 및 앞뒤 공백 제거
     *
     * <p>검증 내용:
     * <ul>
     *   <li>닉네임이 null이 아니고 빈 문자열이 아닌지 확인</li>
     *   <li>앞뒤 공백 제거 (trim)</li>
     * </ul>
     *
     * @param request 회원가입 요청 DTO (null 불가)
     * @return trim 처리된 닉네임 (null 불가)
     * @throws NicknameRequiredException 닉네임이 null이거나 빈 문자열인 경우
     */
    private String validateAndTrimNickname(SignupRequest request) {
        String nickname = request.getNickname();
        if (!StringUtils.hasText(nickname)) {
            log.warn("회원가입 요청: 닉네임이 제공되지 않음");
            throw new NicknameRequiredException(AuthMessage.NICKNAME_REQUIRED.getMessage());
        }
        return nickname.trim();
    }

    /**
     * 닉네임 중복 체크
     * - UserRepository를 통해 닉네임이 이미 사용 중인지 확인
     *
     * @param nickname 검증할 닉네임 (trim 처리됨, null 불가)
     * @throws DuplicateNicknameException 닉네임이 이미 사용 중인 경우
     */
    private void validateNicknameNotDuplicate(String nickname) {
        if (!isNicknameAvailable(nickname)) {
            log.warn("회원가입 실패: 닉네임 중복 - nickname={}", nickname);
            throw new DuplicateNicknameException(UserMessage.DUPLICATE_NICKNAME.format(nickname));
        }
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
     * @param request 회원가입 요청 DTO (null 불가)
     * @throws PasswordRequiredException 비밀번호가 null이거나 빈 문자열인 경우
     */
    private void validatePassword(SignupRequest request) {
        String password = request.getPassword();
        if (!StringUtils.hasText(password)) {
            log.warn("회원가입 요청: 비밀번호가 제공되지 않음");
            throw new PasswordRequiredException(AuthMessage.PASSWORD_REQUIRED.getMessage());
        }
    }

    /**
     * 개인정보 동의 여부 확인
     * - 개인정보 처리방침 동의가 필수임을 확인
     *
     * <p>검증 내용:
     * <ul>
     *   <li>isAgreed가 null이 아니고 true인지 확인</li>
     *   <li>false이거나 null이면 예외 발생</li>
     * </ul>
     *
     * @param request 회원가입 요청 DTO (null 불가)
     * @throws AgreementRequiredException 개인정보 동의가 false이거나 null인 경우
     */
    private void validateAgreement(SignupRequest request) {
        Boolean isAgreed = request.getIsAgreed();
        if (isAgreed == null || !isAgreed) {
            log.warn("회원가입 요청: 개인정보 동의가 필요함");
            throw new AgreementRequiredException(AuthMessage.AGREEMENT_REQUIRED.getMessage());
        }
    }

    /**
     * 이메일 사용 가능 여부 확인
     * - UserRepository를 통해 이메일이 이미 존재하는지 확인
     *
     * @param email 확인할 이메일 (trim 처리됨, null 불가)
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * 닉네임 사용 가능 여부 확인
     * - UserRepository를 통해 닉네임이 이미 존재하는지 확인
     *
     * @param nickname 확인할 닉네임 (trim 처리됨, null 불가)
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }
}
