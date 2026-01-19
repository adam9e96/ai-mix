package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.common.exception.domain.auth.EmailRequiredException;
import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.common.exception.domain.auth.UserDetailsNotFoundException;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UserDetailsService 구현 클래스
 * - Spring Security의 UserDetailsService 인터페이스 구현
 * - 이메일을 통해 사용자를 조회하여 UserDetails를 반환
 * - Spring Security 인증 과정에서 사용자 정보를 제공하는 핵심 서비스
 *
 * <p>주요 기능:
 * <ul>
 *   <li>이메일로 사용자 조회 및 UserDetails 변환</li>
 *   <li>이메일 유효성 검증</li>
 *   <li>사용자 미존재 시 UsernameNotFoundException 발생</li>
 * </ul>
 *
 * <p>동작 과정:
 * <ol>
 *   <li>이메일 유효성 검증 (null 또는 빈 문자열 체크)</li>
 *   <li>UserRepository를 통해 사용자 조회</li>
 *   <li>User 엔티티를 UserDetailsImpl로 변환하여 반환</li>
 * </ol>
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자명(이메일)으로 사용자 정보 조회
     * - Spring Security 인증 과정에서 호출됨
     * - 이메일을 사용자명으로 사용 (Spring Security의 username = 이메일)
     *
     * <p>동작 과정:
     * <ol>
     *   <li>이메일 유효성 검증</li>
     *   <li>UserRepository를 통해 사용자 조회</li>
     *   <li>User 엔티티를 UserDetailsImpl로 변환하여 반환</li>
     * </ol>
     *
     * <p>예외 처리:
     * <ul>
     *   <li>이메일이 null이거나 빈 문자열: EmailRequiredException → UsernameNotFoundException</li>
     *   <li>사용자가 존재하지 않음: UserDetailsNotFoundException → UsernameNotFoundException</li>
     * </ul>
     *
     * @param email 사용자 이메일 (Spring Security의 username으로 사용됨, null 불가)
     * @return UserDetails 구현체 (UserDetailsImpl)
     * @throws UsernameNotFoundException 이메일이 유효하지 않거나 사용자가 존재하지 않는 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            validateEmail(email);
            User user = findUserByEmail(email);
            log.debug("사용자 조회 성공: email={}, role={}", email, user.getRole());
            
            return new UserDetailsImpl(user);
        } catch (EmailRequiredException e) {
            log.warn("이메일이 비어있습니다");
            throw new UsernameNotFoundException(e.getMessage());
        } catch (UserDetailsNotFoundException e) {
            log.warn("사용자 정보를 찾을 수 없습니다: {}", e.getMessage());
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    /**
     * 이메일 유효성 검증
     * - 이메일이 null이 아니고 빈 문자열이 아닌지 확인
     *
     * @param email 검증할 이메일
     * @throws EmailRequiredException 이메일이 null이거나 빈 문자열인 경우
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new EmailRequiredException(AuthMessage.EMAIL_REQUIRED.getMessage());
        }
    }

    /**
     * 이메일로 사용자 조회
     * - UserRepository를 통해 사용자 조회
     *
     * @param email 사용자 이메일 (null 불가, trim 처리 권장)
     * @return 조회된 User 엔티티
     * @throws UserDetailsNotFoundException 사용자가 존재하지 않는 경우
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserDetailsNotFoundException(AuthMessage.USER_DETAILS_NOT_FOUND.format(email)));
    }
}

