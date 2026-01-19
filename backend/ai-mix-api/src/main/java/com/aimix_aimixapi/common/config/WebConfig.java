package com.aimix_aimixapi.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web 설정
 * - 정적 리소스 서빙 설정
 * - Spring Data Page 직렬화 설정 (PageImpl 경고 해결)
 */
@Log4j2
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileUploadConfig fileUploadConfig;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 업로드된 아바타 이미지 서빙
        // 환경 변수: AIMIX_UPLOAD_DIR (예: C:/dev/project/ai-mix/backend/uploads)
        String uploadDir = fileUploadConfig.getDir();

        // 절대 경로로 정규화
        // 환경 변수에서 절대 경로를 받으므로 isAbsolute() 체크 후 처리
        Path baseUploadPath = java.nio.file.Paths.get(uploadDir);
        if (!baseUploadPath.isAbsolute()) {
            // 상대 경로인 경우에만 절대 경로로 변환
            baseUploadPath = baseUploadPath.toAbsolutePath().normalize();
        }

        // users/avatars 폴더 구조 추가
        // DB에 저장된 경로: /uploads/users/avatars/2025/12/09/...
        // 실제 파일 경로: {AIMIX_UPLOAD_DIR}/users/avatars/2025/12/09/...
        Path usersAvatarsPath = baseUploadPath.resolve("users").resolve("avatars");

        // 정적 리소스 서빙 경로 설정
        // file: 프로토콜 사용 (파일 시스템 경로)
        // Windows 경로 구분자를 슬래시(/)로 변환 (Spring ResourceHandler는 / 사용)
        String resourceLocation = "file:" + usersAvatarsPath.toString().replace("\\", "/") + "/";

        // URL 경로: /uploads/users/avatars/** -> 실제 파일: {AIMIX_UPLOAD_DIR}/users/avatars/**
        registry.addResourceHandler("/uploads/users/avatars/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // 1시간 캐시

        log.info("정적 리소스 서빙 설정 완료 - 업로드 디렉토리: {}, 아바타 리소스 위치: {}",
                baseUploadPath, resourceLocation);
    }
}