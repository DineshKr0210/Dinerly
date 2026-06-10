package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.LoginRequest;
import com.restaurant.waitlist.backend.dto.response.LoginResponse;
import com.restaurant.waitlist.backend.dto.response.UserResponse;
import com.restaurant.waitlist.backend.entity.PasswordResetToken;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.PasswordResetTokenRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Defensive handling: trim incoming password and guard against nulls
        String rawPassword = loginRequest.getPassword() == null ? "" : loginRequest.getPassword().trim();
        String storedPassword = user.getPassword();

        if (storedPassword == null || storedPassword.isEmpty()) {
            logger.debug("Stored password for user {} is null/empty", user.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        boolean matched = false;
        try {
            // If the stored password looks like a BCrypt hash, use passwordEncoder.matches
            String lower = storedPassword.toLowerCase();
            if (lower.startsWith("$2a$") || lower.startsWith("$2b$") || lower.startsWith("$2y$")) {
                matched = passwordEncoder.matches(rawPassword, storedPassword);
            } else {
                // Fallback: maybe the value in DB is plaintext (e.g., inserted manually). Compare directly
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

