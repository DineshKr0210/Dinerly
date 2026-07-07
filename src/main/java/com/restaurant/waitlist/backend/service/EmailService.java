package com.restaurant.waitlist.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://dev.dinerly.ca}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify your email - Waitlist Management");
            message.setText("Click the link below to verify your email:\n\n"
                    + verificationLink + "\n\n"
                    + "If you did not request this, please ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Waitlist Management");
            message.setText("Click the link below to reset your password (Valid for 1 hour):\n\n"
                    + resetLink + "\n\n"
                    + "If you didn't request this, please ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    public void sendWaitlistNotification(String toEmail, String guestName, String estimatedWaitTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Table Ready - Your Waitlist Status");
            message.setText("Hello " + guestName + ",\n\n"
                    + "Your table is ready! Please come to the restaurant now.\n"
                    + "Estimated wait time: " + estimatedWaitTime + " minutes\n\n"
                    + "Thank you for your patience!");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending notification email: " + e.getMessage());
        }
    }

    public void sendNightlySummary(String toEmail, String restaurantName, String summaryBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Nightly summary for " + restaurantName);
            message.setText(summaryBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending nightly summary email: " + e.getMessage());
        }
    }
}

