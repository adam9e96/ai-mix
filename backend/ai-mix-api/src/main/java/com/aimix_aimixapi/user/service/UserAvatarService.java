package com.aimix_aimixapi.user.service;

import com.aimix_aimixapi.common.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 사용자 아바타 파일 관리 서비스
 * - 아바타 파일 저장 및 삭제
 * - 파일 경로 관리 및 보안 처리
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final FileUploadConfig fileUploadConfig;

    /**
     * 아바타 파일 저장
     * - 폴더 구조: uploads/users/avatars/YYYY/MM/DD/{hash}/{UUID}.{extension}
     * - 보안: userId를 해시화하여 폴더명 생성 (사용자 ID 노출 방지)
     * - 예: uploads/users/avatars/2025/01/03/a1b2c3/a1b2c3d4.jpg
     *
     * @param file   업로드할 아바타 이미지 파일
     * @param userId 사용자 ID
     * @return 아바타 파일 URL 경로
     * @throws IllegalArgumentException 파일이 null이거나 비어있는 경우
     * @throws RuntimeException         파일 저장 실패 시
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public String saveAvatar(MultipartFile file, Long userId) {
        // 입력값 검증
        if (file == null || file.isEmpty()) {
            log.warn("아바타 파일 저장 요청: 파일이 null이거나 비어있음 - userId={}", userId);
            throw new IllegalArgumentException("아바타 파일이 필요합니다");
        }

        if (userId == null) {
            log.warn("아바타 파일 저장 요청: userId가 null");
            throw new IllegalArgumentException("사용자 ID가 필요합니다");
        }

        try {
            // 기본 업로드 디렉토리 경로 (application.yml에서 설정)
            String baseUploadDir = fileUploadConfig.getDir();
            if (!StringUtils.hasText(baseUploadDir)) {
                log.error("아바타 파일 저장 실패: 업로드 디렉토리 경로가 설정되지 않음");
                throw new RuntimeException("업로드 디렉토리 경로가 설정되지 않았습니다");
            }

            // 경로 처리: 절대 경로인지 확인
            Path baseUploadPath;
            if (Paths.get(baseUploadDir).isAbsolute()) {
                baseUploadPath = Paths.get(baseUploadDir);
            } else {
                baseUploadPath = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            }

            // users/avatars 폴더 구조 추가
            Path usersAvatarsPath = baseUploadPath.resolve("users").resolve("avatars");

            // 날짜별 폴더 구조 생성 (YYYY/MM/DD)
            LocalDate today = LocalDate.now();
            String year = String.valueOf(today.getYear());
            String month = String.format("%02d", today.getMonthValue());
            String day = String.format("%02d", today.getDayOfMonth());

            // 사용자별 폴더 구조 생성 (보안: userId 해시화)
            String userFolder = hashUserId(userId);

            // 최종 저장 경로: baseUploadDir/users/avatars/YYYY/MM/DD/{hash}/
            Path userUploadPath = usersAvatarsPath
                    .resolve(year)
                    .resolve(month)
                    .resolve(day)
                    .resolve(userFolder);

            // 사용자별 업로드 디렉토리 생성
            if (!Files.exists(userUploadPath)) {
                Files.createDirectories(userUploadPath);
                log.debug("아바타 업로드 디렉토리 생성: path={}", userUploadPath);
            }

            // 파일 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 고유한 파일명 생성 (UUID만 사용)
            String filename = UUID.randomUUID() + extension;
            Path filePath = userUploadPath.resolve(filename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL 반환: /uploads/users/avatars/YYYY/MM/DD/{hash}/{UUID}.{extension}
            String avatarUrl = String.format("/uploads/users/avatars/%s/%s/%s/%s/%s",
                    year, month, day, userFolder, filename);

            log.info("아바타 파일 저장 완료: userId={}, path={}, url={}",
                    userId, filePath, avatarUrl);
            return avatarUrl;

        } catch (IOException e) {
            log.error("아바타 파일 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("아바타 파일 저장에 실패했습니다", e);
        }
    }

    /**
     * 아바타 파일 삭제
     * - URL 경로에서 실제 파일 경로를 추출하여 삭제
     * - 빈 사용자 폴더도 자동으로 삭제
     * - 파일 삭제 실패 시 예외를 던지지 않고 로그만 남김 (DB는 이미 삭제되었을 수 있음)
     *
     * @param avatarUrl DB에 저장된 아바타 URL (예: /uploads/users/avatars/2025/01/03/a1b2c3/xxx.jpg)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public void deleteAvatar(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            log.debug("아바타 파일 삭제: avatarUrl이 null이거나 빈 문자열");
            return;
        }

        try {
            // URL에서 실제 파일 경로 추출
            // /uploads/users/avatars/2025/01/03/a1b2c3/xxx.jpg
            // → uploads/users/avatars/2025/01/03/a1b2c3/xxx.jpg
            String relativePath = avatarUrl.startsWith("/")
                    ? avatarUrl.substring(1)
                    : avatarUrl;

            // 기본 업로드 디렉토리 경로
            String baseUploadDir = fileUploadConfig.getDir();
            if (!StringUtils.hasText(baseUploadDir)) {
                log.warn("아바타 파일 삭제 실패: 업로드 디렉토리 경로가 설정되지 않음 - url={}", avatarUrl);
                return;
            }

            Path baseUploadPath;
            if (Paths.get(baseUploadDir).isAbsolute()) {
                baseUploadPath = Paths.get(baseUploadDir);
            } else {
                baseUploadPath = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            }

            // 상대 경로에서 base 경로 제거
            // uploads/users/avatars/2025/01/03/a1b2c3/xxx.jpg
            // → users/avatars/2025/01/03/a1b2c3/xxx.jpg
            String basePath = "uploads/users/avatars/";
            if (relativePath.startsWith(basePath)) {
                relativePath = relativePath.substring(basePath.length());
            } else {
                // 이전 형식 호환성 (uploads/avatars/...)
                String oldBasePath = "uploads/avatars/";
                if (relativePath.startsWith(oldBasePath)) {
                    relativePath = "users/avatars/" + relativePath.substring(oldBasePath.length());
                }
            }

            // users/avatars 경로 추가
            Path filePath = baseUploadPath.resolve(relativePath);

            // 파일 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("아바타 파일 삭제 완료: path={}", filePath);

                // 사용자 폴더가 비어있으면 폴더도 삭제
                Path userFolder = filePath.getParent();
                if (userFolder != null && Files.exists(userFolder)) {
                    try {
                        // 폴더가 비어있는지 확인
                        if (Files.list(userFolder).findAny().isEmpty()) {
                            Files.delete(userFolder);
                            log.debug("빈 사용자 폴더 삭제: {}", userFolder);
                        }
                    } catch (IOException e) {
                        log.warn("사용자 폴더 삭제 실패 (무시): {}", userFolder, e);
                    }
                }
            } else {
                log.warn("삭제할 아바타 파일이 존재하지 않음: path={}", filePath);
            }

        } catch (IOException e) {
            log.error("아바타 파일 삭제 실패: url={}, error={}", avatarUrl, e.getMessage(), e);
            // 파일 삭제 실패는 예외를 던지지 않고 로그만 남김 (DB는 이미 삭제되었을 수 있음)
        }
    }

    /**
     * 사용자 ID를 해시화하여 폴더명 생성
     * - 보안: 사용자 ID 노출 방지를 위해 SHA-256 해시의 첫 6자리 사용
     * - SHA-256 알고리즘 실패 시 폴백 처리
     *
     * @param userId 사용자 ID
     * @return 해시값 (6자리, 소문자)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String hashUserId(Long userId) {
        if (userId == null) {
            log.warn("사용자 ID 해시화: userId가 null, 기본값 사용");
            return "000000";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(userId.toString().getBytes(StandardCharsets.UTF_8));

            // 해시를 16진수 문자열로 변환하고 첫 6자리만 사용
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 첫 6자리 반환 (소문자)
            String hashValue = hexString.substring(0, 6).toLowerCase();
            log.debug("사용자 ID 해시화 완료: userId={}, hash={}", userId, hashValue);
            return hashValue;

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 알고리즘을 찾을 수 없습니다, 폴백 사용: userId={}", userId, e);
            // 폴백: userId를 문자열로 변환 후 간단한 해시
            return String.format("%06d", userId.hashCode() & 0xFFFFFF);
        }
    }
}
