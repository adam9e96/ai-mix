package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.auth.dto.SignupRequest;
import com.aimix_aimixapi.common.util.SettingsUtils;
import com.aimix_aimixapi.user.dto.UserResponse;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.entity.UserProfile;
import com.aimix_aimixapi.user.repository.UserProfileRepository;
import com.aimix_aimixapi.user.repository.UserRepository;
import com.aimix_aimixapi.user.service.UserAvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 생성 서비스
 * - 회원가입 시 사용자 및 프로필 생성 로직 담당
 * - User 엔티티와 UserProfile 엔티티를 생성하고 저장
 *
 * <p>주요 기능:
 * <ul>
 *   <li>비밀번호 암호화 (BCrypt)</li>
 *   <li>User 엔티티 생성 및 저장</li>
 *   <li>UserProfile 엔티티 생성 및 저장</li>
 *   <li>아바타 이미지 업로드 처리</li>
 *   <li>UserResponse DTO 변환</li>
 * </ul>
 *
 * <p>동작 과정:
 * <ol>
 *   <li>이메일, 닉네임 trim 처리</li>
 *   <li>비밀번호 BCrypt 암호화</li>
 *   <li>User 엔티티 생성 및 저장</li>
 *   <li>UserProfile 엔티티 생성 (아바타 포함)</li>
 *   <li>UserProfile 저장 및 User와 연관관계 설정</li>
 *   <li>UserResponse DTO로 변환하여 반환</li>
 * </ol>
 *
 * @apiNote 점검O
 * @since 2026-01-05
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserCreationService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAvatarService avatarService;
    private final SettingsUtils settingsUtils;

    /**
     * 사용자 및 프로필 생성
     * - 회원가입 요청을 받아 User와 UserProfile을 생성하고 저장
     * - 아바타 이미지가 있으면 업로드 처리
     *
     * <p>동작 과정:
     * <ol>
     *   <li>이메일, 닉네임 trim 처리</li>
     *   <li>비밀번호 BCrypt 암호화</li>
     *   <li>User 엔티티 생성 및 저장</li>
     *   <li>UserProfile 엔티티 생성 (아바타 포함)</li>
     *   <li>UserProfile 저장 및 User와 연관관계 설정</li>
     *   <li>UserResponse DTO로 변환하여 반환</li>
     * </ol>
     *
     * <p>주의사항:
     * <ul>
     *   <li>이 메서드 호출 전에 SignupValidator.validate()로 검증이 완료되어야 함</li>
     *   <li>비밀번호는 BCrypt로 암호화되어 저장됨</li>
     *   <li>아바타 이미지는 UserAvatarService.saveAvatar()를 통해 업로드됨</li>
     *   <li>설정(settings)은 SettingsUtils.parseSettings()로 파싱됨</li>
     * </ul>
     *
     * @param request 회원가입 요청 DTO (검증 완료된 상태, null 불가)
     * @return 생성된 사용자 정보 DTO (UserResponse)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    @Transactional
    public UserResponse createUser(SignupRequest request) {
        String email = request.getEmail().trim();
        String nickname = request.getNickname().trim();

        log.debug("회원가입 시작: email={}, nickname={}", email, nickname);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = createUserEntity(request, email, nickname, encodedPassword);
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료: email={}, id={}, nickname={}",
                savedUser.getEmail(), savedUser.getId(), savedUser.getNickname());

        // 프로필 생성
        UserProfile userProfile = createUserProfile(savedUser, request);
        userProfileRepository.save(userProfile);
        savedUser.setUserProfile(userProfile);

        // UserResponse 변환
        return buildUserResponse(savedUser, userProfile);
    }

    /**
     * User 엔티티 생성
     * - 회원가입 요청 정보를 바탕으로 User 엔티티 생성
     *
     * @param request         회원가입 요청 DTO
     * @param email           이메일 (trim 처리됨)
     * @param nickname        닉네임 (trim 처리됨)
     * @param encodedPassword 암호화된 비밀번호
     * @return 생성된 User 엔티티
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private User createUserEntity(SignupRequest request, String email, String nickname, String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .birthDate(request.getBirthDate())
                .isAgreed(request.getIsAgreed())
                .build();
    }

    /**
     * UserProfile 엔티티 생성
     * - User와 연관된 UserProfile 엔티티 생성
     * - 아바타 이미지가 있으면 업로드 처리
     *
     * <p>처리 내용:
     * <ul>
     *   <li>bio(자기소개) 설정</li>
     *   <li>settings(설정) JSON 파싱 및 설정</li>
     *   <li>아바타 이미지가 있으면 업로드 및 URL 설정</li>
     * </ul>
     *
     * @param savedUser 저장된 User 엔티티 (ID가 할당된 상태)
     * @param request   회원가입 요청 DTO
     * @return 생성된 UserProfile 엔티티
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private UserProfile createUserProfile(User savedUser, SignupRequest request) {
        UserProfile userProfile = UserProfile.builder()
                .user(savedUser)
                .bio(request.getBio())
                .settings(settingsUtils.parseSettings(request.getSettings()))
                .build();

        // 아바타 파일 처리
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = avatarService.saveAvatar(request.getAvatar(), savedUser.getId());
            userProfile.setAvatarUrl(avatarUrl);
            log.debug("아바타 업로드 완료: userId={}, avatarUrl={}", savedUser.getId(), avatarUrl);
        }

        return userProfile;
    }

    /**
     * UserResponse DTO 빌드
     * - User 엔티티와 UserProfile 엔티티를 UserResponse DTO로 변환
     *
     * @param user        User 엔티티 (null 불가)
     * @param userProfile UserProfile 엔티티 (null 불가)
     * @return UserResponse DTO
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private UserResponse buildUserResponse(User user, UserProfile userProfile) {
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .avatarUrl(userProfile.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .birthDate(user.getBirthDate())
                .bio(userProfile.getBio())
                .lastLoginAt(user.getLastLoginAt())
                .settings(userProfile.getSettings())
                .build();
    }
}
