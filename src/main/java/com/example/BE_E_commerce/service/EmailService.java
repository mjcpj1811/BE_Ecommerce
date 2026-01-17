package com.example.BE_E_commerce.service;

import jakarta.mail.MessagingException;
import jakarta. mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.beans.factory.annotation. Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org. springframework.mail.javamail. MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf. TemplateEngine;
import org. thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.support-email}")
    private String supportEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message. setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email with template
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender. createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email properties
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // Create mutable map and add all variables
            Map<String, Object> allVariables = new HashMap<>(variables);
            allVariables.put("appName", appName);
            allVariables.put("supportEmail", supportEmail);
            allVariables.put("frontendUrl", frontendUrl);

            // Process template
            Context context = new Context();
            context.setVariables(allVariables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent to: {} with template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Error sending HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String fullName, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("email", to);
        variables.put("resetUrl", resetUrl);
        variables.put("resetToken", resetToken);

        sendHtmlEmail(
                to,
                "Reset Your Password - " + appName,
                "reset-password",
                variables
        );

        log.info("Password reset email sent to: {}", to);
    }

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(String to, String fullName, String username) {
        String loginUrl = frontendUrl + "/login";

        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("username", username);
        variables.put("email", to);
        variables.put("loginUrl", loginUrl);

        sendHtmlEmail(
                to,
                "Welcome to " + appName + "!",
                "welcome",
                variables
        );

        log.info("Welcome email sent to: {}", to);
    }

    /**
     * Send order confirmation email (bonus)
     */
    public void sendOrderConfirmationEmail(String to, String fullName, String orderCode, String orderTotal) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("orderCode", orderCode);
        variables.put("orderTotal", orderTotal);
        variables.put("orderUrl", frontendUrl + "/orders/" + orderCode);

        sendHtmlEmail(
                to,
                "Order Confirmation - " + orderCode,
                "order-confirmation",
                variables
        );
    }
}