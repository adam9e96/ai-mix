package com.aimix_aimixapi.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:true}")
    private boolean starttlsRequired;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}")
    private int connectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:5000}")
    private int timeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout:5000}")
    private int writeTimeout;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username != null ? username.trim() : null);
        // 앱 비밀번호는 공백을 제거해야 함
        mailSender.setPassword(password != null ? password.replace(" ", "").trim() : null);
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = new Properties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.required", starttlsRequired);
        props.put("mail.smtp.connectiontimeout", connectionTimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writeTimeout);

        mailSender.setJavaMailProperties(props);

        return mailSender;
    }

    @jakarta.annotation.PostConstruct
    public void logConfig() {
        System.out.println("=========================================================");
        System.out.println("[MailConfig] Loaded Configuration:");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Username: " + username);
        // Password check
        boolean hasPassword = password != null && !password.isBlank();
        System.out.println("Password set: " + hasPassword + " (Length: " + (hasPassword ? password.length() : 0) + ")");
        System.out.println("=========================================================");
    }
}
