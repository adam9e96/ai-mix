package com.aimix_aimixapi.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 파일 업로드 설정
 * application.yml의 file.upload 설정값을 주입받음
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 파일 업로드 디렉토리
     * - application.yml의 file.upload.dir 값이 주입됨
     * - 상대 경로: 현재 작업 디렉토리 기준 (예: ../uploads/avatars/)
     * - 절대 경로: 시스템 전체 경로 (예: C:/uploads/avatars/, /var/uploads/avatars/)
     */
    private String dir;
}
