# ============================================
# Multi-stage build for Spring Boot application
# ============================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle wrapper 및 빌드 설정 복사 (캐시 최적화)
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# 의존성 다운로드 (캐시 레이어)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# 포트 노출
EXPOSE 8081

# Spring profile을 prod로 설정하여 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
