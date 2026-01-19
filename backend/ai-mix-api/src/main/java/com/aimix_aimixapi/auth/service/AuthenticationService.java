package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.message.UserMessage;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 * - 사용자 인증 로직 담당
 * - 이메일과 비밀번호를 통한 사용자 인증 처리
 * - 비밀번호 검증 및 마지막 로그인 시간 업데이트
 *
 * <p>주요 기능:
 * <ul>
 *   <li>사용자 이메일로 사용자 조회</li>
 *   <li>입력된 비밀번호와 저장된 비밀번호 비교 검증</li>
 *   <li>인증 성공 시 마지막 로그인 시간 업데이트</li>
 * </ul>
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 인증
     * - 이메일과 비밀번호를 통한 사용자 인증 처리
     * - 인증 성공 시 마지막 로그인 시간을 자동으로 업데이트
     *
     * <p>동작 과정:
     * <ol>
     *   <li>이메일로 사용자 조회 (UserService.findUserByEmailForLogin 사용)</li>
     *   <li>입력된 비밀번호와 저장된 암호화된 비밀번호 비교</li>
     *   <li>비밀번호 일치 시 마지막 로그인 시간 업데이트</li>
     *   <li>인증된 사용자 엔티티 반환</li>
     * </ol>
     *
     * <p>주의사항:
     * <ul>
     *   <li>사용자가 존재하지 않으면 UserService에서 AuthenticationFailedException 발생</li>
     *   <li>비밀번호가 일치하지 않으면 BadCredentialsException 발생</li>
     *   <li>인증 성공 시 사용자의 lastLoginAt 필드가 현재 시간으로 업데이트됨</li>
     * </ul>
     *
     * @param email    사용자 이메일 (null 불가, trim 처리된 값 권장)
     * @param password 사용자 비밀번호 (평문, null 불가)
     * @return 인증된 사용자 엔티티 (프로필 정보 포함)
     * @throws BadCredentialsException 비밀번호가 일치하지 않는 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Transactional
    public User authenticate(String email, String password) {
        log.debug("로그인 시도: email={}", email);

        // 사용자 조회 (사용자가 없으면 AuthenticationFailedException 발생)
        User user = userService.findUserByEmailForLogin(email);

        // 비밀번호 검증 (BCrypt로 암호화된 비밀번호와 비교)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("로그인 실패: 비밀번호 불일치 - email={}", email);
            throw new BadCredentialsException(UserMessage.PASSWORD_MISMATCH.getMessage());
        }

        // 마지막 로그인 시간 업데이트 (인증 성공 시에만)
        userService.updateLastLogin(user);

        log.info("로그인 성공: email={}, nickname={}", user.getEmail(), user.getNickname());

        return user;
    }
}
