package com.aimix_aimixapi.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    @Async
    public void sendEmail(String to, String subject, String text) {
        log.info("Sending email to: {}", to);
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true indicates HTML

            emailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (RuntimeException | MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }
}
