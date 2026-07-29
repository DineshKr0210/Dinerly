package com.restaurant.waitlist.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String configuredFromEmail;

    @Value("${app.frontend.url:http://dev.dinerly.ca}")
    private String frontendUrl;

    @Value("${app.email.provider:resend}")
    private String emailProvider;

    @Value("${app.email.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.email.resend.from:}")
    private String resendFrom;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
        String body = "Click the link below to verify your email:\n\n"
                + verificationLink + "\n\n"
                + "If you did not request this, please ignore this email.";
        sendEmail(toEmail, "Verify your email - Waitlist Management", body, "verification");
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        String body = "Click the link below to reset your password (Valid for 1 hour):\n\n"
                + resetLink + "\n\n"
                + "If you didn't request this, please ignore this email.";
        sendEmail(toEmail, "Password Reset Request - Waitlist Management", body, "password-reset");
    }

    public void sendWaitlistNotification(String toEmail, String guestName, String estimatedWaitTime) {
        String body = "Hello " + guestName + ",\n\n"
                + "Your table is ready! Please come to the restaurant now.\n"
                + "Estimated wait time: " + estimatedWaitTime + " minutes\n\n"
                + "Thank you for your patience!";
        sendEmail(toEmail, "Table Ready - Your Waitlist Status", body, "waitlist-notification");
    }

    public void sendNightlySummary(String toEmail, String restaurantName, String summaryBody) {
        sendEmail(toEmail, "Nightly summary for " + restaurantName, summaryBody, "nightly-summary");
    }

    private void sendEmail(String toEmail, String subject, String body, String purpose) {
        try {
            if ("resend".equalsIgnoreCase(emailProvider) && resendApiKey != null && !resendApiKey.isBlank()) {
                sendViaResend(toEmail, subject, body);
                return;
            }

            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(resolveFromEmail());
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                return;
            }

            log.warn("No email provider configured for {}; skipping email to {}", purpose, toEmail);
        } catch (Exception e) {
            log.warn("Email delivery failed for {} to {}: {}", purpose, toEmail, e.getMessage());
        }
    }

    private void sendViaResend(String toEmail, String subject, String body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", (resendFrom != null && !resendFrom.isBlank()) ? resendFrom : resolveFromEmail());
        payload.put("to", new String[]{toEmail});
        payload.put("subject", subject);
        payload.put("html", "<p>" + body.replace("\n", "<br/>") + "</p>");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Resend returned " + response.getStatusCode());
        }
    }

    private String resolveFromEmail() {
        if (configuredFromEmail != null && !configuredFromEmail.isBlank()) {
            return configuredFromEmail;
        }
        return fromEmail != null ? fromEmail : "no-reply@dinerly.local";
    }
}

