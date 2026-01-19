package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.user.dto.UserResponse;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.entity.UserProfile;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * UserResponse 빌더 서비스
 * - User 엔티티를 UserResponse DTO로 변환
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Component
public class UserResponseBuilder {

    /**
     * User 엔티티를 UserResponse로 변환
     *
     * @param user 사용자 엔티티
     * @return UserResponse DTO
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public UserResponse build(User user) {
        UserProfile userProfile = user.getUserProfile();

        // 프로필 정보 추출
        String avatarUrl = null;
        String bio = null;
        Map<String, Object> settings = null;

        if (userProfile != null) {
            avatarUrl = userProfile.getAvatarUrl();
            bio = userProfile.getBio();
            settings = userProfile.getSettings();
        }

        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .avatarUrl(avatarUrl)
                .createdAt(user.getCreatedAt())
                .birthDate(user.getBirthDate())
                .bio(bio)
                .lastLoginAt(user.getLastLoginAt())
                .settings(settings)
                .build();
    }
}
