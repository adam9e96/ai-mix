package com.aimix_aimixapi.user.service;

import com.aimix_aimixapi.common.encryption.ApiKeyEncryptionService;
import com.aimix_aimixapi.common.exception.domain.auth.InvalidRequestException;
import com.aimix_aimixapi.common.exception.encryption.EncryptionException;
import com.aimix_aimixapi.common.exception.domain.user.ApiKeyDecryptionFailedException;
import com.aimix_aimixapi.common.exception.domain.user.ApiKeySaveFailedException;
import com.aimix_aimixapi.common.exception.domain.user.InvalidApiKeyFormatException;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.entity.UserProfile;
import com.aimix_aimixapi.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 API 키 관리 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserApiKeyService {

    private final ApiKeyEncryptionService encryptionService;
    private final UserProfileRepository userProfileRepository;

    /**
     * API 키 저장 (등록/수정)
     * 
     * @param user 사용자
     * @param plainApiKey 평문 API 키
     */
    @Transactional
    public void saveApiKey(User user, String plainApiKey) {
        if (user == null) {
            throw new InvalidRequestException("사용자가 없습니다");
        }
        
        if (plainApiKey == null || plainApiKey.trim().isEmpty()) {
            throw new InvalidApiKeyFormatException("API 키가 없습니다");
        }

        // 형식 검증 (OpenAI API 키는 "sk-"로 시작)
        String trimmedKey = plainApiKey.trim();
        if (!trimmedKey.startsWith("sk-")) {
            throw new InvalidApiKeyFormatException("API 키 형식이 올바르지 않습니다. OpenAI API 키는 'sk-'로 시작해야 합니다");
        }

        try {
            // 암호화
            String encrypted = encryptionService.encrypt(trimmedKey);

            // UserProfile 조회 또는 생성
            UserProfile profile = user.getUserProfile();
            if (profile == null) {
                profile = UserProfile.builder()
                        .user(user)
                        .build();
                user.setUserProfile(profile);
            }

            // 암호화된 API 키 저장
            profile.setOpenaiApiKey(encrypted);
            userProfileRepository.save(profile);

            log.info("API 키 저장 완료: userId={}", user.getId());
        } catch (EncryptionException e) {
            log.error("API 키 암호화 실패: userId={}", user.getId(), e);
            throw new ApiKeySaveFailedException("API 키 저장에 실패했습니다", e);
        }
    }

    /**
     * API 키 조회 (복호화)
     * 
     * @param user 사용자
     * @return 복호화된 평문 API 키, 없으면 null
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Transactional(readOnly = true)
    public String getApiKey(User user) {
        if (user == null) {
            return null;
        }

        UserProfile profile = user.getUserProfile();
        if (profile == null || profile.getOpenaiApiKey() == null) {
            return null;
        }

        try {
            String decrypted = encryptionService.decrypt(profile.getOpenaiApiKey());
            log.debug("API 키 조회 완료: userId={}", user.getId());
            return decrypted;
        } catch (EncryptionException e) {
            log.error("API 키 복호화 실패: userId={}", user.getId(), e);
            throw new ApiKeyDecryptionFailedException("API 키 복호화에 실패했습니다. 암호화 키가 올바른지 확인하세요", e);
        }
    }

    /**
     * API 키 존재 여부 확인
     * 
     * @param user 사용자
     * @return API 키가 있으면 true, 없으면 false
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Transactional(readOnly = true)
    public boolean hasApiKey(User user) {
        if (user == null) {
            return false;
        }

        UserProfile profile = user.getUserProfile();
        return profile != null && profile.getOpenaiApiKey() != null && !profile.getOpenaiApiKey().isEmpty();
    }

    /**
     * API 키 삭제
     * 
     * @param user 사용자
     */
    @Transactional
    public void deleteApiKey(User user) {
        if (user == null) {
            throw new InvalidRequestException("사용자가 없습니다");
        }

        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            profile.setOpenaiApiKey(null);
            userProfileRepository.save(profile);
            log.info("API 키 삭제 완료: userId={}", user.getId());
        }
    }

    /**
     * API 키 마스킹 (보안용)
     * 예: "sk-1234567890abcdef" -> "sk-...****"
     * 
     * @param apiKey API 키
     * @return 마스킹된 API 키
     */
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        if (apiKey.length() <= 7) {
            return "sk-****";
        }

        // "sk-" + 마스킹
        return apiKey.substring(0, 7) + "...****";
    }
}
