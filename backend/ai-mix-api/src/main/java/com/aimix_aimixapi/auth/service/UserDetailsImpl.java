package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.common.exception.domain.auth.UserRequiredException;
import com.aimix_aimixapi.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * UserDetails 구현 클래스
 * - User 엔티티를 Spring Security가 사용할 수 있는 UserDetails 형태로 변환
 * - Spring Security 인증 및 권한 체크에 필요한 정보 제공
 *
 * <p>주요 기능:
 * <ul>
 *   <li>User 엔티티를 UserDetails 인터페이스로 래핑</li>
 *   <li>사용자 권한(ROLE) 정보 제공</li>
 *   <li>계정 상태 정보 제공 (만료, 잠금, 활성화 등)</li>
 * </ul>
 *
 * <p>특징:
 * <ul>
 *   <li>username = 사용자 이메일</li>
 *   <li>권한은 "ROLE_" 접두사와 함께 제공 (예: "ROLE_USER", "ROLE_ADMIN")</li>
 *   <li>현재는 모든 계정 상태를 활성화(true)로 설정</li>
 * </ul>
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    /**
     * Spring Security 권한 접두사
     * - 예: "ROLE_USER", "ROLE_ADMIN"
     */
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * User 엔티티 (원본 사용자 정보)
     */
    private final User user;

    /**
     * 생성자
     * - User 엔티티를 받아 UserDetails로 변환
     *
     * @param user User 엔티티 (null 불가)
     * @throws UserRequiredException user가 null인 경우
     */
    public UserDetailsImpl(User user) {
        if (user == null) {
            throw new UserRequiredException(AuthMessage.USER_REQUIRED.getMessage());
        }
        this.user = user;
    }

    /**
     * 사용자 권한 목록 반환
     * - User 엔티티의 role을 Spring Security 권한 형식으로 변환
     * - "ROLE_" 접두사를 붙여서 반환
     *
     * @return 권한 목록 (예: ["ROLE_USER"])
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = ROLE_PREFIX + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * 사용자 비밀번호 반환
     * - 암호화된 비밀번호 반환
     *
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자명 반환
     * - Spring Security의 username = 사용자 이메일
     *
     * @return 사용자 이메일
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부 확인
     * - 현재는 항상 활성화(true) 반환
     *
     * @return 계정 만료 여부 (true: 만료되지 않음, false: 만료됨)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부 확인
     * - 현재는 항상 잠금 해제(true) 반환
     *
     * @return 계정 잠금 여부 (true: 잠금 해제, false: 잠금됨)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호) 만료 여부 확인
     * - 현재는 항상 유효(true) 반환
     *
     * @return 자격 증명 만료 여부 (true: 유효, false: 만료됨)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 확인
     * - 현재는 항상 활성화(true) 반환
     *
     * @return 계정 활성화 여부 (true: 활성화, false: 비활성화)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}

