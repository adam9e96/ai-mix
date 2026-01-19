package com.aimix_aimixapi.user.service;

import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.battle.repository.BattleRepository;
import com.aimix_aimixapi.chat.repository.ChatSessionRepository;
import com.aimix_aimixapi.common.exception.domain.auth.AuthenticationFailedException;
import com.aimix_aimixapi.common.exception.domain.auth.PasswordRequiredException;
import com.aimix_aimixapi.common.exception.domain.user.DuplicateNicknameException;
import com.aimix_aimixapi.common.exception.domain.user.PasswordMismatchException;
import com.aimix_aimixapi.common.exception.domain.user.UserEmailNotFoundException;
import com.aimix_aimixapi.common.exception.domain.user.UserNotFoundException;
import com.aimix_aimixapi.qna.entity.AnswerType;
import com.aimix_aimixapi.qna.repository.QnaAnswerRepository;
import com.aimix_aimixapi.qna.repository.QnaQuestionRepository;
import com.aimix_aimixapi.user.dto.*;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.entity.UserProfile;
import com.aimix_aimixapi.user.message.UserMessage;
import com.aimix_aimixapi.user.repository.UserProfileRepository;
import com.aimix_aimixapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 사용자 서비스
 * - 사용자 정보 조회 및 관리
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BattleRepository battleRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final QnaQuestionRepository qnaQuestionRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final com.aimix_aimixapi.common.util.SettingsUtils settingsUtils;
    private final UserAvatarService avatarService;

    /**
     * 사용자 null 체크
     *
     * @param user 사용자 엔티티
     * @throws UserNotFoundException 사용자가 null인 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void validateUser(User user) {
        if (user == null) {
            log.warn("사용자 정보가 null입니다 - validateUser 호출됨");
            throw new UserNotFoundException(UserMessage.USER_NOT_FOUND.getMessage());
        }
    }

    /**
     * 이메일로 사용자 조회
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     * @throws UserEmailNotFoundException 사용자를 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다: email={}", email);
                    return new UserEmailNotFoundException(UserMessage.EMAIL_NOT_FOUND.format(email));
                });
    }

    /**
     * 로그인용 이메일로 사용자 조회
     * - 보안을 위해 사용자가 존재하지 않을 때 AuthenticationFailedException을 던짐
     * - 구체적인 사용자 정보를 노출하지 않음
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     * @throws AuthenticationFailedException 사용자를 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Transactional(readOnly = true)
    public User findUserByEmailForLogin(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("로그인 실패: 사용자를 찾을 수 없음 - email={}", email);
                    return new AuthenticationFailedException(
                            AuthMessage.AUTHENTICATION_FAILED.getMessage());
                });
    }

    /**
     * 마지막 로그인 시간 업데이트
     *
     * @param user 사용자 엔티티
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Transactional
    public void updateLastLogin(User user) {
        validateUser(user);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.debug("마지막 로그인 시간 업데이트: email={}, id={}", user.getEmail(), user.getId());
    }

    /**
     * 현재 로그인한 사용자 정보 조회 (단순 프로필 표시용)
     *
     * @param user 사용자 엔티티
     * @return 사용자 정보 DTO
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional(readOnly = true)
    public UserResponse getUser(User user) {
        validateUser(user);
        log.debug("사용자 정보 조회: email={}, nickname={}", user.getEmail(), user.getNickname());

        // 프로필 정보 가져오기 (LAZY 로딩이므로 필요시 조회)
        String avatarUrl = null;
        String bio = null;
        Map<String, Object> settings = null;
        if (user.getUserProfile() != null) {
            avatarUrl = user.getUserProfile().getAvatarUrl();
            bio = user.getUserProfile().getBio();
            settings = user.getUserProfile().getSettings();
        }

        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .birthDate(user.getBirthDate())
                .lastLoginAt(user.getLastLoginAt())
                .avatarUrl(avatarUrl)
                .bio(bio)
                .settings(settings)
                .build();
    }

    /**
     * 마이페이지용 사용자 정보 조회
     * - 기본 사용자 정보
     * - 통계 정보 (배틀 참여 횟수, 채팅 세션 수)
     * - 사용자 설정 정보
     *
     * @param user 사용자 엔티티
     * @return 마이페이지 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(User user) {
        validateUser(user);

        log.debug("마이페이지 정보 조회: email={}, nickname={}", user.getEmail(), user.getNickname());

        // 기본 사용자 정보 조회 (settings 포함)
        UserResponse userResponse = getUser(user);

        // 통계 정보 조회 (count 쿼리로 최적화)
        long battleCount = battleRepository.countByUser(user);
        long chatSessionCount = chatSessionRepository.countByUser(user);

        // 마이페이지 응답 생성
        return MyPageResponse.builder()
                .userResponse(userResponse)
                .statistics(MyPageResponse.UserStatistics.builder()
                        .battleCount(battleCount)
                        .chatSessionCount(chatSessionCount)
                        .build())
                .build();
    }

    /**
     * 비밀번호 확인
     * - 개인정보 수정 전 비밀번호 검증용
     *
     * @param user    사용자 엔티티
     * @param request 비밀번호 확인 요청
     * @return 비밀번호 확인 응답
     * @throws PasswordMismatchException 비밀번호가 일치하지 않는 경우
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional(readOnly = true)
    public PasswordCheckResponse verifyPassword(User user, @NotNull PasswordVerifyRequest request) {
        validateUser(user);

        if (!StringUtils.hasText(request.getPassword())) {
            log.warn("비밀번호가 제공되지 않았습니다: email={}", user.getEmail());
            throw new PasswordRequiredException(UserMessage.PASSWORD_REQUIRED.getMessage());
        }

        // 비밀번호 검증
        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            log.warn("비밀번호 확인 실패: email={}", user.getEmail());
            throw new PasswordMismatchException(UserMessage.PASSWORD_MISMATCH.getMessage());
        }

        log.debug("비밀번호 확인 성공: email={}", user.getEmail());

        return PasswordCheckResponse.builder()
                .verified(true)
                .message("비밀번호가 확인되었습니다")
                .build();
    }

    /**
     * 사용자 정보 수정
     * - 닉네임, 생년월일, 자기소개, 설정, 아바타 이미지 수정 가능
     * - 닉네임 변경 시 중복 체크 (본인 제외)
     * - 아바타 변경 시 기존 파일 삭제 후 새 파일 저장
     *
     * @param user    사용자 엔티티
     * @param request 수정 요청 DTO
     * @return 수정된 사용자 정보
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional
    public UserResponse updateUser(User user, UserUpdateRequest request) {
        validateUser(user);

        // 닉네임 수정 (중복 체크)
        if (StringUtils.hasText(request.getNickname())) {
            String newNickname = request.getNickname().trim();

            // 현재 닉네임과 다를 때만 중복 체크
            if (!newNickname.equals(user.getNickname())) {
                userRepository.findByNickname(newNickname).ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        log.warn("닉네임 중복: email={}, nickname={}", user.getEmail(), newNickname);
                        throw new DuplicateNicknameException(UserMessage.DUPLICATE_NICKNAME.format(newNickname));
                    }
                });
                user.setNickname(newNickname);
                log.debug("닉네임 수정: {} -> {}", user.getNickname(), newNickname);
            }
        }

        // 생년월일 수정
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
            log.debug("생년월일 수정: {}", request.getBirthDate());
        }

        // UserProfile 업데이트 (공통 로직)
        updateUserProfile(user, request.getBio(), request.getSettings(), request.getAvatar());

        // User 저장
        User savedUser = userRepository.save(user);

        log.debug("사용자 정보 수정 완료: email={}, nickname={}", savedUser.getEmail(), savedUser.getNickname());

        // 수정된 사용자 정보 반환
        return getUser(savedUser);
    }

    /**
     * 프로필 수정
     * - UserProfile의 필드만 수정 (bio, avatarUrl, settings)
     * - 아바타 변경 시 기존 파일 삭제 후 새 파일 저장
     *
     * @param user    사용자 엔티티
     * @param request 프로필 수정 요청 DTO
     * @return 수정된 사용자 정보
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional
    public UserResponse updateProfile(User user, UserProfileUpdateRequest request) {
        validateUser(user);

        // UserProfile 업데이트 (공통 로직)
        updateUserProfile(user, request.getBio(), request.getSettings(), request.getAvatar());

        // User 저장 (연관관계 업데이트)
        User savedUser = userRepository.save(user);

        // 수정된 사용자 정보 반환
        return getUser(savedUser);
    }

    /**
     * UserProfile 업데이트 (공통 로직)
     * - UserProfile 조회 또는 생성
     * - bio, settings, avatar 업데이트
     *
     * @param user     사용자 엔티티
     * @param bio      자기소개 (null 가능)
     * @param settings 설정 JSON 문자열 (null 가능)
     * @param avatar   아바타 이미지 파일 (null 가능)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    private void updateUserProfile(User user, String bio, String settings, MultipartFile avatar) {
        // UserProfile 조회 또는 생성
        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = UserProfile.builder()
                    .user(user)
                    .build();
            user.setUserProfile(userProfile);
            log.debug("UserProfile 생성: userId={}", user.getId());
        }

        // 자기소개 수정
        if (bio != null) {
            userProfile.setBio(bio);
            log.debug("자기소개 수정");
        }

        // 설정 수정
        if (settings != null) {
            Map<String, Object> parsedSettings = settingsUtils.parseSettings(settings);
            userProfile.setSettings(parsedSettings);
            log.debug("설정 수정");
        }

        // 아바타 이미지 수정
        if (avatar != null && !avatar.isEmpty()) {
            updateAvatar(userProfile, avatar, user.getId());
        }

        // UserProfile 저장
        userProfileRepository.save(userProfile);
    }

    /**
     * 아바타 이미지 업데이트
     * - 기존 아바타 파일 삭제
     * - 새 아바타 파일 저장
     *
     * @param userProfile UserProfile 엔티티
     * @param avatar      아바타 이미지 파일
     * @param userId      사용자 ID
     * @apiNote 점검O
     * @since 2025-12-28
     */
    private void updateAvatar(UserProfile userProfile, MultipartFile avatar, Long userId) {
        // 기존 아바타 파일 삭제
        if (StringUtils.hasText(userProfile.getAvatarUrl())) {
            avatarService.deleteAvatar(userProfile.getAvatarUrl());
            log.debug("기존 아바타 파일 삭제: {}", userProfile.getAvatarUrl());
        }

        // 새 아바타 파일 저장
        String newAvatarUrl = avatarService.saveAvatar(avatar, userId);
        userProfile.setAvatarUrl(newAvatarUrl);
        log.debug("새 아바타 파일 저장: {}", newAvatarUrl);
    }


    /**
     * 닉네임으로 사용자 프로필 정보 조회 (QnA 게시판용)
     * - QnA 게시판에서 작성자를 클릭했을 때 표시되는 간단한 정보
     * - 공개 정보만 포함 (이메일, 생년월일 등 민감 정보 제외)
     * - QnA 활동 통계 정보 포함
     *
     * @param nickname 사용자 닉네임
     * @return 사용자 프로필 정보 (QnA 통계 포함)
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-12-30
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileByNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            log.warn("닉네임이 제공되지 않았습니다");
            throw new UserNotFoundException(UserMessage.USER_NOT_FOUND.getMessage());
        }

        // 닉네임으로 사용자 조회
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다: nickname={}", nickname);
                    return new UserNotFoundException(UserMessage.USER_NOT_FOUND.getMessage());
                });

        log.debug("사용자 프로필 조회: nickname={}", nickname);

        // 프로필 정보 가져오기 (LAZY 로딩이므로 필요시 조회)
        String avatarUrl = null;
        String bio = null;
        if (user.getUserProfile() != null) {
            avatarUrl = user.getUserProfile().getAvatarUrl();
            bio = user.getUserProfile().getBio();
        }

        // QnA 통계 정보 조회
        long questionCount = qnaQuestionRepository.countByUserAndIsAnonymousFalse(user);
        long answerCount = qnaAnswerRepository.countByUserAndAnswerType(user, AnswerType.USER);
        long acceptedAnswerCount = qnaAnswerRepository.countByUserAndIsAcceptedTrue(user);
        long totalScore = qnaAnswerRepository.sumScoreByUser(user);

        // 프로필 응답 생성
        return UserProfileResponse.builder()
                .nickname(user.getNickname())
                .avatarUrl(avatarUrl)
                .bio(bio)
                .createdAt(user.getCreatedAt())
                .statistics(UserProfileResponse.QnaStatistics.builder()
                        .questionCount(questionCount)
                        .answerCount(answerCount)
                        .acceptedAnswerCount(acceptedAnswerCount)
                        .totalScore(totalScore)
                        .build())
                .build();
    }

}

