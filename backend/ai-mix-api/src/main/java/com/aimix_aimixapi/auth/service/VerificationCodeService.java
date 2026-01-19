package com.aimix_aimixapi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final String PREFIX = "EMAIL_VERIFICATION:";
    private static final long EXPIRATION_MINUTES = 5;

    public void sendVerificationCode(String email) {
        String code = generateCode();
        String key = PREFIX + email;

        // Redis에 저장 (5분 만료)
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(EXPIRATION_MINUTES));

        // 이메일 전송
        String subject = "[AI-MIX] 이메일 인증 코드";
        String content = createEmailContent(code);
        emailService.sendEmail(email, subject, content);

        log.info("Verification code sent to email: {}", email);
    }

    public boolean verifyCode(String email, String code) {
        String key = PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key); // 인증 성공 시 코드 삭제
            return true;
        }
        return false;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    private String createEmailContent(String code) {
        // 간단한 HTML 템플릿
        return "<div style='margin:100px;'>" +
                "<h1>안녕하세요, AI-MIX 입니다.</h1>" +
                "<br>" +
                "<p>아래 인증코드를 회원가입 창에 입력해주세요.</p>" +
                "<br>" +
                "<div align='center' style='border:1px solid black; font-family:verdana;'>" +
                "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>" +
                "<div style='font-size:130%'>" +
                "CODE : <strong>" + code + "</strong>" +
                "</div>" +
                "</div>" +
                "<br/>" +
                "</div>";
    }
}
