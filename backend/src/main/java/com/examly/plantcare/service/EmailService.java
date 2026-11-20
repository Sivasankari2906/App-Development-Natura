package com.examly.plantcare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends email through SMTP when spring.mail.host is configured (SPRING_MAIL_HOST).
 * Without SMTP it only logs the message, which is meant for local development.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.from:no-reply@natura.local}")
    private String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    /** @return true if the message was sent (or logged in development mode). */
    public boolean send(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("SMTP is not configured, so this email is only logged. To: {} | Subject: {}\n{}", to, subject, body);
            return true;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
            return true;
        } catch (MailException e) {
            log.error("Could not send email to {}: {}", to, e.getMessage());
            return false;
        }
    }
}
