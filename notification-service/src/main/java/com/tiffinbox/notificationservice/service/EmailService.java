package com.tiffinbox.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends email via Gmail SMTP. When {@code notification.mail.enabled=false} (the default),
 * it logs the email instead of sending — so the Kafka flow is fully testable without
 * real credentials. Set the flag true and export MAIL_USERNAME / MAIL_PASSWORD to send.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${notification.mail.enabled}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String from;

    /** Sends (or mock-logs) an email. Throws on a real send failure. */
    public void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[MOCK EMAIL] to={} subject='{}' body='{}'", to, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent to {}", to);
    }
}
