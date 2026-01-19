package com.aimix_aimixapi.auth.service;

import com.aimix_aimixapi.auth.dto.DuplicateCheckResponse;
import com.aimix_aimixapi.auth.dto.LoginRequest;
import com.aimix_aimixapi.auth.dto.SignupRequest;
import com.aimix_aimixapi.auth.dto.TokenResponse;
import com.aimix_aimixapi.auth.jwt.JwtProvider;
import com.aimix_aimixapi.common.exception.domain.auth.InvalidRefreshTokenException;
import com.aimix_aimixapi.common.exception.domain.auth.RefreshTokenRequiredException;
import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.user.dto.UserResponse;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 인증 서비스 (Facade)
 * - 회원가입, 로그인, 토큰 재발급, 로그아웃 처리
 * - 각 책임은 별도 서비스로 분리됨
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SignupValidator signupValidator;
    private final UserCreationService userCreationService;
    private final LoginValidator loginValidator;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final UserResponseBuilder userResponseBuilder;
    private final JwtProvider jwtProvider;
    private final VerificationCodeService verificationCodeService;

    /**
     * 이메일 인증 코드 발송
     *
     * @param email 수신 이메일
     */
    public void sendVerificationCode(String email) {
        verificationCodeService.sendVerificationCode(email);
    }

    /**
     * 이메일 인증 코드 검증
     *
     * @param email 이메일
     * @param code  인증 코드
     * @return 검증 성공 여부
     */
    public boolean verifyEmail(String email, String code) {
        return verificationCodeService.verifyCode(email, code);
    }

    /**
     * 이메일 중복 체크
     * - 회원가입 전 이메일 사용 가능 여부 확인
     *
     * @param email 확인할 이메일 주소
     * @return 이메일 중복 체크 응답 (사용 가능 여부 및 메시지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.warn("이메일 중복 체크 요청: 이메일이 제공되지 않음");
            return DuplicateCheckResponse.builder()
                    .available(false)
                    .message("이메일을 입력해주세요")
                    .build();
        }

        String trimmedEmail = email.trim();
        boolean available = isEmailAvailable(trimmedEmail);
        log.debug("이메일 중복 체크: email={}, available={}", trimmedEmail, available);

        return DuplicateCheckResponse.builder()
                .available(available)
                .message(available ? "사용 가능한 이메일입니다" : "이미 사용 중인 이메일입니다")
                .build();
    }

    /**
     * 닉네임 중복 체크
     * - 회원가입 전 닉네임 사용 가능 여부 확인
     *
     * @param nickname 확인할 닉네임
     * @return 닉네임 중복 체크 응답 (사용 가능 여부 및 메시지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            log.warn("닉네임 중복 체크 요청: 닉네임이 제공되지 않음");
            return DuplicateCheckResponse.builder()
                    .available(false)
                    .message("닉네임을 입력해주세요")
                    .build();
        }

        String trimmedNickname = nickname.trim();
        boolean available = isNicknameAvailable(trimmedNickname);
        log.debug("닉네임 중복 체크: nickname={}, available={}", trimmedNickname, available);

        return DuplicateCheckResponse.builder()
                .available(available)
                .message(available ? "사용 가능한 닉네임입니다" : "이미 사용 중인 닉네임입니다")
                .build();
    }

    /**
     * 이메일 사용 가능 여부 확인 (내부 로직)
     * - 중복 체크 로직의 단일 책임 원칙 준수
     *
     * @param email 확인할 이메일 주소 (trim 처리된 값)
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    private boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * 닉네임 사용 가능 여부 확인 (내부 로직)
     * - 중복 체크 로직의 단일 책임 원칙 준수
     *
     * @param nickname 확인할 닉네임 (trim 처리된 값)
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    private boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    /**
     * 회원가입 (multipart/form-data 지원)
     * - 이메일, 닉네임 중복 체크
     * - 비밀번호 암호화
     * - 사용자 및 프로필 생성
     * - 아바타 이미지 업로드 지원
     * - 회원가입 시에는 토큰을 생성하지 않고, 사용자 정보만 반환
     *
     * @param request 회원가입 요청 DTO (이메일, 비밀번호, 닉네임, 생년월일, 동의 여부, 자기소개, 설정, 아바타)
     * @return 사용자 정보 DTO
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        signupValidator.validate(request);
        return userCreationService.createUser(request);
    }

    /**
     * 로그인
     * - 이메일과 비밀번호로 사용자 인증
     * - 인증 성공 시 AccessToken과 RefreshToken을 HttpOnly 쿠키로 설정
     * - 마지막 로그인 시간 업데이트
     * - 사용자 프로필 정보 포함하여 응답 반환
     *
     * @param request      로그인 요청 DTO (이메일, 비밀번호)
     * @param httpResponse HTTP 응답 객체 (쿠키 설정용)
     * @return 사용자 정보 DTO (토큰은 쿠키로 전달)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional
    public UserResponse login(LoginRequest request, HttpServletResponse httpResponse) {
        String email = loginValidator.validate(request);
        User user = authenticationService.authenticate(email, request.getPassword());
        tokenService.generateAndSetTokens(user.getEmail(), httpResponse);
        return userResponseBuilder.build(user);
    }

    /**
     * 토큰 재발급
     * - RefreshToken은 HttpOnly Secure 쿠키에서 읽어옴
     * - JWT 토큰에서 사용자 이메일 추출
     * - Redis에서 RefreshToken 검증
     * - 기존 RefreshToken 삭제 후 새 토큰 생성
     * - 새 AccessToken과 RefreshToken을 HttpOnly Secure 쿠키로 설정
     *
     * @param refreshToken RefreshToken 값 (쿠키에서 읽어온 값)
     * @param httpResponse HTTP 응답 객체 (쿠키 설정용)
     * @return 토큰 재발급 응답 DTO (토큰은 쿠키로 전달)
     * @throws RefreshTokenRequiredException RefreshToken이 제공되지 않은 경우
     * @throws InvalidRefreshTokenException  RefreshToken이 유효하지 않은 경우
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken, HttpServletResponse httpResponse) {
        // RefreshToken 검증
        if (!StringUtils.hasText(refreshToken)) {
            log.warn("토큰 재발급 요청: RefreshToken이 제공되지 않음");
            throw new RefreshTokenRequiredException(AuthMessage.REFRESH_TOKEN_REQUIRED.getMessage());
        }

        log.debug("토큰 재발급 시도");

        // JWT 토큰에서 username 추출 (예외 처리 포함)
        String username;
        try {
            username = jwtProvider.getUsername(refreshToken);
            if (!StringUtils.hasText(username)) {
                log.warn("토큰 재발급 실패: RefreshToken에서 username 추출 실패 - username이 null 또는 빈 문자열");
                throw new InvalidRefreshTokenException(AuthMessage.INVALID_REFRESH_TOKEN.getMessage());
            }
        } catch (InvalidRefreshTokenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("토큰 재발급 실패: RefreshToken에서 username 추출 실패 - {}", e.getMessage());
            throw new InvalidRefreshTokenException(AuthMessage.INVALID_REFRESH_TOKEN.getMessage());
        }

        // Redis + JWT 검증
        if (!jwtProvider.validateRefreshToken(username, refreshToken)) {
            log.warn("토큰 재발급 실패: RefreshToken 검증 실패 - email={}", username);
            throw new InvalidRefreshTokenException(AuthMessage.INVALID_REFRESH_TOKEN.getMessage());
        }

        // 기존 RefreshToken 삭제 (Redis에서)
        jwtProvider.deleteRefresh(username);
        log.debug("기존 RefreshToken 삭제 완료: email={}", username);

        // 새로운 토큰 생성 및 쿠키 설정
        tokenService.generateAndSetTokens(username, httpResponse);

        log.info("토큰 재발급 완료: email={}", username);

        // 응답 생성: AccessToken과 RefreshToken은 모두 쿠키로 전달
        return TokenResponse.empty();
    }

    /**
     * 로그아웃
     * - RefreshToken이 제공된 경우 Redis에서 삭제
     * - AccessToken 쿠키 삭제
     * - RefreshToken 쿠키 삭제
     *
     * @param refreshToken RefreshToken 값 (쿠키에서 읽어온 값, null 가능)
     * @param httpResponse HTTP 응답 객체 (쿠키 삭제용)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Transactional
    public void logout(String refreshToken, HttpServletResponse httpResponse) {
        log.debug("로그아웃 요청");

        // RefreshToken이 제공된 경우 Redis에서 삭제
        if (StringUtils.hasText(refreshToken)) {
            try {
                String username = jwtProvider.getUsername(refreshToken);
                if (StringUtils.hasText(username)) {
                    jwtProvider.deleteRefresh(username);
                    log.info("로그아웃 완료: email={}, RefreshToken 삭제됨", username);
                } else {
                    log.warn("로그아웃: RefreshToken에서 username 추출 실패 - username이 null 또는 빈 문자열");
                }
            } catch (Exception e) {
                log.warn("로그아웃: RefreshToken에서 username 추출 실패 (무시) - {}", e.getMessage());
            }
        } else {
            log.debug("로그아웃: RefreshToken 쿠키가 없음");
        }

        // 쿠키 제거
        tokenService.clearAccessTokenCookie(httpResponse);
        tokenService.clearRefreshTokenCookie(httpResponse);

        log.debug("로그아웃 처리 완료: 쿠키 삭제됨");
    }

}
