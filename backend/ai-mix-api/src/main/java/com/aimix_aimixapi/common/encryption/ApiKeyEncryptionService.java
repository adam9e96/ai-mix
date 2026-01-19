package com.aimix_aimixapi.common.encryption;

import com.aimix_aimixapi.common.exception.encryption.EncryptionException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * API 키 암호화 서비스
 * Spring Security Encryptors를 사용하여 API 키를 암호화/복호화
 * Encryptors.stronger() 사용 (AES-256-GCM, 인증 포함)
 */
@Log4j2
@Service
public class ApiKeyEncryptionService {

    @Value("${api-key.encryption.key}")
    private String encryptionKey;

    @Value("${api-key.encryption.salt}")
    private String salt;

    private BytesEncryptor encryptor;

    @PostConstruct
    public void init() {
        // 환경변수 확인 및 경고
        String envKey = System.getenv("API_KEY_ENCRYPTION_KEY");
        String envSalt = System.getenv("API_KEY_ENCRYPTION_SALT");

        if (envKey == null || envSalt == null) {
            log.warn("============================================================");
            log.warn("⚠️  경고: API 키 암호화 환경변수가 설정되지 않았습니다!");
            log.warn("⚠️    - API_KEY_ENCRYPTION_KEY");
            log.warn("⚠️    - API_KEY_ENCRYPTION_SALT");
            log.warn("⚠️    PowerShell 스크립트 실행: .\\scripts\\generate-encryption-keys.ps1");
            log.warn("============================================================");
        } else {
            log.info("API 키 암호화 환경변수가 설정되었습니다");
        }

        // Encryptors.stronger() 사용 (AES-256-GCM, 인증 포함)
        // BytesEncryptor를 반환하므로 Base64 인코딩/디코딩 필요
        this.encryptor = Encryptors.stronger(encryptionKey, salt);
        log.info("API 키 암호화 서비스 초기화 완료");
    }

    /**
     * API 키 암호화
     *
     * @param plainApiKey 평문 API 키
     * @return 암호화된 API 키 (Base64 인코딩)
     */
    public String encrypt(String plainApiKey) {
        if (plainApiKey == null || plainApiKey.isEmpty()) {
            throw new IllegalArgumentException("암호화할 API 키가 없습니다");
        }

        try {
            // 문자열을 바이트 배열로 변환
            byte[] plainBytes = plainApiKey.getBytes(StandardCharsets.UTF_8);

            // 암호화
            byte[] encryptedBytes = encryptor.encrypt(plainBytes);

            // Base64로 인코딩하여 문자열로 변환
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);

            log.debug("API 키 암호화 완료 (길이: {})", encrypted.length());
            return encrypted;
        } catch (Exception e) {
            log.error("API 키 암호화 실패", e);
            throw new EncryptionException("API 키 암호화에 실패했습니다", e);
        }
    }

    /**
     * API 키 복호화
     *
     * @param encryptedApiKey 암호화된 API 키 (Base64 인코딩)
     * @return 복호화된 평문 API 키
     */
    public String decrypt(String encryptedApiKey) {
        if (encryptedApiKey == null || encryptedApiKey.isEmpty()) {
            return null;
        }

        try {
            // Base64 디코딩
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedApiKey);

            // 복호화
            byte[] decryptedBytes = encryptor.decrypt(encryptedBytes);

            // 바이트 배열을 문자열로 변환
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("API 키 복호화 완료");
            return decrypted;
        } catch (Exception e) {
            log.error("API 키 복호화 실패", e);
            throw new EncryptionException("API 키 복호화에 실패했습니다. 암호화 키가 올바른지 확인하세요", e);
        }
    }
}
