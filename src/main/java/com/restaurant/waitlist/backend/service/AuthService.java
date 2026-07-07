package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.LoginRequest;
import com.restaurant.waitlist.backend.dto.request.RegisterRequest;
import com.restaurant.waitlist.backend.dto.request.ResendVerificationRequest;
import com.restaurant.waitlist.backend.dto.response.LoginResponse;
import com.restaurant.waitlist.backend.dto.response.UserResponse;
import com.restaurant.waitlist.backend.entity.EmailVerificationToken;
import com.restaurant.waitlist.backend.entity.PasswordResetToken;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.EmailVerificationTokenRepository;
import com.restaurant.waitlist.backend.repository.PasswordResetTokenRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new RuntimeException("User account is disabled");
        }

        if (user.getRole() == User.UserRole.RESTAURANT && !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email not verified");
        }

        String rawPassword = loginRequest.getPassword() == null ? "" : loginRequest.getPassword().trim();
        String storedPassword = user.getPassword();

        if (storedPassword == null || storedPassword.isEmpty()) {
            logger.debug("Stored password for user {} is null/empty", user.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        boolean matched = false;
        try {
            String lower = storedPassword.toLowerCase();
            if (lower.startsWith("$2a$") || lower.startsWith("$2b$") || lower.startsWith("$2y$")) {
                matched = passwordEncoder.matches(rawPassword, storedPassword);
            } else {
                matched = storedPassword.equals(rawPassword);
            }
        } catch (Exception e) {
            logger.warn("Error while matching password for user {}: {}", user.getEmail(), e.getMessage());
            matched = false;
        }

        if (!matched) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = generateToken(user);
        UserResponse userResponse = UserResponse.fromUser(user);

        return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Transactional
    public void registerRestaurant(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.RESTAURANT)
                .emailVerified(false)
                .enabled(true)
                .build();

        userRepository.save(user);
        EmailVerificationToken token = createVerificationToken(user);
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.getUsed()) {
            throw new RuntimeException("Verification token has already been used");
        }

        if (LocalDateTime.now().isAfter(verificationToken.getExpiresAt())) {
            throw new RuntimeException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }

        EmailVerificationToken token = createVerificationToken(user);
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }

    private EmailVerificationToken createVerificationToken(User user) {
        return EmailVerificationToken.builder()
                .user(user)
                .token(EmailVerificationToken.generateToken())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(expiryDate)
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(token);
        emailService.sendPasswordResetEmail(email, resetToken);
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.getIsUsed()) {
            throw new RuntimeException("Token has already been used");
        }

        if (LocalDateTime.now().isAfter(resetToken.getExpiryDate())) {
            throw new RuntimeException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String generateToken(User user) {
        return jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().toString(),
                user.getRestaurantId()
        );
    }
}

